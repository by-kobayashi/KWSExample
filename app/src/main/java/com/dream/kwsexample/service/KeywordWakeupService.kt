package com.dream.kwsexample.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import com.k2fsa.sherpa.onnx.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
 * 关键词唤醒服务
 * 基于SherpaOnnx实现语音唤醒功能
 */
class KeywordWakeupService(private val context: Context) {

    companion object {
        private const val TAG = "KeywordWakeupService"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_INTERVAL_MS = 100L // 100ms缓冲区
    }

    private var kws: KeywordSpotter? = null
    private var stream: OnlineStream? = null
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var audioProcessor: AudioProcessor? = null

    @Volatile
    private var isListening = false

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 唤醒回调
    private var wakeupCallback: ((String) -> Unit)? = null

    // 默认关键词（中文）
    private val defaultKeywords = "x iǎo ān x iǎo ān"

    // 当前配置
    private var currentKeywords = defaultKeywords
    private var currentThreshold = 0.25f

    // AEC 和 NS 开关
    private var aecEnabled = true
    private var nsEnabled = true

    // 设备支持情况（在初始化时检查）
    private val aecAvailable: Boolean by lazy {
        AcousticEchoCanceler.isAvailable()
    }

    private val nsAvailable: Boolean by lazy {
        NoiseSuppressor.isAvailable()
    }

    /**
     * 初始化关键词识别器
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing keyword spotter...")

            // 使用中文模型 (type = 0)
            val modelConfig = getKwsModelConfig(type = 0) ?: throw IllegalStateException("Failed to get model config")

            val config = KeywordSpotterConfig(
                featConfig = FeatureConfig(
                    sampleRate = SAMPLE_RATE,
                    featureDim = 80
                ),
                modelConfig = modelConfig,
                keywordsFile = getKeywordsFile(type = 0),
                keywordsThreshold = currentThreshold,
                keywordsScore = 1.5f
            )

            Log.d(TAG, "Creating KeywordSpotter with config (threshold: $currentThreshold)...")
            kws = KeywordSpotter(
                assetManager = context.assets,
                config = config
            )

            Log.d(TAG, "Creating stream with keywords: $currentKeywords")
            stream = kws?.createStream(currentKeywords)

            if (stream == null) {
                throw IllegalStateException("Failed to create keyword stream: stream is null")
            }

            if (stream?.ptr == 0L || stream?.ptr == null) {
                throw IllegalStateException("Failed to create keyword stream: invalid pointer")
            }

            Log.d(TAG, "Keyword spotter initialized successfully, stream ptr: ${stream?.ptr}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize keyword spotter", e)
            // 清理资源
            stream = null
            kws?.release()
            kws = null
            throw e
        }
    }

    /**
     * 开始监听关键词
     */
    fun startListening(callback: (String) -> Unit) {
        if (isListening) {
            Log.w(TAG, "Already listening")
            return
        }

        wakeupCallback = callback

        try {
            if (!initializeMicrophone()) {
                Log.e(TAG, "Failed to initialize microphone")
                return
            }

            audioRecord?.startRecording()
            isListening = true

            recordingThread = thread(true) {
                processAudioSamples()
            }

            Log.d(TAG, "Started keyword listening")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            stopListening()
        }
    }

    /**
     * 停止监听关键词
     */
    fun stopListening() {
        if (!isListening) {
            return
        }

        isListening = false

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            recordingThread?.interrupt()
            recordingThread = null

            stream?.release()

            Log.d(TAG, "Stopped keyword listening")

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listening", e)
        }
    }

    /**
     * 设置自定义关键词
     */
    fun setKeywords(keywords: String) {
        try {
            currentKeywords = keywords
            stream?.release()
            stream = kws?.createStream(keywords)

            if (stream?.ptr == 0L) {
                Log.e(TAG, "Failed to set keywords: $keywords")
            } else {
                Log.d(TAG, "Keywords set to: $keywords")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to set keywords", e)
        }
    }

    /**
     * 设置检测阈值
     * 注意: 阈值修改需要重新初始化才能生效
     */
    suspend fun setThreshold(threshold: Float) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Setting threshold to: $threshold")

            // 保存当前状态
            val wasListening = isListening
            val callback = wakeupCallback

            // 停止监听
            if (wasListening) {
                stopListening()
            }

            // 更新阈值
            currentThreshold = threshold

            // 释放旧的资源
            stream?.release()
            stream = null
            kws?.release()
            kws = null

            // 重新初始化
            initialize()

            // 如果之前在监听，恢复监听
            if (wasListening && callback != null) {
                startListening(callback)
            }

            Log.d(TAG, "Threshold updated successfully to: $threshold")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to set threshold", e)
            throw e
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        stopListening()

        try {
            audioProcessor?.release()
            audioProcessor = null

            kws?.release()
            kws = null
            stream = null

            scope.cancel()

            Log.d(TAG, "Keyword wakeup service released")

        } catch (e: Exception) {
            Log.e(TAG, "Error releasing service", e)
        }
    }

    /**
     * 初始化麦克风
     */
    private fun initializeMicrophone(): Boolean {
        try {
            // 检查录音权限
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val permission = android.Manifest.permission.RECORD_AUDIO
                if (context.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "RECORD_AUDIO permission not granted")
                    return false
                }
            }

            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize * 2
            )

            val initialized = audioRecord?.state == AudioRecord.STATE_INITIALIZED

            if (initialized) {
                // 初始化音频处理器（AEC + NS）
                val sessionId = audioRecord?.audioSessionId ?: 0
                audioProcessor = AudioProcessor(sessionId)
                audioProcessor?.initialize()
                audioProcessor?.setAecEnabled(aecEnabled)
                audioProcessor?.setNsEnabled(nsEnabled)

                Log.d(TAG, audioProcessor?.getStatusInfo() ?: "")
            }

            return initialized

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize microphone", e)
            return false
        }
    }

    /**
     * 处理音频样本
     */
    private fun processAudioSamples() {
        Log.d(TAG, "Processing audio samples")

        val bufferSize = (BUFFER_INTERVAL_MS * SAMPLE_RATE / 1000).toInt()
        val buffer = ShortArray(bufferSize)

        while (isListening && !Thread.currentThread().isInterrupted) {
            try {
                val ret = audioRecord?.read(buffer, 0, buffer.size)

                if (ret != null && ret > 0) {
                    // 转换为浮点数组
                    val samples = FloatArray(ret) { buffer[it] / 32768.0f }

                    // 处理音频样本
                    stream?.let { s ->
                        // 检查 stream 是否有效
                        if (s.ptr == 0L) {
                            Log.e(TAG, "stream pointer is null, stopping")
                            isListening = false
                            return
                        }

                        // 检查样本数组是否有效
                        if (samples.isEmpty()) {
                            Log.w(TAG, "Empty samples array")
                            return@let
                        }

                        try {
                            s.acceptWaveform(samples, SAMPLE_RATE)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in acceptWaveform", e)
                            return@let
                        }

                        // 检查是否有识别结果
                        while (kws?.isReady(s) == true) {
                            kws?.decode(s)

                            val result = kws?.getResult(s)
                            val keyword = result?.keyword

                            if (!keyword.isNullOrBlank()) {
                                Log.d(TAG, "Keyword detected: $keyword")

                                // 重置流
                                kws?.reset(s)

                                // 回调通知
                                wakeupCallback?.invoke(keyword)
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                if (isListening) {
                    Log.e(TAG, "Error processing audio samples", e)
                }
            }
        }
    }

    /**
     * 检查是否正在监听
     */
    fun isListening(): Boolean = isListening

    /**
     * 检查服务是否已初始化
     */
    fun isInitialized(): Boolean = kws != null && stream != null

    /**
     * 启用/禁用 AEC（回声消除）
     */
    fun setAecEnabled(enabled: Boolean) {
        aecEnabled = enabled
        audioProcessor?.setAecEnabled(enabled)
    }

    /**
     * 启用/禁用 NS（噪声抑制）
     */
    fun setNsEnabled(enabled: Boolean) {
        nsEnabled = enabled
        audioProcessor?.setNsEnabled(enabled)
    }

    /**
     * 检查 AEC 是否可用（使用缓存值，无需等待 AudioProcessor 初始化）
     */
    fun isAecAvailable(): Boolean = aecAvailable

    /**
     * 检查 NS 是否可用（使用缓存值，无需等待 AudioProcessor 初始化）
     */
    fun isNsAvailable(): Boolean = nsAvailable

    /**
     * 检查 AEC 是否已启用
     */
    fun isAecEnabled(): Boolean = audioProcessor?.isAecEnabled() ?: aecEnabled

    /**
     * 检查 NS 是否已启用
     */
    fun isNsEnabled(): Boolean = audioProcessor?.isNsEnabled() ?: nsEnabled

    /**
     * 获取音频处理状态信息
     */
    fun getAudioProcessorStatus(): String = audioProcessor?.getStatusInfo() ?: "音频处理器未初始化"
}

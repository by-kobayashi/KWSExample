package com.dream.kwsexample.service

import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Log

/**
 * 音频处理器
 * 提供 AEC（回声消除）和 NS（噪声抑制）功能
 */
class AudioProcessor(private val audioSessionId: Int) {
    
    companion object {
        private const val TAG = "AudioProcessor"
    }
    
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    
    private var isAecEnabled = false
    private var isNsEnabled = false
    
    /**
     * 初始化音频处理器
     */
    fun initialize() {
        try {
            // 检查并初始化 AEC
            if (AcousticEchoCanceler.isAvailable()) {
                aec = AcousticEchoCanceler.create(audioSessionId)
                if (aec != null) {
                    aec?.enabled = true
                    isAecEnabled = true
                    Log.d(TAG, "AEC (回声消除) 已启用")
                } else {
                    Log.w(TAG, "AEC 创建失败")
                }
            } else {
                Log.w(TAG, "设备不支持 AEC")
            }
            
            // 检查并初始化 NS
            if (NoiseSuppressor.isAvailable()) {
                ns = NoiseSuppressor.create(audioSessionId)
                if (ns != null) {
                    ns?.enabled = true
                    isNsEnabled = true
                    Log.d(TAG, "NS (噪声抑制) 已启用")
                } else {
                    Log.w(TAG, "NS 创建失败")
                }
            } else {
                Log.w(TAG, "设备不支持 NS")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "初始化音频处理器失败", e)
        }
    }
    
    /**
     * 启用/禁用 AEC
     */
    fun setAecEnabled(enabled: Boolean) {
        try {
            aec?.enabled = enabled
            isAecEnabled = enabled
            Log.d(TAG, "AEC ${if (enabled) "已启用" else "已禁用"}")
        } catch (e: Exception) {
            Log.e(TAG, "设置 AEC 失败", e)
        }
    }
    
    /**
     * 启用/禁用 NS
     */
    fun setNsEnabled(enabled: Boolean) {
        try {
            ns?.enabled = enabled
            isNsEnabled = enabled
            Log.d(TAG, "NS ${if (enabled) "已启用" else "已禁用"}")
        } catch (e: Exception) {
            Log.e(TAG, "设置 NS 失败", e)
        }
    }
    
    /**
     * 检查 AEC 是否可用
     */
    fun isAecAvailable(): Boolean = AcousticEchoCanceler.isAvailable()
    
    /**
     * 检查 NS 是否可用
     */
    fun isNsAvailable(): Boolean = NoiseSuppressor.isAvailable()
    
    /**
     * 检查 AEC 是否已启用
     */
    fun isAecEnabled(): Boolean = isAecEnabled
    
    /**
     * 检查 NS 是否已启用
     */
    fun isNsEnabled(): Boolean = isNsEnabled
    
    /**
     * 获取状态信息
     */
    fun getStatusInfo(): String {
        val sb = StringBuilder()
        sb.append("音频处理状态:\n")
        sb.append("AEC (回声消除): ")
        if (isAecAvailable()) {
            sb.append(if (isAecEnabled) "✓ 已启用" else "○ 已禁用")
        } else {
            sb.append("✗ 不支持")
        }
        sb.append("\n")
        sb.append("NS (噪声抑制): ")
        if (isNsAvailable()) {
            sb.append(if (isNsEnabled) "✓ 已启用" else "○ 已禁用")
        } else {
            sb.append("✗ 不支持")
        }
        return sb.toString()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        try {
            aec?.release()
            aec = null
            ns?.release()
            ns = null
            Log.d(TAG, "音频处理器已释放")
        } catch (e: Exception) {
            Log.e(TAG, "释放音频处理器失败", e)
        }
    }
}

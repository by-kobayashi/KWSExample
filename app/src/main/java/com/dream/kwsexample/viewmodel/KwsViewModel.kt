package com.dream.kwsexample.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dream.kwsexample.model.DetectionRecord
import com.dream.kwsexample.service.KeywordWakeupService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * KWS ViewModel
 * 管理关键词唤醒服务的状态和业务逻辑
 */
class KwsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "KwsViewModel"
    }

    private val kwsService = KeywordWakeupService(application)

    // UI 状态
    private val _uiState = MutableStateFlow(KwsUiState())
    val uiState: StateFlow<KwsUiState> = _uiState.asStateFlow()

    init {
        initializeService()
    }

    /**
     * 初始化服务
     */
    private fun initializeService() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isInitializing = true,
                    errorMessage = null
                )

                kwsService.initialize()

                _uiState.value = _uiState.value.copy(
                    isInitialized = true,
                    isInitializing = false,
                    aecEnabled = kwsService.isAecEnabled(),
                    nsEnabled = kwsService.isNsEnabled()
                )

                Log.d(TAG, "KWS service initialized successfully")
                Log.d(TAG, "AEC available: ${kwsService.isAecAvailable()}, NS available: ${kwsService.isNsAvailable()}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize KWS service", e)
                _uiState.value = _uiState.value.copy(
                    isInitializing = false,
                    errorMessage = "初始化失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 开始监听
     */
    fun startListening() {
        if (!_uiState.value.isInitialized) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "服务未初始化"
            )
            return
        }

        try {
            kwsService.startListening { keyword ->
                onKeywordDetected(keyword)
            }

            _uiState.value = _uiState.value.copy(
                isListening = true,
                errorMessage = null
            )

            Log.d(TAG, "Started listening for keywords")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _uiState.value = _uiState.value.copy(
                errorMessage = "启动监听失败: ${e.message}"
            )
        }
    }

    /**
     * 停止监听
     */
    fun stopListening() {
        try {
            kwsService.stopListening()

            _uiState.value = _uiState.value.copy(
                isListening = false
            )

            Log.d(TAG, "Stopped listening for keywords")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        }
    }

    /**
     * 关键词检测回调
     */
    private fun onKeywordDetected(keyword: String) {
        Log.d(TAG, "Keyword detected: $keyword")

        val history = _uiState.value.detectionHistory.toMutableList()
        history.add(
            0, DetectionRecord(
                keyword = keyword,
                timestamp = System.currentTimeMillis()
            )
        )

        // 只保留最近20条记录
        if (history.size > 20) {
            history.removeAt(history.size - 1)
        }

        _uiState.value = _uiState.value.copy(
            lastDetectedKeyword = keyword,
            detectionCount = _uiState.value.detectionCount + 1,
            detectionHistory = history,
            showWakeupDialog = true  // 显示唤醒弹窗
        )
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 清除检测历史
     */
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(
            detectionHistory = emptyList(),
            detectionCount = 0,
            lastDetectedKeyword = null
        )
    }

    /**
     * 设置关键词
     */
    fun setKeywords(keywords: String) {
        try {
            kwsService.setKeywords(keywords)
            _uiState.value = _uiState.value.copy(
                currentKeyword = keywords,
                errorMessage = null
            )
            Log.d(TAG, "Keywords updated to: $keywords")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set keywords", e)
            _uiState.value = _uiState.value.copy(
                errorMessage = "设置关键词失败: ${e.message}"
            )
        }
    }

    /**
     * 设置检测阈值
     */
    fun setThreshold(threshold: Float) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isInitializing = true,
                    errorMessage = null
                )

                kwsService.setThreshold(threshold)
                _uiState.value = _uiState.value.copy(
                    threshold = threshold,
                    isInitializing = false
                )

                Log.d(TAG, "Threshold updated to: $threshold")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set threshold", e)
                _uiState.value = _uiState.value.copy(
                    isInitializing = false,
                    errorMessage = "设置阈值失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 关闭唤醒弹窗
     */
    fun dismissWakeupDialog() {
        _uiState.value = _uiState.value.copy(
            showWakeupDialog = false
        )
    }
    
    /**
     * 启用/禁用 AEC（回声消除）
     */
    fun setAecEnabled(enabled: Boolean) {
        kwsService.setAecEnabled(enabled)
        _uiState.value = _uiState.value.copy(
            aecEnabled = enabled
        )
        Log.d(TAG, "AEC ${if (enabled) "已启用" else "已禁用"}")
    }
    
    /**
     * 启用/禁用 NS（噪声抑制）
     */
    fun setNsEnabled(enabled: Boolean) {
        kwsService.setNsEnabled(enabled)
        _uiState.value = _uiState.value.copy(
            nsEnabled = enabled
        )
        Log.d(TAG, "NS ${if (enabled) "已启用" else "已禁用"}")
    }
    
    /**
     * 检查 AEC 是否可用
     */
    fun isAecAvailable(): Boolean = kwsService.isAecAvailable()
    
    /**
     * 检查 NS 是否可用
     */
    fun isNsAvailable(): Boolean = kwsService.isNsAvailable()

    override fun onCleared() {
        super.onCleared()
        kwsService.release()
    }
}

/**
 * UI 状态数据类
 */
data class KwsUiState(
    val isInitialized: Boolean = false,
    val isInitializing: Boolean = false,
    val isListening: Boolean = false,
    val currentKeyword: String = "x iǎo ān x iǎo ān",
    val threshold: Float = 0.25f,
    val lastDetectedKeyword: String? = null,
    val detectionCount: Int = 0,
    val detectionHistory: List<DetectionRecord> = emptyList(),
    val errorMessage: String? = null,
    val showWakeupDialog: Boolean = false,
    val aecEnabled: Boolean = true,
    val nsEnabled: Boolean = true
)



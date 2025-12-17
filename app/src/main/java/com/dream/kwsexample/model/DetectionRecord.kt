package com.dream.kwsexample.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * 检测记录数据类
 */
data class DetectionRecord(
    val keyword: String,
    val timestamp: Long,
    val formattedTime: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
)

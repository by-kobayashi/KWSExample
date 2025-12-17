package com.dream.kwsexample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 预设唤醒词列表
 */
private val PRESET_KEYWORDS = listOf(
    "小安小安" to "x iǎo ān x iǎo ān",
    "你好世界" to "n ǐ h ǎo sh ì j iè",
    "打开灯光" to "d ǎ k āi d ēng g uāng",
    "关闭电视" to "g uān b ì d iàn sh ì",
    "播放音乐" to "b ō f àng y īn y uè"
)

/**
 * 关键词配置对话框
 * 用于自定义唤醒关键词和调整检测阈值
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordConfigDialog(
    currentKeyword: String,
    currentThreshold: Float = 0.25f,
    aecEnabled: Boolean = true,
    nsEnabled: Boolean = true,
    aecAvailable: Boolean = true,
    nsAvailable: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String, Float, Boolean, Boolean) -> Unit
) {
    var keyword by remember { mutableStateOf(currentKeyword) }
    var threshold by remember { mutableStateOf(currentThreshold) }
    var aec by remember { mutableStateOf(aecEnabled) }
    var ns by remember { mutableStateOf(nsEnabled) }
    var showPresets by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("配置唤醒设置")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 唤醒词输入
                Text(
                    text = "唤醒词",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("拼音格式") },
                    placeholder = { Text("例如: x iǎo ān x iǎo ān") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
                
                // 预设唤醒词按钮
                OutlinedButton(
                    onClick = { showPresets = !showPresets },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showPresets) "隐藏预设" else "选择预设唤醒词")
                }
                
                // 预设唤醒词列表
                if (showPresets) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            PRESET_KEYWORDS.forEach { (name, pinyin) ->
                                ListItem(
                                    headlineContent = { Text(name) },
                                    supportingContent = { Text(pinyin, style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.clickable {
                                        keyword = pinyin
                                        showPresets = false
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                                if (PRESET_KEYWORDS.last() != (name to pinyin)) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                
                // 检测阈值调节
                Text(
                    text = "检测灵敏度",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "阈值: ${String.format("%.2f", threshold)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = when {
                                threshold < 0.15f -> "非常敏感"
                                threshold < 0.20f -> "敏感"
                                threshold < 0.30f -> "正常"
                                else -> "不敏感"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Slider(
                        value = threshold,
                        onValueChange = { threshold = it },
                        valueRange = 0.05f..0.50f,
                        steps = 17,  // 0.05 到 0.50，步长 0.025
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "敏感",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "不敏感",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // 音频处理设置
                Text(
                    text = "音频处理",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // AEC 开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AEC (回声消除)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (aecAvailable) MaterialTheme.colorScheme.onSurface 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Text(
                            text = if (aecAvailable) "消除扬声器回声" else "设备不支持",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = aec,
                        onCheckedChange = { aec = it },
                        enabled = aecAvailable
                    )
                }
                
                // NS 开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NS (噪声抑制)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (nsAvailable) MaterialTheme.colorScheme.onSurface 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Text(
                            text = if (nsAvailable) "抑制环境噪声" else "设备不支持",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = ns,
                        onCheckedChange = { ns = it },
                        enabled = nsAvailable
                    )
                }
                
                // 提示信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "提示",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "• 使用拼音格式，空格分隔音节\n• 阈值越低越敏感，但误触率越高\n• AEC 和 NS 可提高识别准确率",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (keyword.isNotBlank()) {
                        onConfirm(keyword, threshold, aec, ns)
                        onDismiss()
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}


/**
 * 关键词配置对话框预览
 */
@Preview(showBackground = true)
@Composable
fun KeywordConfigDialogPreview() {
    MaterialTheme {
        KeywordConfigDialog(
            currentKeyword = "x iǎo ān x iǎo ān",
            currentThreshold = 0.25f,
            aecEnabled = true,
            nsEnabled = true,
            aecAvailable = true,
            nsAvailable = true,
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "配置对话框 - 高灵敏度")
@Composable
fun KeywordConfigDialogHighSensitivityPreview() {
    MaterialTheme {
        KeywordConfigDialog(
            currentKeyword = "n ǐ h ǎo sh ì j iè",
            currentThreshold = 0.15f,
            aecEnabled = true,
            nsEnabled = false,
            aecAvailable = true,
            nsAvailable = true,
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}

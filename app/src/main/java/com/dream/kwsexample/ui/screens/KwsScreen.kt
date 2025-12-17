package com.dream.kwsexample.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dream.kwsexample.model.DetectionRecord
import com.dream.kwsexample.ui.components.KeywordConfigDialog
import com.dream.kwsexample.ui.components.WakeupDialog
import com.dream.kwsexample.viewmodel.KwsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * KWS 主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KwsScreen(
    viewModel: KwsViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showKeywordDialog by remember { mutableStateOf(false) }

    // 权限请求
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // 关键词配置对话框
    if (showKeywordDialog) {
        KeywordConfigDialog(
            currentKeyword = uiState.currentKeyword,
            currentThreshold = uiState.threshold,
            aecEnabled = uiState.aecEnabled,
            nsEnabled = uiState.nsEnabled,
            aecAvailable = viewModel.isAecAvailable(),
            nsAvailable = viewModel.isNsAvailable(),
            onDismiss = { showKeywordDialog = false },
            onConfirm = { keyword, threshold, aec, ns ->
                viewModel.setKeywords(keyword)
                viewModel.setThreshold(threshold)
                viewModel.setAecEnabled(aec)
                viewModel.setNsEnabled(ns)
            }
        )
    }

    // 唤醒弹窗
    if (uiState.showWakeupDialog && uiState.lastDetectedKeyword != null) {
        WakeupDialog(
            keyword = uiState.lastDetectedKeyword!!,
            onDismiss = { viewModel.dismissWakeupDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sherpa-ONNX JNI 唤醒") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showKeywordDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置关键词")
                    }
                    if (uiState.detectionHistory.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Default.Delete, contentDescription = "清除历史")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 错误提示
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                ErrorCard(
                    message = uiState.errorMessage ?: "",
                    onDismiss = { viewModel.clearError() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 状态卡片
            StatusCard(
                isInitialized = uiState.isInitialized,
                isInitializing = uiState.isInitializing,
                isListening = uiState.isListening,
                detectionCount = uiState.detectionCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 控制按钮
            ControlButton(
                isListening = uiState.isListening,
                isInitialized = uiState.isInitialized,
                hasPermission = hasPermission,
                onStartClick = { viewModel.startListening() },
                onStopClick = { viewModel.stopListening() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 最后检测到的关键词
            LastDetectionCard(keyword = uiState.lastDetectedKeyword)

            Spacer(modifier = Modifier.height(16.dp))

            // 检测历史
            DetectionHistoryList(
                history = uiState.detectionHistory,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 错误提示卡片
 */
@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * 状态卡片
 */
@Composable
fun StatusCard(
    isInitialized: Boolean,
    isInitializing: Boolean,
    isListening: Boolean,
    detectionCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    label = "服务状态",
                    value = when {
                        isInitializing -> "初始化中..."
                        isInitialized -> "已就绪"
                        else -> "未初始化"
                    },
                    icon = when {
                        isInitializing -> Icons.Default.Refresh
                        isInitialized -> Icons.Default.CheckCircle
                        else -> Icons.Default.Info
                    }
                )

                StatusItem(
                    label = "监听状态",
                    value = if (isListening) "监听中" else "已停止",
                    icon = if (isListening) Icons.Default.Mic else Icons.Default.MicOff
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "检测次数: $detectionCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * 状态项
 */
@Composable
fun StatusItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * 控制按钮
 */
@Composable
fun ControlButton(
    isListening: Boolean,
    isInitialized: Boolean,
    hasPermission: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // 背景圆圈动画
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 主按钮
        FloatingActionButton(
            onClick = {
                if (isInitialized && hasPermission) {
                    if (isListening) {
                        onStopClick()
                    } else {
                        onStartClick()
                    }
                }
            },
            modifier = Modifier.size(120.dp),
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.error
            } else if (isInitialized && hasPermission) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "停止" else "开始",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isListening) "停止" else "开始",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * 最后检测卡片
 */
@Composable
fun LastDetectionCard(keyword: String?) {
    AnimatedVisibility(
        visible = keyword != null,
        enter = slideInVertically() + expandVertically() + fadeIn(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "最后检测",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = keyword ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * 检测历史列表
 */
@Composable
fun DetectionHistoryList(
    history: List<DetectionRecord>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.HistoryToggleOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无检测记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = "检测历史",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { record ->
                    DetectionHistoryItem(record)
                }
            }
        }
    }
}

/**
 * 检测历史项
 */
@Composable
fun DetectionHistoryItem(record: DetectionRecord) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.VoiceChat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = record.keyword,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = dateFormat.format(Date(record.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}


// ==================== 预览 ====================

/**
 * 主界面预览 - 初始状态
 */
@Preview(showBackground = true, name = "主界面 - 初始状态")
@Composable
fun KwsScreenInitialPreview() {
    MaterialTheme {
        Surface {
            // 模拟初始状态
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusCard(
                    isInitialized = false,
                    isInitializing = true,
                    isListening = false,
                    detectionCount = 0
                )
                Spacer(modifier = Modifier.height(24.dp))
                ControlButton(
                    isListening = false,
                    isInitialized = false,
                    hasPermission = true,
                    onStartClick = {},
                    onStopClick = {}
                )
            }
        }
    }
}

/**
 * 主界面预览 - 监听中
 */
@Preview(showBackground = true, name = "主界面 - 监听中")
@Composable
fun KwsScreenListeningPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusCard(
                    isInitialized = true,
                    isInitializing = false,
                    isListening = true,
                    detectionCount = 5
                )
                Spacer(modifier = Modifier.height(24.dp))
                ControlButton(
                    isListening = true,
                    isInitialized = true,
                    hasPermission = true,
                    onStartClick = {},
                    onStopClick = {}
                )
                Spacer(modifier = Modifier.height(24.dp))
                LastDetectionCard(keyword = "小安小安")
            }
        }
    }
}

/**
 * 状态卡片预览
 */
@Preview(showBackground = true, name = "状态卡片 - 已就绪")
@Composable
fun StatusCardReadyPreview() {
    MaterialTheme {
        StatusCard(
            isInitialized = true,
            isInitializing = false,
            isListening = false,
            detectionCount = 0
        )
    }
}

@Preview(showBackground = true, name = "状态卡片 - 监听中")
@Composable
fun StatusCardListeningPreview() {
    MaterialTheme {
        StatusCard(
            isInitialized = true,
            isInitializing = false,
            isListening = true,
            detectionCount = 10
        )
    }
}

/**
 * 控制按钮预览
 */
@Preview(showBackground = true, name = "控制按钮 - 开始")
@Composable
fun ControlButtonStartPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            ControlButton(
                isListening = false,
                isInitialized = true,
                hasPermission = true,
                onStartClick = {},
                onStopClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "控制按钮 - 停止")
@Composable
fun ControlButtonStopPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            ControlButton(
                isListening = true,
                isInitialized = true,
                hasPermission = true,
                onStartClick = {},
                onStopClick = {}
            )
        }
    }
}

/**
 * 最后检测卡片预览
 */
@Preview(showBackground = true, name = "最后检测卡片")
@Composable
fun LastDetectionCardPreview() {
    MaterialTheme {
        LastDetectionCard(keyword = "小安小安")
    }
}

/**
 * 检测历史列表预览
 */
@Preview(showBackground = true, name = "检测历史 - 有数据")
@Composable
fun DetectionHistoryListPreview() {
    MaterialTheme {
        DetectionHistoryList(
            history = listOf(
                DetectionRecord("小安小安", System.currentTimeMillis()),
                DetectionRecord("你好世界", System.currentTimeMillis() - 10000),
                DetectionRecord("打开灯光", System.currentTimeMillis() - 20000)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "检测历史 - 空状态")
@Composable
fun DetectionHistoryListEmptyPreview() {
    MaterialTheme {
        DetectionHistoryList(
            history = emptyList(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 错误卡片预览
 */
@Preview(showBackground = true, name = "错误提示")
@Composable
fun ErrorCardPreview() {
    MaterialTheme {
        ErrorCard(
            message = "初始化失败: 模型文件未找到",
            onDismiss = {}
        )
    }
}

# 📖 KWS 离线唤醒 App - 完整使用指南

## 目录

- [项目概述](#项目概述)
- [界面说明](#界面说明)
- [功能特性](#功能特性)
- [使用方法](#使用方法)
- [自定义配置](#自定义配置)
- [性能优化](#性能优化)
- [开发指南](#开发指南)

## 项目概述

### 项目结构

```
app/src/main/java/com/dream/kwsexample/
├── MainActivity.kt                    # 主活动
├── viewmodel/
│   └── KwsViewModel.kt               # ViewModel，管理状态
├── ui/
│   ├── screens/
│   │   └── KwsScreen.kt             # 主界面 UI
│   ├── components/
│   │   └── KeywordConfigDialog.kt   # 配置对话框
│   └── theme/                        # 主题配置
└── service/
    ├── KeywordWakeupService.kt       # 唤醒服务
    ├── AudioUtil.kt                  # 音频工具（单例）
    └── kws/                          # KWS 核心组件
```

### 技术架构

- **MVVM 架构**: ViewModel + StateFlow
- **Jetpack Compose**: 声明式 UI
- **单例模式**: AudioUtil 使用安全的单例
- **协程**: 异步处理和状态管理

## 界面说明

### 顶部应用栏

- **标题**: "KWS 离线唤醒"
- **设置按钮** (⚙️): 配置自定义唤醒词
- **清除按钮** (🗑️): 清除检测历史

### 状态卡片

显示三个关键信息：

1. **服务状态**
   - 初始化中... (加载模型时)
   - 已就绪 (可以使用)
   - 未初始化 (出错时)

2. **监听状态**
   - 监听中 (正在检测)
   - 已停止 (待机状态)

3. **检测次数**
   - 实时统计检测到的关键词次数

### 控制按钮

- **大型圆形按钮** (120dp)
  - 蓝色 + 麦克风图标 = 开始监听
  - 红色 + 停止图标 = 停止监听
  - 监听时有脉冲动画效果

### 检测历史

- 显示所有检测记录
- 每条记录包含关键词和时间戳
- 最多保留 20 条记录
- 支持滚动查看

## 功能特性

### 1. 离线语音唤醒

基于 SherpaOnnx 引擎：
- 16kHz 采样率
- 单声道音频
- 实时流式处理
- 低延迟检测（<100ms）

### 2. 动画效果

**脉冲动画**
```kotlin
// 监听时的扩散效果
- 1 秒循环
- 1.0x → 1.1x 缩放
- 径向渐变
```

**滑入动画**
```kotlin
// 卡片显示动画
- 垂直滑入
- 展开效果
- 淡入效果
```

### 3. 状态管理

使用 StateFlow 实现响应式更新：

```kotlin
data class KwsUiState(
    val isInitialized: Boolean,
    val isListening: Boolean,
    val lastDetectedKeyword: String?,
    val detectionCount: Int,
    val detectionHistory: List<DetectionRecord>
)
```

### 4. 权限管理

- 自动检测权限状态
- 友好的权限请求
- 权限拒绝处理

## 使用方法

### 基本使用

1. **启动应用**
   ```
   等待服务初始化（约 2-3 秒）
   状态卡片显示"已就绪"
   ```

2. **授予权限**
   ```
   首次使用会请求录音权限
   点击"允许"继续
   ```

3. **开始监听**
   ```
   点击中央麦克风按钮
   按钮变红，出现脉冲动画
   ```

4. **说出唤醒词**
   ```
   清晰说出"小安小安"
   检测到后会显示在界面上
   ```

5. **停止监听**
   ```
   点击停止按钮
   按钮变蓝，动画停止
   ```

### 查看历史

- 向下滚动查看所有检测记录
- 每条记录显示关键词和时间
- 点击顶部删除按钮清除历史

## 自定义配置

### 方法 1: 界面配置（推荐）

1. 点击顶部设置按钮 (⚙️)
2. 选择预设唤醒词或输入自定义拼音
3. 调整检测灵敏度滑块
4. 点击"确定"保存

**预设唤醒词**:
- 小安小安: `x iǎo ān x iǎo ān`
- 你好世界: `n ǐ h ǎo sh ì j iè`
- 打开灯光: `d ǎ k āi d ēng g uāng`
- 关闭电视: `g uān b ì d iàn sh ì`
- 播放音乐: `b ō f àng y īn y uè`

**灵敏度设置**:
- **0.05-0.15**: 非常敏感（容易误触）
- **0.15-0.20**: 敏感（推荐安静环境）
- **0.20-0.30**: 正常（默认 0.25，推荐）
- **0.30-0.50**: 不敏感（需要清晰发音）

### 方法 2: 代码配置

编辑 `KeywordWakeupService.kt`:

```kotlin
private val defaultKeywords = "n ǐ h ǎo sh ì j iè"

val config = KeywordSpotterConfig(
    keywordsThreshold = 0.15f,  // 调整阈值
    keywordsScore = 1.5f
)
```

## 性能优化

### 1. 内存优化

**限制历史记录**
```kotlin
// 在 KwsViewModel 中
if (history.size > 20) {
    history.removeAt(history.size - 1)
}
```

**及时释放资源**
```kotlin
override fun onCleared() {
    super.onCleared()
    kwsService.release()
}
```

### 2. CPU 优化

**调整缓冲区大小**
```kotlin
// 在 KeywordWakeupService 中
private const val BUFFER_INTERVAL_MS = 200L  // 增大减少 CPU 占用
```

**使用量化模型**
```kotlin
// 使用 int8 量化模型
encoder = "$modelDir/encoder.int8.onnx"
```

### 3. 电池优化

- 不使用时停止监听
- 在 onPause 时暂停服务
- 避免后台运行

## 开发指南

### AudioUtil 单例模式

**使用方法**:
```kotlin
// 获取单例实例
val audioUtil = AudioUtil.getInstance(context)

// 使用实例
audioUtil.initRecorder()
audioUtil.startRecording()
```

**特性**:
- 线程安全的双重检查锁定
- 使用 Application Context 避免泄漏
- 提供 destroyInstance() 清理方法

**注意事项**:
```kotlin
// ✅ 推荐 - 自动转换为 applicationContext
AudioUtil.getInstance(activity)
AudioUtil.getInstance(service)

// 在应用退出时清理
AudioUtil.destroyInstance()
```

### 添加新功能

**1. 语音反馈**
```kotlin
private fun onKeywordDetected(keyword: String) {
    playBeepSound()  // 播放提示音
    updateUI(keyword)
}
```

**2. 统计功能**
```kotlin
data class Statistics(
    val totalDetections: Int,
    val todayDetections: Int,
    val averagePerDay: Float
)
```

**3. 多语言支持**
```kotlin
// 切换到英文模型
val config = getKwsModelConfig(type = 1)  // 1 = 英文模型
```

### 调试技巧

**查看日志**:
```bash
# 过滤 KWS 相关日志
adb logcat | grep -E "KwsViewModel|KeywordWakeupService"

# 查看崩溃日志
adb logcat | grep -E "FATAL|AndroidRuntime"
```

**常用日志标签**:
- `KwsViewModel`: ViewModel 状态
- `KeywordWakeupService`: 服务和检测
- `AudioUtil`: 音频处理

### 测试建议

1. **在真机上测试** - 模拟器性能可能不足
2. **测试不同环境** - 安静/嘈杂环境
3. **测试不同发音** - 清晰/模糊发音
4. **长时间运行测试** - 检查稳定性

## 最佳实践

### 1. 权限处理

```kotlin
// 在 Activity 中
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        viewModel.startListening()
    } else {
        showPermissionDeniedDialog()
    }
}
```

### 2. 生命周期管理

```kotlin
// 在 Activity 中
override fun onPause() {
    super.onPause()
    viewModel.stopListening()  // 暂停监听
}

override fun onResume() {
    super.onResume()
    // 根据需要恢复监听
}
```

### 3. 错误处理

```kotlin
try {
    kwsService.initialize()
} catch (e: SecurityException) {
    showError("权限被拒绝")
} catch (e: IllegalStateException) {
    showError("初始化失败: ${e.message}")
} catch (e: Exception) {
    showError("未知错误")
}
```

## 扩展功能

### 计划中的功能

- [ ] 多语言支持
- [ ] 语音反馈
- [ ] 统计图表
- [ ] 云端同步
- [ ] 自定义主题
- [ ] 小部件支持

### 如何贡献

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 发起 Pull Request

---

**最后更新**: 2025-11-10

# 🔧 故障排除指南

## 常见问题和解决方案

### 1. 应用崩溃 - JNI 错误

#### 症状
```
Fatal signal 11 (SIGSEGV)
libsherpa-onnx-jni.so
OnlineStream.acceptWaveform
```

#### 原因
- Stream 指针无效或为 null
- 传递给 native 方法的参数无效
- 模型文件损坏或缺失
- JNI 库不兼容

#### 解决方案

**1. 检查模型文件**
```bash
# 确保 assets 目录下有完整的模型文件
ls -la app/src/main/assets/sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01/
```

应该包含:
- encoder-*.onnx
- decoder-*.onnx
- joiner-*.onnx
- tokens.txt
- keywords.txt

**2. 检查 JNI 库**
```bash
# 确保有对应架构的 JNI 库
ls -la app/libs/jniLibs/
```

应该包含:
- arm64-v8a/libsherpa-onnx-jni.so
- armeabi-v7a/libsherpa-onnx-jni.so
- x86/libsherpa-onnx-jni.so
- x86_64/libsherpa-onnx-jni.so

**3. 添加更多日志**

在 `KeywordWakeupService.kt` 中:
```kotlin
Log.d(TAG, "Stream ptr: ${stream?.ptr}")
Log.d(TAG, "Samples size: ${samples.size}")
Log.d(TAG, "Sample rate: $SAMPLE_RATE")
```

**4. 清理并重新构建**
```bash
./gradlew clean
./gradlew build
./gradlew installDebug
```

### 2. 初始化失败

#### 症状
```
Failed to initialize keyword spotter
Failed to get model config
```

#### 解决方案

**1. 检查模型类型**
```kotlin
// 确保使用正确的模型类型
val config = getKwsModelConfig(type = 0) // 0 = 中文模型
```

**2. 检查 assets 路径**
```kotlin
// 确保路径正确
val modelDir = "sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01"
```

**3. 检查权限**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### 3. 无法检测关键词

#### 症状
- 应用运行正常
- 但说出关键词没有反应

#### 解决方案

**1. 调整检测灵敏度（推荐）**

通过界面调整:
1. 点击顶部设置按钮 (⚙️)
2. 拖动"检测灵敏度"滑块向左
3. 建议设置为 0.15-0.20（敏感）
4. 点击确定保存

灵敏度参考:
- 0.05-0.15: 非常敏感（容易误触）
- 0.15-0.20: 敏感（推荐安静环境）
- 0.20-0.30: 正常（默认 0.25）
- 0.30-0.50: 不敏感

**2. 检查关键词格式**
```kotlin
// 正确的拼音格式
val keywords = "x iǎo ān x iǎo ān"  // 小安小安

// 错误的格式
val keywords = "小安小安"  // ❌ 不要用汉字
val keywords = "xiǎo ān xiǎo ān"  // ❌ 不要连写
```

**3. 检查麦克风**
```kotlin
// 测试麦克风是否工作
val ret = audioRecord?.read(buffer, 0, buffer.size)
Log.d(TAG, "Audio read: $ret bytes")
```

**4. 检查音频格式**
```kotlin
// 确保音频格式正确
SAMPLE_RATE = 16000  // 必须是 16kHz
CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO  // 单声道
AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT  // 16位
```

### 4. 内存泄漏

#### 症状
- 应用运行一段时间后变慢
- 内存占用持续增长
- Lint 警告: "Do not place Android context classes in static fields"

#### 解决方案

**1. 避免静态持有 Activity Context**
```kotlin
// ❌ 错误 - 会导致内存泄漏
companion object {
    private var INSTANCE: AudioUtil? = null
    fun getInstance(context: Context): AudioUtil {
        return INSTANCE ?: AudioUtil(context)  // 直接持有传入的 context
    }
}

// ✅ 正确 - 使用 Application Context
class AudioUtil private constructor(context: Context) {
    private val appContext: Context = context.applicationContext
    
    companion object {
        @Volatile
        private var INSTANCE: AudioUtil? = null
        
        fun getInstance(context: Context): AudioUtil {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioUtil(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }
    }
}
```

**2. 确保释放资源**
```kotlin
override fun onCleared() {
    super.onCleared()
    kwsService.release()  // 必须调用
}
```

**3. 停止监听时释放**
```kotlin
fun stopListening() {
    isListening = false
    audioRecord?.stop()
    audioRecord?.release()
    audioRecord = null
}
```

**4. 清理历史记录**
```kotlin
// 限制历史记录数量
if (history.size > 20) {
    history.removeAt(history.size - 1)
}
```

**5. 使用 Application Context**
```kotlin
// 如果必须长期持有 Context，使用 applicationContext
class MyService(context: Context) {
    private val appContext = context.applicationContext
}
```

### 5. 权限被拒绝

#### 症状
```
RECORD_AUDIO permission not granted
```

#### 解决方案

**1. 检查权限声明**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

**2. 请求运行时权限**
```kotlin
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
```

**3. 手动授予权限**
```bash
adb shell pm grant com.dream.kwsexample android.permission.RECORD_AUDIO
```

### 6. 性能问题

#### 症状
- 应用卡顿
- CPU 占用高
- 电池消耗快

#### 解决方案

**1. 调整缓冲区大小**
```kotlin
// 增大缓冲区减少 CPU 占用
private const val BUFFER_INTERVAL_MS = 200L  // 从 100ms 增加到 200ms
```

**2. 使用更小的模型**
```kotlin
// 使用 int8 量化模型
encoder = "$modelDir/encoder.int8.onnx"
```

**3. 不使用时停止监听**
```kotlin
// 在 onPause 时停止
override fun onPause() {
    super.onPause()
    viewModel.stopListening()
}
```

### 7. 模型加载失败

#### 症状
```
Failed to load model
Cannot find model file
```

#### 解决方案

**1. 检查文件是否存在**
```bash
# 使用 adb 检查
adb shell run-as com.dream.kwsexample ls -la /data/data/com.dream.kwsexample/files/
```

**2. 重新添加模型文件**
```bash
# 确保模型文件在正确位置
app/src/main/assets/sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01/
```

**3. 检查文件大小**
```bash
# 确保文件完整
ls -lh app/src/main/assets/sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01/*.onnx
```

## 调试技巧

### 1. 查看日志
```bash
# 过滤应用日志
adb logcat | grep "com.dream.kwsexample"

# 过滤特定标签
adb logcat | grep -E "KwsViewModel|KeywordWakeupService"

# 查看崩溃日志
adb logcat | grep -E "FATAL|AndroidRuntime"
```

### 2. 使用 Android Studio Profiler
- CPU Profiler: 查看 CPU 占用
- Memory Profiler: 查看内存使用
- Network Profiler: 查看网络请求（虽然是离线应用）

### 3. 添加断点调试
在以下位置添加断点:
- `KeywordWakeupService.initialize()`
- `KeywordWakeupService.processAudioSamples()`
- `KwsViewModel.startListening()`

### 4. 检查设备信息
```bash
# 查看设备架构
adb shell getprop ro.product.cpu.abi

# 查看 Android 版本
adb shell getprop ro.build.version.release

# 查看可用内存
adb shell cat /proc/meminfo
```

## 获取帮助

如果以上方法都无法解决问题:

1. **查看完整日志**
   ```bash
   adb logcat > logcat.txt
   ```

2. **检查堆栈跟踪**
   - 找到 `FATAL EXCEPTION` 行
   - 查看完整的堆栈跟踪
   - 记录崩溃位置

3. **提供详细信息**
   - Android 版本
   - 设备型号
   - 应用版本
   - 复现步骤
   - 完整日志

4. **查看相关文档**
   - [SherpaOnnx 官方文档](https://k2-fsa.github.io/sherpa/onnx/)
   - [项目 README](../README.md)
   - [使用指南](USAGE_GUIDE.md)

---

**最后更新**: 2025-11-10

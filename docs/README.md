# 📚 KWS 离线唤醒 App - 文档

> 基于 Jetpack Compose 的 Android 离线关键词唤醒应用

## 🚀 快速开始

### 构建和运行

```bash
# 构建项目
./gradlew build

# 安装到设备
./gradlew installDebug
```

### 使用步骤

1. **打开应用** → 自动初始化 KWS 服务
2. **授予权限** → 允许录音权限
3. **点击麦克风按钮** → 开始监听
4. **说出"小安小安"** → 查看检测结果

## ✨ 主要特性

- 🎤 **离线语音唤醒** - 无需网络，本地实时检测
- 🎨 **Material Design 3** - 现代化 UI 设计
- 🔄 **实时监听** - 低延迟音频处理
- 📊 **检测历史** - 自动记录所有检测结果
- ⚙️ **自定义唤醒词** - 支持配置个性化关键词
- 🎭 **流畅动画** - 丰富的视觉反馈效果

## 📖 文档导航

### 核心文档

1. **[完整使用指南](GUIDE.md)** - 详细的功能说明、使用方法和最佳实践
2. **[故障排除](TROUBLESHOOTING.md)** - 常见问题和解决方案

### 快速参考

| 需求 | 查看 |
|------|------|
| 快速上手 | 本文档 |
| 详细功能 | [GUIDE.md](GUIDE.md) |
| 解决问题 | [TROUBLESHOOTING.md](TROUBLESHOOTING.md) |
| 自定义唤醒词 | [GUIDE.md#自定义唤醒词](GUIDE.md#自定义唤醒词) |
| 性能优化 | [GUIDE.md#性能优化](GUIDE.md#性能优化) |

## 🎯 预设唤醒词

应用内置 5 个预设唤醒词，可在设置中一键选择：

| 中文 | 拼音格式 |
|------|----------|
| 小安小安 | x iǎo ān x iǎo ān |
| 你好世界 | n ǐ h ǎo sh ì j iè |
| 打开灯光 | d ǎ k āi d ēng g uāng |
| 关闭电视 | g uān b ì d iàn sh ì |
| 播放音乐 | b ō f àng y īn y uè |

## 🛠️ 技术栈

- **Kotlin** - 编程语言
- **Jetpack Compose** - 现代 UI 框架
- **Material Design 3** - UI 设计规范
- **ViewModel** - 状态管理
- **Coroutines & Flow** - 异步处理
- **SherpaOnnx** - 离线语音识别引擎

## 📋 系统要求

- Android 8.0 (API 26) 及以上
- 录音权限
- 麦克风硬件

## 🔗 相关链接

- [返回项目主页](../README.md)
- [查看源代码](../app/src/main/java/com/dream/kwsexample/)
- [SherpaOnnx 官方文档](https://k2-fsa.github.io/sherpa/onnx/)

---

**最后更新**: 2025-11-10

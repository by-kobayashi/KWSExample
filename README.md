# KWS 离线唤醒 App

> 基于 Jetpack Compose 的 Android 离线关键词唤醒应用示例

[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-2024.09.00-orange.svg)](https://developer.android.com/jetpack/compose)

## 🎯 项目简介

这是一个功能完整的 KWS（Keyword Spotting）离线语音唤醒 Android 应用，使用 Jetpack Compose 构建现代化 UI，基于 Sherpa-ONNX 引擎实现离线语音识别。

## ✨ 主要特性

- 🎤 **离线语音唤醒** - 无需网络，本地实时检测
- 🎨 **Material Design 3** - 现代化 UI 设计
- 🔄 **实时监听** - 低延迟音频处理
- 🔇 **AEC + NS** - 回声消除和噪声抑制
- 📊 **检测历史** - 自动记录所有检测结果
- ⚙️ **自定义唤醒词** - 支持配置个性化关键词
- 🎭 **流畅动画** - 丰富的视觉反馈效果
- 🔐 **权限管理** - 智能的运行时权限处理

## 🚀 快速开始

### 构建和运行

```bash
# 构建项目
./gradlew build

# 安装到设备
./gradlew installDebug
```

### 使用步骤

1. 打开应用，等待服务初始化
2. 授予录音权限
3. 点击中央麦克风按钮开始监听
4. 说出唤醒词"小安小安"
5. 查看检测结果和历史记录

## 📱 界面预览

应用包含以下主要界面：

- **顶部栏**: 标题 + 设置按钮 + 清除按钮
- **状态卡片**: 显示服务状态、监听状态、检测次数
- **控制按钮**: 大型圆形按钮，带脉冲动画
- **检测卡片**: 显示最后检测到的关键词
- **历史列表**: 滚动显示所有检测记录

## 🏗️ 项目结构

```
app/src/main/java/com/dream/kwsexample/
├── MainActivity.kt                    # 主活动
├── viewmodel/
│   └── KwsViewModel.kt               # ViewModel
├── ui/
│   ├── components/
│   │   └── KeywordConfigDialog.kt   # 配置对话框
│   ├── screens/
│   │   └── KwsScreen.kt             # 主界面
│   └── theme/                        # 主题配置
└── service/                          # 服务层
    ├── KeywordWakeupService.kt       # 唤醒服务
    ├── AudioUtil.kt                  # 音频工具
    └── kws/                          # KWS 核心
```

## 📚 文档

- **[完整使用指南](docs/GUIDE.md)** - 详细的功能说明和使用方法
- **[故障排除](docs/TROUBLESHOOTING.md)** - 常见问题和解决方案

## 🛠️ 技术栈

- **Kotlin** - 编程语言
- **Jetpack Compose** - 现代 UI 框架
- **Material Design 3** - UI 设计规范
- **ViewModel** - 状态管理
- **Coroutines & Flow** - 异步处理
- **Sherpa-ONNX** - 离线语音识别引擎

## 📋 系统要求

- Android 8.0 (API 26) 及以上
- 录音权限
- 麦克风硬件

## 🎯 核心功能

### 1. 离线唤醒
- 基于 Sherpa-ONNX 引擎
- 16kHz 采样率
- 实时流式处理
- 低延迟检测
- AEC（回声消除）
- NS（噪声抑制）

### 2. UI 界面
- Material Design 3 设计
- 响应式布局
- 流畅动画效果
- 深色/浅色主题

### 3. 状态管理
- MVVM 架构
- StateFlow 响应式更新
- 生命周期感知

### 4. 数据管理
- 检测历史记录
- 自动保存
- 一键清除

## ⚙️ 自定义配置

### 修改唤醒词

**方法 1: 界面设置**
1. 点击顶部设置按钮
2. 输入拼音格式的唤醒词
3. 点击确定

**方法 2: 代码修改**
编辑 `KeywordWakeupService.kt`:
```kotlin
private val defaultKeywords = "x iǎo ān x iǎo ān"
```

### 调整检测灵敏度

编辑 `KeywordWakeupService.kt`:
```kotlin
keywordsThreshold = 0.25f,  // 降低值提高灵敏度
keywordsScore = 1.5f
```

## 🐛 故障排除

### 检测不到关键词
- 检查麦克风权限
- 在安静环境测试
- 清晰发音
- 调整检测阈值

### 应用崩溃
- 检查模型文件
- 查看 Logcat 日志
- 确保设备支持

### 性能问题
- 在真机上测试
- 清除历史记录
- 停止不必要的监听

## 📝 开发说明

### 查看日志
```bash
adb logcat | grep -E "KwsViewModel|KeywordWakeupService"
```

### 构建变体
```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目仅供学习和参考使用。

## 🎉 开始使用

现在就运行应用，体验离线语音唤醒功能！

```bash
./gradlew installDebug
```

---

**Made with ❤️ using Jetpack Compose**

# Compose 播放器全功能迁移计划

## 目标与完成标准

将视频播放页从 JZVD 传统 View 宿主迁移为纯 Compose 页面。迁移完成时必须满足：

1. 播放器画面由 Compose `AndroidExternalSurface` 承载，不再使用 `AndroidView`、`ComposeView` 或手工拼装的 View 容器。
2. ExoPlayer、系统 MediaPlayer、MPV 三种现有内核继续可选，现有播放器设置保持有效。
3. 清晰度、倍速、进度恢复、手势、全屏、自动旋转、画中画、H 关键帧、MPV Anime4K 等现有能力全部迁移。
4. 播放页介绍、评论、平板侧栏与播放器使用同一个 Compose 布局树。
5. 删除 JZVD 依赖、`HJzvdStd`、旧媒体内核适配层、JZVD 数据源、旧布局和仅供 JZVD 使用的资源。
6. 全项目扫描不存在 `cn.jzvd`、`Jzvd`、`HJzvdStd`、播放器相关 `AndroidView` 和旧 JZVD 布局引用。
7. Debug Kotlin 编译、资源处理、相关测试和静态残留检查全部通过，并完成真机/模拟器播放验证清单。

## 当前架构审计

| 能力 | 当前所有者 | Compose 迁移目标 |
| --- | --- | --- |
| 视频渲染 | `HJzvdStd` 的 `TextureView` | `AndroidExternalSurface` |
| ExoPlayer | `ExoMediaKernel`，通过 `JZMediaInterface` 回调 UI | 独立 `ExoPlaybackEngine`，状态通过 `StateFlow` 暴露 |
| 系统 MediaPlayer | `SystemMediaKernel` | 独立 `SystemPlaybackEngine` |
| MPV | `MpvMediaKernel` | 独立 `MpvPlaybackEngine`，保留全部 MPV 参数和 Anime4K |
| 播放状态 | JZVD 整数状态和静态 `CURRENT_JZVD` | 强类型 `PlaybackPhase` 与不可变状态 |
| 清晰度切换 | `JZDataSource` + PopupWindow | Compose 菜单，保留进度与播放状态切源 |
| 倍速 | `HJzvdStd` 字段与弹窗 | 控制器状态 + Compose 菜单 |
| 进度恢复 | `HJzvdStd.savedProgress` | 控制器首次就绪时恢复，Compose 提供继续播放提示 |
| 手势 | JZVD touch override | Compose pointer input：横向进度、左侧亮度、右侧音量、长按快进 |
| 全屏/旋转 | JZVD 把 View 移入 decorView | Compose 页面状态 + Activity 方向和系统栏控制 |
| 画中画 | 读取 JZVD 静态实例 | `VideoPageHost` 直接委托 Compose 控制器 |
| H 关键帧 | JZVD View、PopupWindow 内嵌 Compose | Compose 控制栏、列表和编辑对话框 |
| 页面布局 | `LinearLayout` + `FrameLayout` + `ComposeView` | 单一 Compose `VideoShellContent` |
| Wi-Fi 提示 | 覆写 JZVD 对话框 | 播放前策略回调 + Compose 对话框 |

## 实施阶段

### 阶段一：内核解耦

- 定义统一播放请求、播放状态、内核类型和 `PlaybackEngine` 接口。
- 实现 ExoPlayer 内核，支持 HLS、普通媒体、请求头、本地 URI、缓冲状态和视频尺寸。
- 实现系统 MediaPlayer 与 MPV 内核；MPV 参数从现有实现逐项迁移。
- 建立控制器，负责清晰度选择、切源保进度、默认倍速、进度轮询和 Surface 生命周期。

### 阶段二：Compose 播放器 UI

- 重写 `VideoPlayerUi`，移除 `View` 参数和 `AndroidView`。
- 接入 `AndroidExternalSurface`，画面层、封面、加载、错误和重试状态完全由 Compose 驱动。
- 完成顶部栏、底部进度、播放暂停、全屏、锁定、倍速、清晰度、Anime4K 与 H 关键帧入口。
- 控件在播放后自动隐藏，点击画面恢复；锁定时只保留解锁入口。

### 阶段三：完整交互和系统能力

- 横向拖动预览/提交进度，左侧纵向亮度，右侧纵向音量。
- 长按按当前倍速乘设置倍率快进，松开恢复原倍速。
- 继续播放提示、底部常驻进度条、倒计时提示与评论联动。
- 全屏、返回优先退出全屏、横竖屏视频方向判断、自动旋转和系统栏沉浸。
- 画中画进入条件、SourceRect、播放/暂停 RemoteAction 和生命周期暂停策略。

### 阶段四：页面宿主迁移

- `VideoRouteHostScreen` 直接组合播放器、标签页和平板相关视频侧栏。
- `VideoShellContent` 改为接收 Compose 内容，不再创建或搬运 View。
- 删除 `VideoRouteShell`、`ComposeView`、`FrameLayout`、`LinearLayout` 和 JZVD 返回回调。
- 历史进度、评论徽标、下载流程和导航行为接到新控制器。

### 阶段五：清理与验证

- 将设置页对速度常量和内核枚举的引用迁到新播放器模型。
- 将 H 关键帧时间格式化改为项目内工具。
- 删除 `HJzvdStd.kt`、旧 `HMediaKernel.kt`、`HanimeDataSource.kt`、JZVD XML 和专用资源。
- 删除 `jiaozivideoplayer` 依赖及版本目录项。
- 执行编译、测试和以下残留扫描：

```text
cn.jzvd | Jzvd | HJzvdStd | JZDataSource | JZMediaInterface
layout_jzvd | AndroidView | ComposeView
```

## 运行验证清单

- 在线 HLS/MP4、下载文件和 content URI 均可播放。
- 三种内核分别完成播放、暂停、拖动、切清晰度、切倍速和释放。
- 前后台、页面切换、锁屏、来电音频焦点和旋转不泄漏播放器。
- 竖屏、横屏、平板侧栏、全屏和画中画布局正确。
- 弱网缓冲、错误重试、移动网络确认和空链接跳转行为正确。
- 历史进度写入与恢复、H 关键帧增删改跳转、MPV Anime4K 正常。


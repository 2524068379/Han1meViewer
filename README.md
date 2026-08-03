# 🚫 请不要在任何公开平台宣传本软件

本软件不接受任何形式的公开宣传。若出现公开宣传、搬运或引流，仓库维护者可能随时归档或隐藏仓库，并删除已编译的发行版。

# 🌸 Han1meViewer+

🔞 **R18 警告：未满 18 岁禁止下载和使用。**

Han1meViewer 是一个使用 Kotlin 开发的 Android 客户端，用于浏览、搜索、播放和管理 hanime 相关公开视频页面内容。当前项目以 Jetpack Compose、Material 3、Navigation 3、ViewModel、StateFlow、Retrofit、Jsoup、Room、DataStore、WorkManager、Media3/MPV 为主要技术栈，围绕视频浏览、详情播放、搜索、用户列表、下载管理、评论、订阅、设置和隐私保护等功能组织。

本应用没有任何官方网站。GitHub Release 是唯一下载及更新渠道。

本项目[原仓库](https://github.com/misaka10032w/Han1meViewer)已归档。因为对项目弃坑感到惋惜，同时也是原项目的使用者，所以现由我进行接下来的维护。

以下是原项目维护者的停更声明：

```
鉴于很多用户的意见，对部分功能存在较大意见，开发目标产生分歧，但是又说不出个所以然来，我又猜不到你想说什么，故：

本项目将不再提供更新，仓库将归档，并已删除所有编译产物，如您需要更丰富的功能，请自行下载修改编译。

有缘再见
```

还请大家好好看片，不要去打击用爱发电的开发者们的积极性

# 📜 目前做了什么

### 已移除

- GMS 和 Firebase 追踪统计模块
- CI 更新频道
- 旧外部存储读写权限与 Android 9 以下兼容代码
- 创作中心、日本语翻译（日本网友无法访问H站，保留日语无意义）
- 旧的主题、多语言和依赖传统 View 的工具类
- JZVD

### 当前功能与重构

- 使用 Material 3 和 Compose 重构主要页面、卡片、列表、弹窗和播放器界面，拥有更精致的 UI 和更好的一致性。
- 主导航迁移至 Navigation 3，使用统一路由、顶层返回栈、页面级状态保存和预测性返回；登录、Cookie 手动导入及 Cloudflare 验证迁移到单 Activity 架构。
- 将应用设置从 SharedPreferences 迁移到 Preferences DataStore，网络、Cookie、下载、播放器、主题、语言、签到、首页和备份等设置使用统一的数据流和仓储。
- 完全重构的 ExoPlayer、MediaPlayer、MPV 三种播放链路，以及 Anime4K、关键 H 帧、清晰度切换、倍速、画中画、本地视频和播放手势。
- “冲了么”小组件使用 Glance Compose 实现。
- 播放页可选的平板模式现支持经典和分栏两种样式。

### 近期修复和完善

- 视频卡片解析适配网站结构变化，补全作者与时长显示，并增强解析失败、登录过期、Cloudflare 和 IP blocked 的状态提示。
- 修复新播放器切换视频、重新挂载 Surface、暂停帧和横竖屏尺寸同步问题；修复播放器高度和页面切换时的状态同步。
- 播放页推荐区和经典平板侧栏改用惰性列表，避免超大离屏图层导致 RenderThread 崩溃；搜索筛选标签改为连续折叠，减少滚动抖动。
- 优化了超大字号下的标题显示和横屏挖孔区域安全边距，在所有设备上的体验更一致。
- 强化 Cloudflare 验证后的 Cookie 主机隔离、并发等待、取消与超时处理；退出登录后及时清理相关状态。

## 🤝 贡献说明

- 提交代码前请先确认可以通过 `:app:compileDebugKotlin`。
- 修改网络列表、分页或 Compose `Lazy*` 列表时，请检查重复 key 风险。
- 修改播放、下载、账号、Cookie、Cloudflare、更新逻辑时，请尽量说明验证方式。
- 提交共享关键 H 帧可参考 `.github/PULL_REQUEST_TEMPLATE/submit_h_keyframe.md`。

## 🧩 TODO

- 以后再说

# 📄 许可证

- 本项目作为包含 GPLv3 派生代码的整体，按 GNU GPLv3 发布。
- 项目包含来自 [MomoQR](https://github.com/daisukiKaffuChino/MomoQR) 的代码，归属作者 daisukiKaffuChino，并遵循 GPLv3。
- 原项目 Yenaly 的遗留归属和 MomoQR 归属见 [NOTICE](NOTICE)，Apache-2.0 许可证文本见 [LICENSE-APACHE](LICENSE-APACHE)；完整许可证说明见 [LICENSE](LICENSE)。

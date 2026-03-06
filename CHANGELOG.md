# 更新日志 (Changelog)

## [1.1.0] - 2024-xx-xx

### 新增功能

#### 竖屏全屏模式
- 新增竖屏全屏按钮，点击后以竖屏方向进入全屏模式
- 适合短剧、短视频等竖屏视频场景
- `setFullscreenPortraitButtonVisibility(int visibility)` - 控制竖屏全屏按钮显示
- `setOnFullscreenPortraitClickListener()` - 竖屏全屏点击监听

#### 按钮可见性控制
- 支持批量控制底部按钮可见性
  - `setVisibilityBottom(select, speed, previous, next)` - 4参数版本
  - `setVisibilityBottom(select, speed, previous, next, fullscreen, fullscreenPortrait)` - 6参数版本
- 支持单独控制各按钮可见性
  - `setSelectButtonVisibility()` - 选集按钮
  - `setSpeedButtonVisibility()` - 倍速按钮
  - `setPreviousButtonVisibility()` - 上一集按钮
  - `setNextButtonVisibility()` - 下一集按钮
  - `setFullscreenButtonVisibility()` - 全屏按钮
  - `setFullscreenPortraitButtonVisibility()` - 竖屏全屏按钮
- 支持批量控制顶部按钮可见性
  - `setTitleButtonsVisibility(back, pip, screen, settings)`
- 支持单独控制顶部各按钮可见性
  - `setBackButtonVisibility()` - 返回按钮
  - `setPipButtonVisibility()` - 小窗按钮
  - `setScreenButtonVisibility()` - 投屏按钮
  - `setSettingsButtonVisibility()` - 设置按钮
  - `setSysTimeVisibility()` - 系统时间

### 优化改进

- 修复全屏切换后按钮可见性状态丢失的问题
- 按钮可见性状态现在会在竖屏/全屏布局切换时保持一致

---

## [1.0.0] - 初始版本

### 功能特性

- **双内核支持** - 支持 IJKPlayer 和 ExoPlayer 两种播放内核，可在设置中切换
- **选集功能** - 剧集列表展示与快速切换，支持自定义适配器
- **倍速播放** - 支持多档倍速（0.5x、0.75x、1.0x、1.25x、1.5x、2.0x）
- **长按倍速** - 长按屏幕快速播放（默认3倍速，可自定义1.0x~10.0x）
- **定时关闭** - 支持30分钟、60分钟定时关闭
- **跳过片头/片尾** - 自动跳过指定时间段
- **画面比例调整** - 支持16:9、4:3、默认、填充、缩放、裁剪等模式
- **静音控制** - 一键静音/取消静音
- **手势控制** - 滑动调节亮度、音量、进度
- **全屏/小窗模式** - 支持全屏播放和画中画
- **投屏功能接口** - 预留投屏回调接口
- **刘海屏适配** - 完美适配刘海屏设备
- **锁屏功能** - 全屏模式下可锁定控制栏
- **隐藏进度条** - 可选择隐藏底部进度条
- **自动旋转** - 支持根据设备方向自动切换横竖屏

# StarVideoPlayer
[![](https://jitpack.io/v/1240444767/StarVideoPlayer.svg)](https://jitpack.io/#1240444767/StarVideoPlayer)
![GitHub Repo stars](https://img.shields.io/github/stars/1240444767/StarVideoPlayer)

专为短剧/视频应用设计的 Android 播放器，基于 DKPlayer。

## 功能特性

- **播放内核** — 默认 ExoPlayer 2.x，可通过 `setPlayerFactory()` 切换 IJKPlayer 等
- **选集功能** — 默认适配器 / 自定义适配器 / 完全自定义内容面板
- **倍速播放** — 0.5x ~ 3.0x，弹出菜单选择
- **长按倍速** — 默认 3x，1.0x ~ 10.0x 可调
- **双击暂停/播放**
- **定时关闭** — 播完当前 / 30 分钟 / 60 分钟
- **跳过片头/片尾** — 自动跳过，持久化
- **画面比例** — 默认 / 16:9 / 4:3 / 填充 / 原始 / 裁剪（持久化）
- **静音** / **手势控制**（亮度/音量/进度）/ **锁屏**
- **横竖全屏** — 横屏全屏 + 竖屏全屏双模式
- **刘海屏适配** / **自动旋转**
- **投屏接口** — 预留回调 + 内置 DLNA-Cast 模块
- **按钮可见性** — `setButtonVisible(PlayerButton, vis)` 统一入口，区分全屏/非全屏
- **颜色自定义** — `setColor(PlayerColor, color)` 一键设色
- **短剧播放器** — `StarShortDramaPlayer`，自动精简按钮
- **底部栏合二为一** — 单 XML 双容器，切换无开销

---

## 引入

### 依赖

```groovy
dependencies {
    implementation 'xyz.doikki.android.dkplayer:dkplayer-java:3.3.7'
    // Media3 由本库自带，无需额外引入
    implementation 'com.github.1240444767:StarVideoPlayer:2.1.8'
}
```

### 布局

```xml
<!-- 标准播放器 -->
<com.star.play.StarVideoPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="200dp" />

<!-- 短剧播放器（无选集/上下集按钮） -->
<com.star.play.StarShortDramaPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 权限

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 快速开始

```java
StarVideoPlayer player = findViewById(R.id.player);
// 默认 ExoPlayer 内核，无需手动设 PlayerFactory

player.setUrl("https://example.com/video.mp4");
player.addDefaultControlComponent("视频标题", false);
player.start();

@Override protected void onPause()  { player.pause(); }
@Override protected void onResume() { player.resume(); }
@Override protected void onDestroy(){ player.release(); }
@Override public void onBackPressed() {
    if (!player.onBackPressed()) super.onBackPressed();
}
```

**切换内核：**

```java
// 默认 ExoPlayer，如需 IJKPlayer：
player.setPlayerFactory(IjkPlayerFactory.create());
```

---

## 播放控制

```java
// 倍速
player.setPlaybackSpeed(1.5f);
float s = player.getPlaybackSpeed();

// 长按倍速
player.setLongPressSpeed(3.0f);
float l = player.getLongPressSpeed();

// 静音
player.setMuted(true);
boolean m = player.isMuted();

// 画面比例（持久化）
player.setScreenScale(VideoView.SCREEN_SCALE_16_9);
int sc = player.getScreenScale();

// 跳过片头片尾（秒）
player.setSkipStartTime(30);
player.setSkipEndTime(60);

// 定时关闭
player.setTimingOption("30分钟"); // 不启用 / 播完当前 / 30分钟 / 60分钟

// 自动下一集
player.setAutoNext(true);

// 隐藏底部进度条
player.setHideProgress(true);

// 自动旋转
player.setAutoRotate(true);
```

---

## 选集

```java
// 默认适配器
player.setEpisodes(Arrays.asList("第1集","第2集","第3集"), 0);
player.setOnEpisodeSelectListener((index, title) -> {
    player.setUrl(urls.get(index));
    player.start();
});

// 自定义适配器
player.setEpisodeAdapter(new MyAdapter(data));

// 完全自定义面板
player.setEpisodeCustomContentView(R.layout.my_panel);
player.restoreEpisodeDefaultContent();

// 面板控制
player.showEpisodePanel();
player.hideEpisodePanel();
player.setEpisodePanelTitle("选集");
player.setCurrentEpisodeIndex(3);
```

---

## 监听器

```java
player.setOnWindowClickListener(v -> { /* 小窗 */ });
player.setOnScreenClickListener(v -> { /* 投屏 */ });
player.setOnUpSetClickListener(v -> { /* 上一集 */ });
player.setOnDownSetClickListener(v -> { /* 下一集 */ });
player.setOnEpisodeSelectListener((i, t) -> { /* 选集 */ });
player.setOnFullscreenPortraitClickListener(v -> { /* 竖屏全屏 */ });
```

---

## 按钮可见性

```java
import static com.star.play.PlayerButton.*;

// 全局（非全屏 + 全屏一致）
player.setButtonVisible(SELECT, View.GONE);
player.setButtonVisible(SPEED, View.VISIBLE);

// 区分模式（第 2 参数 = 非全屏，第 3 参数 = 全屏）
player.setButtonVisible(SELECT, View.GONE, View.VISIBLE);
player.setButtonVisible(PREV, View.GONE, View.VISIBLE);
```

底部 6 个：`SELECT` `SPEED` `PREV` `NEXT` `FULLSCREEN` `PORTRAIT_FULLSCREEN`
顶部 5 个：`BACK` `PIP` `CAST` `SETTINGS` `SYS_TIME`

---

## 颜色

```java
import static com.star.play.PlayerColor.*;

// 标题栏所有按钮图标（返回/小窗/投屏/设置）
player.setColor(TITLE_ICON, Color.WHITE);
// 底部栏所有按钮图标（播放/上下集/倍速/选集/全屏/竖屏全屏）
player.setColor(BOTTOM_ICON, Color.WHITE);
```

---

## 设置面板

```java
player.showSettingsPanel();
player.hideSettingsPanel();
```

面板内可调：画面比例、静音、隐藏进度条、自动旋转、定时关闭、长按倍速、跳过片头片尾。全部自动持久化。

---

## API 速查

| 分类 | 方法 |
|------|------|
| **生命周期** | `setPlayerFactory()` `setUrl()` `addDefaultControlComponent()` `start()` `pause()` `resume()` `release()` `onBackPressed()` |
| **播放** | `setPlaybackSpeed()` `getPlaybackSpeed()` `setLongPressSpeed()` `getLongPressSpeed()` `setMuted()` `isMuted()` |
| **画面** | `setScreenScale()` `getScreenScale()` `setHideProgress()` `isHideProgress()` `setAutoRotate()` `isAutoRotate()` |
| **跳过** | `setSkipStartTime()` `getSkipStartTime()` `setSkipEndTime()` `getSkipEndTime()` |
| **定时** | `setTimingOption()` `getTimingOption()` `setAutoNext()` `isAutoNext()` |
| **选集** | `setEpisodes()` `setEpisodeAdapter()` `getEpisodeAdapter()` `getEpisodeRecyclerView()` `getCurrentEpisodeIndex()` `setCurrentEpisodeIndex()` |
| **选集面板** | `showEpisodePanel()` `hideEpisodePanel()` `isEpisodePanelShowing()` `setEpisodePanelTitle()` `setEpisodePanelTitleColor()` `setEpisodePanelTitleBarVisibility()` `setEpisodePanelCloseButtonVisibility()` `setEpisodeCustomContentView()` `restoreEpisodeDefaultContent()` `getEpisodeContentContainer()` |
| **设置面板** | `showSettingsPanel()` `hideSettingsPanel()` `isSettingsPanelShowing()` |
| **按钮** | `setButtonVisible(PlayerButton, vis)` `setButtonVisible(PlayerButton, normalVis, fullscreenVis)` |
| **颜色** | `setColor(PlayerColor, color)` |
| **监听** | `setOnWindowClickListener()` `setOnScreenClickListener()` `setOnUpSetClickListener()` `setOnDownSetClickListener()` `setOnEpisodeSelectListener()` `setOnFullscreenPortraitClickListener()` |

**画面比例常量：** `SCREEN_SCALE_DEFAULT` `SCREEN_SCALE_16_9` `SCREEN_SCALE_4_3` `SCREEN_SCALE_MATCH_PARENT` `SCREEN_SCALE_ORIGINAL` `SCREEN_SCALE_CENTER_CROP`

---

## 混淆

```pro
-keep class com.star.play.** { *; }
-dontwarn com.star.play.**
```

---

## 依赖说明

基于 [DKPlayer](https://github.com/Doikki/DKVideoPlayer)。

---

## 更新日志

### v2.1.8 (2026-05-03)

#### 新增
- ✨ 统一按钮 API —— `setButtonVisible(PlayerButton, vis)` 替代 25 个分散方法
- ✨ 统一颜色 API —— `setColor(PlayerColor, color)` 替代 7 个分散方法

#### 优化
- 🔧 底部栏 XML 三合一，切换零开销
- 🔧 `StarPlayerSettings` 统一持久化
- 🔧 `StarCutoutHelper` 消除刘海屏重复代码
- 🔧 选集适配器提取为 `StarDefaultEpisodeAdapter`（可继承扩展）

#### 修复
- 🐛 画面比例重启后丢失 — 已持久化

### v2.0.0 (2026-05-02)
- 🔧 抽取 `StarPlayerSettings` 统一管理所有 SharedPreferences 持久化
- 🔧 抽取 `StarCutoutHelper` 合并 4 处重复的刘海屏适配代码
- 🔧 设置面板画面比例映射从字符串匹配改为资源 ID 匹配
- 🔧 清理 `play/build.gradle` 重复的 `buildFeatures` 声明



### v1.9.0 (2026-04-07)

- SeekBar 替换 Slider、拖动时间指示器、标题栏/底部栏颜色自定义

### v1.8.0 (2025-03-29)

- 移除设置面板内核切换，改为用户自行 `setPlayerFactory()`

### v1.7.0 ~ v1.0.0

- 按钮可见性区分全屏/非全屏、倍速弹窗、竖屏全屏、选集、手势控制、锁屏等

## License

Apache License 2.0

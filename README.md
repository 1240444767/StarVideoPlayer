# StarVideoPlayer
[![](https://jitpack.io/v/1240444767/StarVideoPlayer.svg)](https://jitpack.io/#1240444767/StarVideoPlayer)
![GitHub Repo stars](https://img.shields.io/github/stars/1240444767/StarVideoPlayer)

一个功能强大的 Android 视频播放器库，专为短剧/视频应用设计，基于 DKPlayer 开发。

## 功能特性

- **播放内核** — 支持 ExoPlayer、IJKPlayer，用户自行选择引入
- **选集功能** — 剧集列表展示与快速切换，支持默认适配器、自定义适配器、完全自定义内容面板
- **倍速播放** — 点击倍速按钮弹出菜单选择（0.5x ~ 3.0x）
- **长按倍速** — 长按屏幕快速播放（默认 3 倍速，可自定义 1.0x ~ 10.0x）
- **双击暂停/播放** — 双击屏幕切换播放/暂停状态
- **定时关闭** — 支持「播完当前」「30 分钟」「60 分钟」
- **跳过片头/片尾** — 自动跳过指定时间段
- **画面比例调整** — 支持默认、16:9、4:3、填充、原始、裁剪（设置持久化，重启恢复）
- **静音控制** — 一键静音/取消静音
- **手势控制** — 滑动调节亮度、音量、进度
- **全屏/小窗模式** — 支持横屏全屏和竖屏全屏两种模式
- **投屏功能接口** — 预留投屏回调接口
- **刘海屏适配** — 完美适配刘海屏设备
- **锁屏功能** — 全屏模式下可锁定控制栏
- **隐藏进度条** — 底部常驻进度条可隐藏
- **自动旋转** — 根据视频宽高比自动切换横竖屏
- **按钮可见性控制** — 底部/顶部各按钮独立控制，支持区分全屏/非全屏状态
- **颜色自定义** — 标题栏和底部栏的文字颜色、图标颜色、背景颜色
- **拖动时间指示器** — 拖动 SeekBar 时气泡跟随显示当前时间
- **短剧播放器** — `StarShortDramaPlayer`，自动隐藏选集/上下集按钮

## 引入方式

### Step 1. 添加 JitPack 仓库

在项目根目录的 `settings.gradle` 中添加：

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2. 添加依赖

```groovy
dependencies {
    implementation 'xyz.doikki.android.dkplayer:dkplayer-java:3.3.7'
    // 根据需要选择播放内核（二选一或都引入）
    implementation 'xyz.doikki.android.dkplayer:player-exo:3.3.7'   // ExoPlayer
    // implementation 'xyz.doikki.android.dkplayer:player-ijk:3.3.7'  // IJKPlayer
    implementation 'com.github.1240444767:StarVideoPlayer:2.0.0'
}
```

## 使用方法

### 1. 权限

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 2. 布局

```xml
<!-- 标准播放器（带选集功能） -->
<com.star.play.StarVideoPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="200dp" />

<!-- 短剧播放器（精简版，无选集/上下集按钮） -->
<com.star.play.StarShortDramaPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 3. 基础使用

```java
public class MainActivity extends AppCompatActivity {
    private StarVideoPlayer videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.player);

        // 必须设置播放内核（根据你引入的依赖选择）
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create());   // ExoPlayer
        // videoView.setPlayerFactory(IjkPlayerFactory.create());     // IJKPlayer

        // 设置视频地址和标题
        videoView.setUrl("https://example.com/video.mp4");
        videoView.addDefaultControlComponent("视频标题", false);

        // 开始播放
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }

    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
```

### 4. 短剧播放器 StarShortDramaPlayer

`StarShortDramaPlayer` 继承自 `StarVideoPlayer`，构造函数中自动隐藏选集、上一集、下一集按钮。用法与父类完全一致。

```java
StarShortDramaPlayer videoView = findViewById(R.id.player);
videoView.setPlayerFactory(ExoMediaPlayerFactory.create());
videoView.setUrl("https://example.com/video.m3u8");
videoView.addDefaultControlComponent("短剧标题", false);
videoView.start();
```

---

### 5. 播放控制

```java
// 倍速（通过底部倍速按钮弹出菜单选择，支持 0.5x ~ 3.0x）
videoView.setPlaybackSpeed(1.5f);
float speed = videoView.getPlaybackSpeed();

// 长按倍速（默认为 3.0x，范围 1.0x ~ 10.0x，在设置面板中调节）
videoView.setLongPressSpeed(3.0f);
float longPress = videoView.getLongPressSpeed();

// 静音
videoView.setMuted(true);
boolean isMute = videoView.isMuted();

// 画面比例（设置持久化，重启恢复）
videoView.setScreenScale(VideoView.SCREEN_SCALE_16_9);
videoView.setScreenScale(VideoView.SCREEN_SCALE_4_3);
videoView.setScreenScale(VideoView.SCREEN_SCALE_DEFAULT);
videoView.setScreenScale(VideoView.SCREEN_SCALE_MATCH_PARENT);
videoView.setScreenScale(VideoView.SCREEN_SCALE_ORIGINAL);
videoView.setScreenScale(VideoView.SCREEN_SCALE_CENTER_CROP);
int scale = videoView.getScreenScale();

// 隐藏底部进度条
videoView.setHideProgress(true);
boolean hidden = videoView.isHideProgress();

// 自动旋转（根据视频宽高比自动切换横竖屏）
videoView.setAutoRotate(true);
boolean auto = videoView.isAutoRotate();

// 跳过片头/片尾（单位：秒）
videoView.setSkipStartTime(30);
videoView.setSkipEndTime(60);
int start = videoView.getSkipStartTime();
int end = videoView.getSkipEndTime();

// 定时关闭（"不启用"、"播完当前"、"30分钟"、"60分钟"）
videoView.setTimingOption("30分钟");
String timing = videoView.getTimingOption();

// 自动播放下一集（默认 true，播完当前自动触发下一集回调）
videoView.setAutoNext(true);
boolean autoNext = videoView.isAutoNext();
```

---

### 6. 选集功能

#### 方式一：默认适配器

```java
List<String> episodes = Arrays.asList("第1集", "第2集", "第3集");
videoView.setEpisodes(episodes, 0);  // 第二个参数为当前选中索引

videoView.setOnEpisodeSelectListener((index, title) -> {
    videoView.setUrl(getEpisodeUrl(index));
    videoView.start();
});
```

#### 方式二：自定义适配器

```java
MyEpisodeAdapter adapter = new MyEpisodeAdapter(episodeList);
videoView.setEpisodeAdapter(adapter);

RecyclerView recyclerView = videoView.getEpisodeRecyclerView();
```

#### 方式三：完全自定义内容面板

```java
// 用自定义 View 替换选集面板中的 RecyclerView
videoView.setEpisodeCustomContentView(customView);
// 或用布局资源
videoView.setEpisodeCustomContentView(R.layout.my_episode_panel);

// 恢复默认 RecyclerView
videoView.restoreEpisodeDefaultContent();

// 获取内容容器，动态添加子 View
FrameLayout container = videoView.getEpisodeContentContainer();
```

#### 选集面板外观

```java
videoView.setEpisodePanelTitle("选择剧集");
videoView.setEpisodePanelTitleColor(Color.WHITE);
videoView.setEpisodePanelTitleBarVisibility(View.VISIBLE);
videoView.setEpisodePanelCloseButtonVisibility(View.VISIBLE);
```

#### 选集面板控制

```java
videoView.showEpisodePanel();
videoView.hideEpisodePanel();
boolean showing = videoView.isEpisodePanelShowing();
```

#### 当前集数

```java
videoView.setCurrentEpisodeIndex(3);
int index = videoView.getCurrentEpisodeIndex();
```

---

### 7. 上一集 / 下一集

```java
videoView.setOnUpSetClickListener(view -> {
    int prev = videoView.getCurrentEpisodeIndex() - 1;
    videoView.setCurrentEpisodeIndex(prev);
    videoView.setUrl(getEpisodeUrl(prev));
    videoView.start();
});

videoView.setOnDownSetClickListener(view -> {
    int next = videoView.getCurrentEpisodeIndex() + 1;
    videoView.setCurrentEpisodeIndex(next);
    videoView.setUrl(getEpisodeUrl(next));
    videoView.start();
});
```

---

### 8. 监听器

```java
// 小窗按钮（画中画）
videoView.setOnWindowClickListener(view -> { /* 启动小窗 */ });

// 投屏按钮
videoView.setOnScreenClickListener(view -> { /* 启动投屏 */ });

// 选集按钮点击（面板中选中某集后触发）
videoView.setOnSelectClickListener(view -> { /* 处理选集 */ });

// 上一集 / 下一集
videoView.setOnUpSetClickListener(view -> { /* 上一集 */ });
videoView.setOnDownSetClickListener(view -> { /* 下一集 */ });

// 选集面板选中
videoView.setOnEpisodeSelectListener((index, title) -> { /* 切换视频 */ });

// 竖屏全屏按钮
videoView.setFullscreenPortraitButtonVisibility(View.VISIBLE);
videoView.setOnFullscreenPortraitClickListener(view -> { /* 额外操作 */ });
```

---

### 9. 按钮可见性控制

所有方法接受 `View.VISIBLE`、`View.GONE` 或 `View.INVISIBLE`。

#### 底部按钮

```java
// 全局（同时影响全屏和非全屏）
videoView.setVisibilityBottom(View.GONE, View.GONE, View.GONE, View.GONE);
videoView.setVisibilityBottom(select, speed, previous, next, fullscreen, portraitFullscreen);

// 仅非全屏
videoView.setVisibilityBottomNormal(select, speed, previous, next);
videoView.setVisibilityBottomNormal(select, speed, previous, next, fullscreen, portraitFullscreen);

// 仅全屏
videoView.setVisibilityBottomFullscreen(select, speed, previous, next);
videoView.setVisibilityBottomFullscreen(select, speed, previous, next, fullscreen, portraitFullscreen);

// 一次性设置全屏+非全屏 （12 参数）
videoView.setVisibilityBottomAll(
    select_n, speed_n, prev_n, next_n, full_n, pf_n,   // 非全屏
    select_f, speed_f, prev_f, next_f, full_f, pf_f     // 全屏
);

// 单独控制 — 全局
videoView.setSelectButtonVisibility(visibility);
videoView.setSpeedButtonVisibility(visibility);
videoView.setPreviousButtonVisibility(visibility);
videoView.setNextButtonVisibility(visibility);
videoView.setFullscreenButtonVisibility(visibility);
videoView.setFullscreenPortraitButtonVisibility(visibility);

// 单独控制 — 区分 Normal / Fullscreen
videoView.setSelectButtonVisibilityNormal(visibility);
videoView.setSelectButtonVisibilityFullscreen(visibility);
// ... 每个按钮都有对应的 Normal / Fullscreen 版本，格式为 setXxxVisibility[状态后缀]
```

#### 顶部按钮

```java
// 批量
videoView.setTitleButtonsVisibility(back, pip, screen, settings);

// 单独
videoView.setBackButtonVisibility(visibility);
videoView.setPipButtonVisibility(visibility);
videoView.setScreenButtonVisibility(visibility);
videoView.setSettingsButtonVisibility(visibility);
videoView.setSysTimeVisibility(visibility);
```

---

### 10. 颜色自定义

```java
// 标题栏
videoView.setTitleTextColor(Color.WHITE);
videoView.setSysTimeTextColor(Color.WHITE);
videoView.setTitleContainerBackground(Color.TRANSPARENT);
videoView.setButtonIconTint(Color.WHITE);           // 标题栏按钮图标

// 底部控制栏
videoView.setTimeTextColor(Color.WHITE);
videoView.setBottomContainerBackground(Color.TRANSPARENT);
videoView.setBottomButtonIconTint(Color.WHITE);     // 底部按钮图标
```

---

### 11. 设置面板控制

```java
videoView.showSettingsPanel();
videoView.hideSettingsPanel();
boolean open = videoView.isSettingsPanelShowing();
```

用户可通过设置面板调节：画面比例、静音、隐藏进度条、自动旋转、定时关闭、长按倍速、跳过片头片尾。所有设置自动持久化到 SharedPreferences，重启恢复。

---

## API 速查

### 核心生命周期

| 方法 | 说明 |
|------|------|
| `setPlayerFactory(PlayerFactory factory)` | 设置播放内核（必须调用） |
| `setUrl(String url)` | 设置视频地址 |
| `addDefaultControlComponent(String title, boolean isLive)` | 设置标题并初始化控制器 |
| `start()` | 开始播放 |
| `pause()` | 暂停播放 |
| `resume()` | 恢复播放 |
| `release()` | 释放播放器资源 |
| `onBackPressed()` | 处理返回键（返回 true 表示已处理） |

### 播放控制

| 方法 | 说明 |
|------|------|
| `setPlaybackSpeed(float)` / `getPlaybackSpeed()` | 设置/获取播放速度 |
| `setLongPressSpeed(float)` / `getLongPressSpeed()` | 设置/获取长按倍速 |
| `setMuted(boolean)` / `isMuted()` | 设置/获取静音状态 |
| `setScreenScale(int)` / `getScreenScale()` | 设置/获取画面比例（持久化） |
| `setHideProgress(boolean)` / `isHideProgress()` | 设置/获取隐藏进度条（持久化） |
| `setAutoRotate(boolean)` / `isAutoRotate()` | 设置/获取自动旋转（持久化） |
| `setSkipStartTime(int)` / `getSkipStartTime()` | 设置/获取跳过片头（持久化） |
| `setSkipEndTime(int)` / `getSkipEndTime()` | 设置/获取跳过片尾（持久化） |
| `setTimingOption(String)` / `getTimingOption()` | 设置/获取定时关闭选项 |
| `setAutoNext(boolean)` / `isAutoNext()` | 设置/获取自动播放下一集（持久化） |

### 选集

| 方法 | 说明 |
|------|------|
| `setEpisodes(List<String>, int)` | 设置剧集列表（使用默认适配器） |
| `setEpisodeAdapter(Adapter<?>)` | 设置自定义适配器 |
| `getEpisodeAdapter()` | 获取当前适配器 |
| `getEpisodeRecyclerView()` | 获取 RecyclerView |
| `getCurrentEpisodeIndex()` | 获取当前集数 |
| `setCurrentEpisodeIndex(int)` | 设置当前集数（更新 UI 高亮） |
| `showEpisodePanel()` / `hideEpisodePanel()` | 显示/隐藏选集面板 |
| `isEpisodePanelShowing()` | 选集面板是否正在显示 |
| `setEpisodePanelTitle(String)` | 设置面板标题 |
| `setEpisodePanelTitleColor(int)` | 设置面板标题颜色 |
| `setEpisodePanelTitleBarVisibility(int)` | 设置标题栏可见性 |
| `setEpisodePanelCloseButtonVisibility(int)` | 设置关闭按钮可见性 |
| `setEpisodeCustomContentView(View)` | 替换内容区域为自定义 View |
| `setEpisodeCustomContentView(int)` | 替换内容区域为自定义布局 |
| `restoreEpisodeDefaultContent()` | 恢复默认 RecyclerView 内容 |
| `getEpisodeContentContainer()` | 获取内容容器 FrameLayout |

### 按钮可见性

| 方法 | 说明 |
|------|------|
| `setVisibilityBottom(4 args)` | 底部按钮 — 全局（同时影响全屏/非全屏） |
| `setVisibilityBottom(6 args)` | 底部按钮 — 全局（含全屏/竖屏全屏按钮） |
| `setVisibilityBottomNormal(4/6 args)` | 底部按钮 — 仅非全屏 |
| `setVisibilityBottomFullscreen(4/6 args)` | 底部按钮 — 仅全屏 |
| `setVisibilityBottomAll(12 args)` | 底部按钮 — 一次性设置全屏+非全屏 |
| `setXxxVisibility(vis)` | 单独按钮 — 全局 |
| `setXxxVisibilityNormal(vis)` | 单独按钮 — 仅非全屏 |
| `setXxxVisibilityFullscreen(vis)` | 单独按钮 — 仅全屏 |
| `setTitleButtonsVisibility(4 args)` | 顶部按钮 — 批量 |
| `setXxxVisibility(vis)` | 顶部各按钮 — 单独 |

按钮列表：`Select`(选集)、`Speed`(倍速)、`Previous`(上一集)、`Next`(下一集)、`Fullscreen`(全屏)、`FullscreenPortrait`(竖屏全屏)、`Back`(返回)、`Pip`(小窗)、`Screen`(投屏)、`Settings`(设置)、`SysTime`(系统时间)。

### 颜色

| 方法 | 说明 |
|------|------|
| `setTitleTextColor(int)` | 标题文字颜色 |
| `setSysTimeTextColor(int)` | 系统时间文字颜色 |
| `setTitleContainerBackground(int)` | 标题栏背景颜色 |
| `setButtonIconTint(int)` | 标题栏按钮图标颜色 |
| `setTimeTextColor(int)` | 底部时间文字颜色 |
| `setBottomContainerBackground(int)` | 底部控制栏背景颜色 |
| `setBottomButtonIconTint(int)` | 底部按钮图标颜色 |

### 监听器

| 方法 | 说明 |
|------|------|
| `setOnWindowClickListener(l)` | 小窗按钮（画中画） |
| `setOnScreenClickListener(l)` | 投屏按钮 |
| `setOnSelectClickListener(l)` | 选集选中 |
| `setOnUpSetClickListener(l)` | 上一集按钮 |
| `setOnDownSetClickListener(l)` | 下一集按钮 |
| `setOnEpisodeSelectListener(l)` | 选集面板选中回调 |
| `setOnFullscreenPortraitClickListener(l)` | 竖屏全屏按钮 |

### 画面比例常量（定义在 VideoView 中）

| 常量 | 说明 |
|------|------|
| `SCREEN_SCALE_DEFAULT` | 默认比例 |
| `SCREEN_SCALE_16_9` | 16:9 比例 |
| `SCREEN_SCALE_4_3` | 4:3 比例 |
| `SCREEN_SCALE_MATCH_PARENT` | 填充父容器 |
| `SCREEN_SCALE_ORIGINAL` | 原始尺寸 |
| `SCREEN_SCALE_CENTER_CROP` | 居中裁剪 |

### 设置面板控制

| 方法 | 说明 |
|------|------|
| `showSettingsPanel()` | 打开设置面板 |
| `hideSettingsPanel()` | 关闭设置面板 |
| `isSettingsPanelShowing()` | 面板是否正在显示 |

---

## 混淆配置

```pro
-keep class com.star.play.** { *; }
-dontwarn com.star.play.**
```

## 依赖说明

本库基于 [DKPlayer](https://github.com/Doikki/DKPlayer) 开发，感谢原作者的贡献。

## 更新日志

### v2.0.0 (2026-05-02)

#### 修复问题
- 🐛 修复画面比例设置重启后丢失 — 画面比例现已持久化到 SharedPreferences，重启自动恢复

#### 优化改进
- 🔧 内置选集适配器 `StarDefaultEpisodeAdapter` 改为 package-private，支持通过继承扩展
- 🔧 抽取 `StarPlayerSettings` 统一管理所有 SharedPreferences 持久化
- 🔧 抽取 `StarCutoutHelper` 合并 4 处重复的刘海屏适配代码
- 🔧 设置面板画面比例映射从字符串匹配改为资源 ID 匹配
- 🔧 清理 `play/build.gradle` 重复的 `buildFeatures` 声明

---

### v1.9.0 (2026-04-07)

#### 新增功能
- ✨ SeekBar 替换 Slider — 进度条从 Material Slider 替换为原生 SeekBar
- ✨ 拖动时间指示器 — 拖动进度条时气泡跟随 thumb 实时显示时间
- ✨ 标题栏颜色自定义 — 标题文字色、系统时间色、按钮图标色、背景色
- ✨ 底部控制栏颜色自定义 — 时间文字色、按钮图标色、背景色

---

### v1.8.0 (2025-03-29)

#### 优化改进
- 移除设置面板中的播放内核切换功能，改为用户自行调用 `setPlayerFactory()` 设置内核
- 避免因未引入某个播放内核导致切换崩溃

---

### v1.7.0 (2025-03-09)

#### 新增功能
- ✨ 按钮可见性支持区分全屏/非全屏 — 新增 `Normal` / `Fullscreen` 后缀方法
- ✨ 倍速按钮弹出菜单选择播放速度 — 支持 0.5x ~ 3.0x 八档

---

### v1.6.0 (2025-03-07)

#### 修复问题
- 🐛 修复 Slider 进度值超出范围导致崩溃（`Math.max(0, Math.min(1000, progress))`）

---

### v1.4.0 (2025-03-07)

#### 新增功能
- 新增 `StarShortDramaPlayer` 短剧专用播放器，自动隐藏选集/上下集按钮
- 新增双击暂停/播放功能

---

### v1.3.0 (2025-03-06)

#### 新增功能
- 竖屏全屏模式，新增竖屏全屏按钮和监听
- 按钮可见性控制 — 底部/顶部按钮批量及单独控制

#### 优化改进
- 修复全屏切换后按钮可见性状态丢失

---

### v1.2.0 (2025-03-05)

#### 新增功能
- 选集功能支持自定义适配器
- 设置面板新增隐藏进度条、自动旋转开关
- 全屏模式下播放按钮组居中显示

#### 修复问题
- 修复锁屏按钮无效、全屏标题栏不显示、加载指示器不显示等多项问题
- 修复多处空指针风险和数组越界

---

### v1.1.0 (2025-03-04)

#### 修复问题
- 修复打包版本过高和权限冲突

---

### v1.0.0 (2025-03-03)

#### 首次发布
- 基于 DKPlayer 封装，支持双内核、倍速播放、手势控制、全屏/小窗、锁屏、刘海屏适配

## License

Apache License 2.0

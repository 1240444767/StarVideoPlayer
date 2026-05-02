package com.star.play;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.star.play.controller.StarBottomView;
import com.star.play.controller.StarCompleteView;
import com.star.play.controller.StarEpisodeView;
import com.star.play.controller.StarErrorView;
import com.star.play.controller.StarGestureView;
import com.star.play.controller.StarPrepareView;
import com.star.play.controller.StarSettingsView;
import com.star.play.controller.StarTitleView;
import java.util.Locale;

import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory;
import xyz.doikki.videoplayer.player.VideoView;

public class StarVideoPlayer extends VideoView {

    // ── 常量 ──────────────────────────────────

    private static final String TIMING_OFF = "不启用";
    private static final String TIMING_AFTER_CURRENT = "播完当前";
    private static final String TIMING_30_MIN = "30分钟";
    private static final String TIMING_60_MIN = "60分钟";

    // ── 状态字段 ──────────────────────────────

    private StarPlayerSettings mSettings;

    private float mCurrentSpeed = 1.0f;
    private String mCurrentSpeedText = "1.0x";
    private float mLongPressSpeed = 3.0f;
    private String mLongPressSpeedText = "3.0x";

    private String mTimingText = TIMING_OFF;
    private CountDownTimer mCountDownTimer;

    private StarStandardVideoController mController;
    private StarBottomView mBottomView;
    private StarEpisodeView mEpisodeView;
    private StarSettingsView mSettingsView;
    private StarTitleView mTitleView;
    private int mCurrentEpisodeIndex;

    private int mScreenScaleType = SCREEN_SCALE_DEFAULT;
    private boolean mHideProgress;
    private boolean mAutoRotate;
    private String mCurrentUrl;

    // ── 监听接口 ──────────────────────────────

    public interface OnWindowClickListener  { void onClick(android.view.View v); }
    public interface OnScreenClickListener  { void onClick(android.view.View v); }
    public interface OnSelectClickListener  { void onClick(android.view.View v); }
    public interface OnUpSetClickListener   { void onClick(android.view.View v); }
    public interface OnDownSetClickListener { void onClick(android.view.View v); }

    private OnWindowClickListener  mOnWindowClickListener;
    private OnScreenClickListener  mOnScreenClickListener;
    private OnSelectClickListener  mOnSelectClickListener;
    private OnUpSetClickListener   mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;

    // ═══════════════════════════════════════════
    // 构造 & 初始化
    // ═══════════════════════════════════════════

    public StarVideoPlayer(@NonNull Context c) { this(c, null); }
    public StarVideoPlayer(@NonNull Context c, @Nullable AttributeSet a) { this(c, a, 0); }

    public StarVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setPlayerFactory(ExoMediaPlayerFactory.create());
        mSettings = new StarPlayerSettings(getContext());
        loadSettings();
        setupController();
        setupControllerCallbacks();
        syncSettingsView();
        addOnStateChangeListener(new OnStateChangeListener() {
            @Override public void onPlayerStateChanged(int ps) { handlePlayerState(ps); }
            @Override public void onPlayStateChanged(int ps)  { handlePlayState(ps); }
        });
    }

    // ── 持久化恢复 ──

    private void loadSettings() {
        mLongPressSpeed = mSettings.getLongPressSpeed();
        mLongPressSpeedText = mSettings.getLongPressSpeedText();
        setMute(mSettings.isMute());
        mHideProgress = mSettings.isHideProgress();
        mAutoRotate = mSettings.isAutoRotate();
        mScreenScaleType = mSettings.getScreenScale();
        setScreenScaleType(mScreenScaleType);
    }

    // ── 控制器 & 组件装配 ──

    private void setupController() {
        mController = new StarStandardVideoController(getContext());
        mBottomView = new StarBottomView(getContext());
        mEpisodeView = new StarEpisodeView(getContext());
        mSettingsView = new StarSettingsView(getContext());
        mTitleView = new StarTitleView(getContext());

        mController.addControlComponent(
                new StarCompleteView(getContext()), new StarErrorView(getContext()),
                buildPrepareView(), mTitleView);
        mController.addControlComponent(new StarGestureView(getContext()));
        mController.addControlComponent(mEpisodeView);
        mController.addControlComponent(mSettingsView);
        mController.addControlComponent(mBottomView);
        mController.setCanChangePosition(true);
        setVideoController(mController);
    }

    private StarPrepareView buildPrepareView() {
        StarPrepareView v = new StarPrepareView(getContext());
        v.setClickStart();
        return v;
    }

    // ── 回调连线 ──

    private void setupControllerCallbacks() {
        mBottomView.setShowBottomProgress(!mHideProgress);

        // ── 底部 ──
        mBottomView.setOnSpeedOptionSelectedListener((s, t) -> setPlaybackSpeed(s));
        mBottomView.setOnUpSetClickListener(v -> { if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v); });
        mBottomView.setOnDownSetClickListener(v -> { if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v); });
        mBottomView.setOnSelectClickListener(v -> mEpisodeView.show());
        mBottomView.setOnProgressListener(() -> {
            long dur = getDuration(), pos = getCurrentPosition();
            if (mSettings.getSkipEndProgress() > 0 && (dur - pos) <= mSettings.getSkipEndProgress() * 1000L) {
                seekTo(dur);
            }
        });

        // ── 选集 ──
        mEpisodeView.setOnEpisodeSelectListener((i, t) -> {
            mCurrentEpisodeIndex = i;
            if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(null);
        });

        // ── 标题栏 ──
        mTitleView.setOnPipClickListener(v -> { if (mOnWindowClickListener != null) mOnWindowClickListener.onClick(v); });
        mTitleView.setOnScreenClickListener(v -> { if (mOnScreenClickListener != null) mOnScreenClickListener.onClick(v); });
        mTitleView.setOnSettingsClickListener(v -> mSettingsView.show());

        // ── 设置面板 ──
        mSettingsView.setOnScaleChangeListener((t, tx) -> { setScreenScaleType(t); mScreenScaleType = t; mSettings.setScreenScale(t); });
        mSettingsView.setOnMuteChangeListener(m -> { setMute(m); mSettings.setMute(m); });
        mSettingsView.setOnHideProgressChangeListener(h -> { mHideProgress = h; mBottomView.setShowBottomProgress(!h); mSettings.setHideProgress(h); });
        mSettingsView.setOnAutoRotateChangeListener(a -> { mAutoRotate = a; mSettings.setAutoRotate(a); if (a) checkVideoOrientation(); });
        mSettingsView.setOnTimingOptionSelectedListener(this::applyTiming);
        mSettingsView.setOnLongPressSpeedChangeListener(s -> { mLongPressSpeed = s; mLongPressSpeedText = String.format(Locale.US, "%.1fX", s); mSettings.setLongPressSpeed(s); });
        mSettingsView.setOnSkipStartChangeListener((p, t) -> { mSettings.setSkipStartProgress(p); if (getCurrentPosition() < p * 1000L) seekTo(p * 1000L); });
        mSettingsView.setOnSkipEndChangeListener((p, t) -> mSettings.setSkipEndProgress(p));

        // ── 长按倍速 ──
        mController.setOnSpeedListener(() -> { setSpeed(mLongPressSpeed); mController.setSpeedLayoutVisibility(android.view.View.VISIBLE); boolean same = Math.abs(mLongPressSpeed - mCurrentSpeed) < 0.01f; mController.setSpeedText(same ? "已经是 " + mLongPressSpeedText + " 倍速" : mLongPressSpeedText + " 倍速中"); });
        mController.setOnCancelSpeedListener(() -> { setSpeed(mCurrentSpeed); mController.setSpeedLayoutVisibility(android.view.View.GONE); });
    }

    // ── 同步设置面板 UI ──

    private void syncSettingsView() {
        mSettingsView.setScaleType(mScreenScaleType);
        mSettingsView.setMuteChecked(isMute());
        mSettingsView.setTimingText(mTimingText);
        mSettingsView.setLongPressSpeed(mLongPressSpeed);
        mSettingsView.setHideProgressChecked(mHideProgress);
        mSettingsView.setAutoRotateChecked(mAutoRotate);
        int ss = mSettings.getSkipStartProgress(), se = mSettings.getSkipEndProgress();
        mSettingsView.setSkipStartTime(formatSkipTime(ss), ss);
        mSettingsView.setSkipEndTime(formatSkipTime(se), se);
    }

    // ═══════════════════════════════════════════
    // 播放器状态处理
    // ═══════════════════════════════════════════

    @Override protected void onDetachedFromWindow() { super.onDetachedFromWindow(); cancelTimer(); }

    private void handlePlayerState(int ps) {
        if (ps == PLAYER_FULL_SCREEN || ps == PLAYER_NORMAL) { if (mAutoRotate) checkVideoOrientation(); }
    }

    private void handlePlayState(int ps) {
        if (ps == STATE_PREPARING) {
            int ss = mSettings.getSkipStartProgress();
            if (ss > 0) seekTo(ss * 1000L);
        } else if (ps == STATE_PREPARED) {
            if (mAutoRotate) checkVideoOrientation();
        } else if (ps == STATE_PLAYBACK_COMPLETED) {
            if (TIMING_AFTER_CURRENT.equals(mTimingText)) { Activity a = getActivity(); if (a != null) a.finish(); }
            if (mSettings.isAutoNext() && mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(null);
        }
    }

    private void checkVideoOrientation() {
        Activity a = getActivity();
        if (a == null || !mAutoRotate) return;
        int vw = getVideoSize()[0], vh = getVideoSize()[1];
        if (vw <= 0 || vh <= 0) return;
        a.setRequestedOrientation(vw > vh ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    // ═══════════════════════════════════════════
    // 生命周期
    // ═══════════════════════════════════════════

    @Override public void setUrl(String url) { super.setUrl(url); mCurrentUrl = url; }
    @Override public void start() { super.start(); }
    public void addDefaultControlComponent(String title, boolean isLive) { mTitleView.setTitle(title); }

    // ═══════════════════════════════════════════
    // 定时关闭
    // ═══════════════════════════════════════════

    private void applyTiming(String option) {
        cancelTimer();
        mTimingText = option;
        mSettingsView.setTimingText(option);
        long ms = 0;
        if (TIMING_30_MIN.equals(option)) ms = 30 * 60_000L;
        else if (TIMING_60_MIN.equals(option)) ms = 60 * 60_000L;
        if (ms > 0) {
            mCountDownTimer = new CountDownTimer(ms, 1_000) {
                @Override public void onTick(long l) {}
                @Override public void onFinish() { Activity a = getActivity(); if (a != null) a.finish(); }
            }.start();
        }
    }

    private void cancelTimer() { if (mCountDownTimer != null) { mCountDownTimer.cancel(); mCountDownTimer = null; } }

    // ═══════════════════════════════════════════
    // 播放控制 SET/GET
    // ═══════════════════════════════════════════

    public void setPlaybackSpeed(float s) { mCurrentSpeed = s; mCurrentSpeedText = String.format(Locale.US, "%.1fX", s); setSpeed(s); mBottomView.setCurrentSpeed(s); }
    public float getPlaybackSpeed() { return mCurrentSpeed; }

    public void setLongPressSpeed(float s) { mLongPressSpeed = s; mLongPressSpeedText = String.format(Locale.US, "%.1fX", s); mSettingsView.setLongPressSpeed(s); mSettings.setLongPressSpeed(s); }
    public float getLongPressSpeed() { return mLongPressSpeed; }

    public void setMuted(boolean m) { setMute(m); mSettingsView.setMuteChecked(m); mSettings.setMute(m); }
    public boolean isMuted() { return isMute(); }

    public void setScreenScale(int t) { mScreenScaleType = t; setScreenScaleType(t); mSettingsView.setScaleType(t); mSettings.setScreenScale(t); }
    public int getScreenScale() { return mScreenScaleType; }

    public void setHideProgress(boolean h) { mHideProgress = h; mBottomView.setShowBottomProgress(!h); mSettingsView.setHideProgressChecked(h); mSettings.setHideProgress(h); }
    public boolean isHideProgress() { return mHideProgress; }

    public void setAutoRotate(boolean a) { mAutoRotate = a; mSettingsView.setAutoRotateChecked(a); mSettings.setAutoRotate(a); if (a) checkVideoOrientation(); }
    public boolean isAutoRotate() { return mAutoRotate; }

    public void setSkipStartTime(int s) { if (s < 0) s = 0; mSettingsView.setSkipStartTime(formatSkipTime(s), s); mSettings.setSkipStartProgress(s); }
    public int getSkipStartTime() { return mSettings.getSkipStartProgress(); }

    public void setSkipEndTime(int s) { if (s < 0) s = 0; mSettingsView.setSkipEndTime(formatSkipTime(s), s); mSettings.setSkipEndProgress(s); }
    public int getSkipEndTime() { return mSettings.getSkipEndProgress(); }

    public void setTimingOption(String o) { applyTiming(o); }
    public String getTimingOption() { return mTimingText; }

    public void setAutoNext(boolean a) { mSettings.setAutoNext(a); }
    public boolean isAutoNext() { return mSettings.isAutoNext(); }

    // ═══════════════════════════════════════════
    // 选集
    // ═══════════════════════════════════════════

    public void setEpisodes(java.util.List<String> e, int i) { mEpisodeView.setEpisodes(e, i); mCurrentEpisodeIndex = i; }
    public void setEpisodeAdapter(androidx.recyclerview.widget.RecyclerView.Adapter<?> a) { mEpisodeView.setAdapter(a); }
    public androidx.recyclerview.widget.RecyclerView.Adapter<?> getEpisodeAdapter() { return mEpisodeView.getAdapter(); }
    public androidx.recyclerview.widget.RecyclerView getEpisodeRecyclerView() { return mEpisodeView.getRecyclerView(); }
    public int getCurrentEpisodeIndex() { return mCurrentEpisodeIndex; }
    public void setCurrentEpisodeIndex(int i) { mCurrentEpisodeIndex = i; mEpisodeView.setCurrentIndex(i); }
    public void setOnEpisodeSelectListener(StarEpisodeView.OnEpisodeSelectListener l) { mEpisodeView.setOnEpisodeSelectListener(l); }

    public void showEpisodePanel() { mEpisodeView.show(); }
    public void hideEpisodePanel() { mEpisodeView.hide(); }
    public boolean isEpisodePanelShowing() { return mEpisodeView.isEpisodeShowing(); }
    public void setEpisodePanelTitle(String t) { mEpisodeView.setPanelTitle(t); }
    public void setEpisodePanelTitleColor(int c) { mEpisodeView.setPanelTitleColor(c); }
    public void setEpisodePanelTitleBarVisibility(int v) { mEpisodeView.setTitleBarVisibility(v); }
    public void setEpisodePanelCloseButtonVisibility(int v) { mEpisodeView.setCloseButtonVisibility(v); }
    public void setEpisodeCustomContentView(android.view.View v) { mEpisodeView.setCustomContentView(v); }
    public void setEpisodeCustomContentView(int l) { mEpisodeView.setCustomContentView(l); }
    public void restoreEpisodeDefaultContent() { mEpisodeView.restoreDefaultContent(); }
    public android.widget.FrameLayout getEpisodeContentContainer() { return mEpisodeView.getContentContainer(); }

    // ═══════════════════════════════════════════
    // 设置面板
    // ═══════════════════════════════════════════

    public void showSettingsPanel() { mSettingsView.show(); }
    public void hideSettingsPanel() { mSettingsView.hide(); }
    public boolean isSettingsPanelShowing() { return mSettingsView.isSettingsShowing(); }

    // ═══════════════════════════════════════════
    // 按钮可见性 — 统一入口
    // ═══════════════════════════════════════════

    /**
     * 设置按钮可见性（全局）。
     * <pre>{@code
     * player.setButtonVisible(PlayerButton.SELECT, View.GONE);
     * player.setButtonVisible(PlayerButton.SPEED, View.VISIBLE);
     * }</pre>
     */
    public void setButtonVisible(PlayerButton btn, int visibility) {
        setButtonVisible(btn, visibility, visibility);
    }

    /**
     * 设置按钮可见性（区分非全屏 / 全屏）。
     * <pre>{@code
     * player.setButtonVisible(PlayerButton.SPEED, View.GONE, View.VISIBLE); // normal=gone, fullscreen=visible
     * }</pre>
     */
    public void setButtonVisible(PlayerButton btn, int normalVis, int fullscreenVis) {
        switch (btn) {
            // ── 底部 ──
            case SELECT:              mBottomView.setSelectButtonVisibilityNormal(normalVis);         mBottomView.setSelectButtonVisibilityFullscreen(fullscreenVis);         break;
            case SPEED:               mBottomView.setSpeedButtonVisibilityNormal(normalVis);           mBottomView.setSpeedButtonVisibilityFullscreen(fullscreenVis);           break;
            case PREV:                mBottomView.setPreviousButtonVisibilityNormal(normalVis);        mBottomView.setPreviousButtonVisibilityFullscreen(fullscreenVis);        break;
            case NEXT:                mBottomView.setNextButtonVisibilityNormal(normalVis);            mBottomView.setNextButtonVisibilityFullscreen(fullscreenVis);            break;
            case FULLSCREEN:          mBottomView.setFullscreenButtonVisibilityNormal(normalVis);      mBottomView.setFullscreenButtonVisibilityFullscreen(fullscreenVis);      break;
            case PORTRAIT_FULLSCREEN: mBottomView.setFullscreenPortraitButtonVisibilityNormal(normalVis);mBottomView.setFullscreenPortraitButtonVisibilityFullscreen(fullscreenVis);break;
            // ── 顶部 ──
            case BACK:      mTitleView.setBackButtonVisibility(normalVis);       break;
            case PIP:       mTitleView.setPipButtonVisibility(normalVis);        break;
            case CAST:      mTitleView.setScreenButtonVisibility(normalVis);     break;
            case SETTINGS:  mTitleView.setSettingsButtonVisibility(normalVis);   break;
            case SYS_TIME:  mTitleView.setSysTimeVisibility(normalVis);          break;
        }
    }

    public void bottomBar(BottomBarConfig.Block<BottomBarConfig> b) {
        BottomBarConfig c = new BottomBarConfig();
        b.configure(c);
        mBottomView.applyConfig(c);
    }

    // ── 底部按钮（旧 API，保留兼容）──
    public void setVisibilityBottom(int sel, int spd, int prev, int next) { mBottomView.setBottomButtonsVisibility(sel, spd, prev, next); }
    public void setVisibilityBottom(int sel, int spd, int prev, int next, int full, int pf) { mBottomView.setBottomButtonsVisibility(sel, spd, prev, next, full, pf); }
    public void setVisibilityBottomNormal(int sel, int spd, int prev, int next) { mBottomView.setBottomButtonsVisibilityNormal(sel, spd, prev, next); }
    public void setVisibilityBottomNormal(int sel, int spd, int prev, int next, int full, int pf) { mBottomView.setBottomButtonsVisibilityNormal(sel, spd, prev, next, full, pf); }
    public void setVisibilityBottomFullscreen(int sel, int spd, int prev, int next) { mBottomView.setBottomButtonsVisibilityFullscreen(sel, spd, prev, next); }
    public void setVisibilityBottomFullscreen(int sel, int spd, int prev, int next, int full, int pf) { mBottomView.setBottomButtonsVisibilityFullscreen(sel, spd, prev, next, full, pf); }
    public void setVisibilityBottomAll(int sn, int spn, int pn, int nn, int fn, int pfn, int sf, int spf, int pf, int nf, int ff, int pff) { mBottomView.setBottomButtonsVisibilityAll(sn, spn, pn, nn, fn, pfn, sf, spf, pf, nf, ff, pff); }

    // ── 单按钮：选集 ──
    public void setSelectButtonVisibility(int v) { mBottomView.setSelectButtonVisibility(v); }
    public void setSelectButtonVisibilityNormal(int v) { mBottomView.setSelectButtonVisibilityNormal(v); }
    public void setSelectButtonVisibilityFullscreen(int v) { mBottomView.setSelectButtonVisibilityFullscreen(v); }

    // ── 单按钮：倍速 ──
    public void setSpeedButtonVisibility(int v) { mBottomView.setSpeedButtonVisibility(v); }
    public void setSpeedButtonVisibilityNormal(int v) { mBottomView.setSpeedButtonVisibilityNormal(v); }
    public void setSpeedButtonVisibilityFullscreen(int v) { mBottomView.setSpeedButtonVisibilityFullscreen(v); }

    // ── 单按钮：上一集 ──
    public void setPreviousButtonVisibility(int v) { mBottomView.setPreviousButtonVisibility(v); }
    public void setPreviousButtonVisibilityNormal(int v) { mBottomView.setPreviousButtonVisibilityNormal(v); }
    public void setPreviousButtonVisibilityFullscreen(int v) { mBottomView.setPreviousButtonVisibilityFullscreen(v); }

    // ── 单按钮：下一集 ──
    public void setNextButtonVisibility(int v) { mBottomView.setNextButtonVisibility(v); }
    public void setNextButtonVisibilityNormal(int v) { mBottomView.setNextButtonVisibilityNormal(v); }
    public void setNextButtonVisibilityFullscreen(int v) { mBottomView.setNextButtonVisibilityFullscreen(v); }

    // ── 单按钮：全屏 ──
    public void setFullscreenButtonVisibility(int v) { mBottomView.setFullscreenButtonVisibility(v); }
    public void setFullscreenButtonVisibilityNormal(int v) { mBottomView.setFullscreenButtonVisibilityNormal(v); }
    public void setFullscreenButtonVisibilityFullscreen(int v) { mBottomView.setFullscreenButtonVisibilityFullscreen(v); }

    // ── 单按钮：竖屏全屏 ──
    public void setFullscreenPortraitButtonVisibility(int v) { mBottomView.setFullscreenPortraitButtonVisibility(v); }
    public void setFullscreenPortraitButtonVisibilityNormal(int v) { mBottomView.setFullscreenPortraitButtonVisibilityNormal(v); }
    public void setFullscreenPortraitButtonVisibilityFullscreen(int v) { mBottomView.setFullscreenPortraitButtonVisibilityFullscreen(v); }

    // ── 竖屏全屏点击 ──
    public void setOnFullscreenPortraitClickListener(StarBottomView.OnFullscreenPortraitClickListener l) { mBottomView.setOnFullscreenPortraitClickListener(l); }

    // ═══════════════════════════════════════════
    // 顶部按钮可见性
    // ═══════════════════════════════════════════

    public void setTitleButtonsVisibility(int back, int pip, int screen, int settings) { mTitleView.setTitleButtonsVisibility(back, pip, screen, settings); }
    public void setBackButtonVisibility(int v)    { mTitleView.setBackButtonVisibility(v); }
    public void setPipButtonVisibility(int v)     { mTitleView.setPipButtonVisibility(v); }
    public void setScreenButtonVisibility(int v)  { mTitleView.setScreenButtonVisibility(v); }
    public void setSettingsButtonVisibility(int v){ mTitleView.setSettingsButtonVisibility(v); }
    public void setSysTimeVisibility(int v)       { mTitleView.setSysTimeVisibility(v); }

    // ═══════════════════════════════════════════
    // 颜色
    // ═══════════════════════════════════════════

    /**
     * 一键设色。
     * <pre>{@code
     * player.setColor(PlayerColor.TITLE_ICON, Color.WHITE);   // 标题栏所有按钮图标
     * player.setColor(PlayerColor.BOTTOM_ICON, Color.WHITE);  // 底部栏所有按钮图标
     * }</pre>
     */
    public void setColor(PlayerColor item, int color) {
        if (item == PlayerColor.TITLE_ICON)  mTitleView.setButtonIconTint(color);
        if (item == PlayerColor.BOTTOM_ICON) mBottomView.setButtonIconTint(color);
    }

    // ── 旧 API（保留兼容）──
    public void setTitleTextColor(int c)          { mTitleView.setTitleTextColor(c); }
    public void setSysTimeTextColor(int c)        { mTitleView.setSysTimeTextColor(c); }
    public void setButtonIconTint(int c)           { setColor(PlayerColor.TITLE_ICON, c); }
    public void setTitleContainerBackground(int c) { mTitleView.setTitleContainerBackground(c); }
    public void setTimeTextColor(int c)            { mBottomView.setTimeTextColor(c); }
    public void setBottomButtonIconTint(int c)     { setColor(PlayerColor.BOTTOM_ICON, c); }
    public void setBottomContainerBackground(int c){ mBottomView.setBottomContainerBackground(c); }

    // ═══════════════════════════════════════════
    // 监听设置
    // ═══════════════════════════════════════════

    public void setOnWindowClickListener(OnWindowClickListener l)   { mOnWindowClickListener = l; }
    public void setOnScreenClickListener(OnScreenClickListener l)   { mOnScreenClickListener = l; }
    public void setOnSelectClickListener(OnSelectClickListener l)   { mOnSelectClickListener = l; }
    public void setOnUpSetClickListener(OnUpSetClickListener l)     { mOnUpSetClickListener = l; }
    public void setOnDownSetClickListener(OnDownSetClickListener l) { mOnDownSetClickListener = l; }

    // ── 工具 ──

    private String formatSkipTime(int seconds) {
        int min = seconds / 60, sec = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }
}

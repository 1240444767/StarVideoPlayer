package com.star.play;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.datasource.DefaultHttpDataSource;

import com.star.play.controller.PlayerConstants;
import com.star.play.controller.PlayerController;
import com.star.play.controller.StarBottomView;
import com.star.play.controller.StarCompleteView;
import com.star.play.controller.StarEpisodeView;
import com.star.play.controller.StarErrorView;
import com.star.play.controller.StarGestureView;
import com.star.play.controller.StarPrepareView;
import com.star.play.controller.StarSettingsView;
import com.star.play.controller.StarTitleView;

import java.util.Locale;

public class StarVideoPlayer extends FrameLayout {

    // ── 常量 ──

    private static final String TIMING_OFF = "不启用";
    private static final String TIMING_AFTER_CURRENT = "播完当前";
    private static final String TIMING_30_MIN = "30分钟";
    private static final String TIMING_60_MIN = "60分钟";

    // ── 内核 ──

    private ExoPlayer mExoPlayer;
    private TextureView mTextureView;

    // ── 状态 ──

    private int mCurrentPlayState = PlayerConstants.STATE_IDLE;
    private int mCurrentPlayerState = PlayerConstants.PLAYER_NORMAL;
    private boolean mIsFullScreen;
    private boolean mIsMute;
    private String mUrl;
    private int mVideoWidth, mVideoHeight;
    private int mScreenScaleType = PlayerConstants.SCREEN_SCALE_DEFAULT;
    private float mSpeed = 1.0f;

    // ── Handler ──

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mProgressRunnable = new Runnable() {
        @Override public void run() {
            if (mExoPlayer != null && mExoPlayer.isPlaying()) {
                long dur = mExoPlayer.getDuration();
                long pos = mExoPlayer.getCurrentPosition();
                mController.notifyProgress((int) dur, (int) pos);
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    // ── 控制器 ──

    PlayerController mController;
    private StarPlayerSettings mSettings;
    private StarBottomView mBottomView;
    private StarEpisodeView mEpisodeView;
    private StarSettingsView mSettingsView;
    private StarTitleView mTitleView;

    private float mCurrentSpeed = 1.0f;
    private String mCurrentSpeedText = "1.0x";
    private float mLongPressSpeed = 3.0f;
    private String mLongPressSpeedText = "3.0x";
    private String mTimingText = TIMING_OFF;
    private CountDownTimer mCountDownTimer;
    private int mCurrentEpisodeIndex;
    private boolean mShowBufferedProgress = true;
    private boolean mHideProgress;
    private boolean mAutoRotate;

    // ── Activity 引用 ──

    private Activity mActivity;

    // ── 监听接口 ──

    public interface OnWindowClickListener  { void onClick(View v); }
    public interface OnScreenClickListener  { void onClick(View v); }
    public interface OnSelectClickListener  { void onClick(View v); }
    public interface OnUpSetClickListener   { void onClick(View v); }
    public interface OnDownSetClickListener { void onClick(View v); }

    private OnWindowClickListener  mOnWindowClickListener;
    private OnScreenClickListener  mOnScreenClickListener;
    private OnSelectClickListener  mOnSelectClickListener;
    private OnUpSetClickListener   mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;

    // ═══════════════════════════════════════════
    // 构造
    // ═══════════════════════════════════════════

    public StarVideoPlayer(@NonNull Context c) { this(c, null); }
    public StarVideoPlayer(@NonNull Context c, @Nullable AttributeSet a) { this(c, a, 0); }

    public StarVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundColor(Color.BLACK);
        initTextureView();
        initPlayer();
        mSettings = new StarPlayerSettings(context);
        loadSettings();
        mController = new PlayerController(this);
        setupController();
        setupControllerCallbacks();
        syncSettingsView();
    }

    // ── TextureView ──

    private void initTextureView() {
        mTextureView = new TextureView(getContext());
        mTextureView.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTextureView.setKeepScreenOn(true);
        addView(mTextureView);
    }

    // ── ExoPlayer 初始化 ──

    private void initPlayer() {
        mExoPlayer = new ExoPlayer.Builder(getContext())
                .setLoadControl(new EagerLoadControl())
                .build();
        mExoPlayer.setVideoTextureView(mTextureView);
        mExoPlayer.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        setPlayState(PlayerConstants.STATE_BUFFERING);
                        break;
                    case Player.STATE_READY:
                        setPlayState(PlayerConstants.STATE_PREPARED);
                        setPlayState(PlayerConstants.STATE_BUFFERED);
                        setPlayState(PlayerConstants.STATE_PLAYING);
                        break;
                    case Player.STATE_ENDED:
                        setPlayState(PlayerConstants.STATE_PLAYBACK_COMPLETED);
                        if (TIMING_AFTER_CURRENT.equals(mTimingText)) {
                            Activity a = getPlayerActivity();
                            if (a != null) a.finish();
                        }
                        if (mSettings.isAutoNext() && mOnDownSetClickListener != null) {
                            mOnDownSetClickListener.onClick(null);
                        }
                        break;
                    case Player.STATE_IDLE:
                        setPlayState(PlayerConstants.STATE_IDLE);
                        break;
                }
            }
            @Override public void onPlayerError(@NonNull PlaybackException e) {
                setPlayState(PlayerConstants.STATE_ERROR);
            }
            @Override public void onVideoSizeChanged(int w, int h) {
                mVideoWidth = w; mVideoHeight = h;
                if (mAutoRotate) checkVideoOrientation();
            }
            @Override public void onIsPlayingChanged(boolean playing) {
                if (playing) {
                    setPlayState(PlayerConstants.STATE_PLAYING);
                    mHandler.removeCallbacks(mProgressRunnable);
                    mHandler.post(mProgressRunnable);
                } else {
                    setPlayState(PlayerConstants.STATE_PAUSED);
                    mHandler.removeCallbacks(mProgressRunnable);
                }
            }
        });
    }

    // ── 持久化恢复 ──

    private void loadSettings() {
        mLongPressSpeed = mSettings.getLongPressSpeed();
        mLongPressSpeedText = mSettings.getLongPressSpeedText();
        setMuted(mSettings.isMute());
        mHideProgress = mSettings.isHideProgress();
        mShowBufferedProgress = mSettings.isBufferedProgressEnabled();
        mAutoRotate = mSettings.isAutoRotate();
        mScreenScaleType = mSettings.getScreenScale();
        setScreenScaleType(mScreenScaleType);
    }

    // ── 控制器装配 ──

    private void setupController() {
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
    }

    private StarPrepareView buildPrepareView() {
        StarPrepareView v = new StarPrepareView(getContext());
        v.setClickStart();
        return v;
    }

    // ── 回调连线 ──

    private void setupControllerCallbacks() {
        mBottomView.setShowBottomProgress(!mHideProgress);

        mBottomView.setOnSpeedOptionSelectedListener((s, t) -> setPlaybackSpeed(s));
        mBottomView.setOnUpSetClickListener(v -> { if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v); });
        mBottomView.setOnDownSetClickListener(v -> { if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v); });
        mBottomView.setOnSelectClickListener(v -> mEpisodeView.show());
        mBottomView.setOnProgressListener(() -> {
            long dur = getDuration(), pos = getCurrentPosition();
            if (mSettings.getSkipEndProgress() > 0 && (dur - pos) <= mSettings.getSkipEndProgress() * 1000L) {
                seekTo(dur);
            }
            if (mShowBufferedProgress) mBottomView.setBufferedProgress(getBufferedPercentage());
        });

        mEpisodeView.setOnEpisodeSelectListener((i, t) -> {
            mCurrentEpisodeIndex = i;
            if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(null);
        });

        mTitleView.setOnPipClickListener(v -> { if (mOnWindowClickListener != null) mOnWindowClickListener.onClick(v); });
        mTitleView.setOnScreenClickListener(v -> { if (mOnScreenClickListener != null) mOnScreenClickListener.onClick(v); });
        mTitleView.setOnSettingsClickListener(v -> mSettingsView.show());

        mSettingsView.setOnScaleChangeListener((t, tx) -> { setScreenScaleType(t); mScreenScaleType = t; mSettings.setScreenScale(t); });
        mSettingsView.setOnMuteChangeListener(m -> { setMuted(m); mSettings.setMute(m); });
        mSettingsView.setOnHideProgressChangeListener(h -> { mHideProgress = h; mBottomView.setShowBottomProgress(!h); mSettings.setHideProgress(h); });
        mSettingsView.setOnAutoRotateChangeListener(a -> { mAutoRotate = a; mSettings.setAutoRotate(a); if (a) checkVideoOrientation(); });
        mSettingsView.setOnBufferedProgressChangeListener(e -> { mShowBufferedProgress = e; mSettings.setBufferedProgressEnabled(e); if (!e) mBottomView.clearBufferedProgress(); });
        mSettingsView.setOnTimingOptionSelectedListener(this::applyTiming);
        mSettingsView.setOnLongPressSpeedChangeListener(s -> { mLongPressSpeed = s; mLongPressSpeedText = String.format(Locale.US, "%.1fX", s); mSettings.setLongPressSpeed(s); });
        mSettingsView.setOnSkipStartChangeListener((p, t) -> { mSettings.setSkipStartProgress(p); if (getCurrentPosition() < p * 1000L) seekTo(p * 1000L); });
        mSettingsView.setOnSkipEndChangeListener((p, t) -> mSettings.setSkipEndProgress(p));
    }

    private void syncSettingsView() {
        mSettingsView.setScaleType(mScreenScaleType);
        mSettingsView.setMuteChecked(isMute());
        mSettingsView.setTimingText(mTimingText);
        mSettingsView.setLongPressSpeed(mLongPressSpeed);
        mSettingsView.setHideProgressChecked(mHideProgress);
        mSettingsView.setAutoRotateChecked(mAutoRotate);
        mSettingsView.setBufferedProgressChecked(mShowBufferedProgress);
        int ss = mSettings.getSkipStartProgress(), se = mSettings.getSkipEndProgress();
        mSettingsView.setSkipStartTime(formatSkipTime(ss), ss);
        mSettingsView.setSkipEndTime(formatSkipTime(se), se);
    }

    // ═══════════════════════════════════════════
    // 状态机
    // ═══════════════════════════════════════════

    private void setPlayState(int state) {
        if (mCurrentPlayState == state) return;
        mCurrentPlayState = state;
        mController.notifyPlayState(state);

        if (state == PlayerConstants.STATE_PREPARING) {
            int ss = mSettings.getSkipStartProgress();
            if (ss > 0) seekTo(ss * 1000L);
        } else if (state == PlayerConstants.STATE_PREPARED) {
            if (mAutoRotate) checkVideoOrientation();
        }
    }

    private void setPlayerState(int state) {
        mCurrentPlayerState = state;
        mController.notifyPlayerState(state);
    }

    // ═══════════════════════════════════════════
    // 公开 API
    // ═══════════════════════════════════════════

    public void setUrl(String url) {
        mUrl = url;
        if (url != null && !url.isEmpty()) {
            MediaItem mediaItem = MediaItem.fromUri(url);
            DefaultHttpDataSource.Factory dsFactory = new DefaultHttpDataSource.Factory();
            MediaSource source;
            if (url.contains(".m3u8")) {
                source = new HlsMediaSource.Factory(dsFactory).createMediaSource(mediaItem);
            } else {
                source = new ProgressiveMediaSource.Factory(dsFactory).createMediaSource(mediaItem);
            }
            mExoPlayer.setMediaSource(source);
        }
    }

    public void start() {
        setPlayState(PlayerConstants.STATE_PREPARING);
        mExoPlayer.prepare();
        mExoPlayer.play();
    }

    public void pause() { mExoPlayer.pause(); }
    public void resume() { mExoPlayer.play(); }

    public void release() {
        mHandler.removeCallbacks(mProgressRunnable);
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    public long getDuration() { return mExoPlayer != null ? mExoPlayer.getDuration() : 0; }
    public long getCurrentPosition() { return mExoPlayer != null ? mExoPlayer.getCurrentPosition() : 0; }
    public void seekTo(long pos) { if (mExoPlayer != null) mExoPlayer.seekTo(pos); }
    public boolean isPlaying() { return mExoPlayer != null && mExoPlayer.isPlaying(); }
    public int getBufferedPercentage() { return mExoPlayer != null ? mExoPlayer.getBufferedPercentage() : 0; }
    public void setSpeed(float speed) { mSpeed = speed; if (mExoPlayer != null) mExoPlayer.setPlaybackSpeed(speed); }
    public float getSpeed() { return mSpeed; }
    public void setMute(boolean mute) { mIsMute = mute; if (mExoPlayer != null) mExoPlayer.setVolume(mute ? 0f : 1f); }
    public boolean isMute() { return mIsMute; }
    public int[] getVideoSize() { return new int[]{mVideoWidth, mVideoHeight}; }

    public void setScreenScaleType(int type) {
        mScreenScaleType = type;
        if (mTextureView == null) return;
        ViewGroup.LayoutParams lp = mTextureView.getLayoutParams();
        float vw = mVideoWidth, vh = mVideoHeight;
        if (vw <= 0 || vh <= 0) { vw = 16; vh = 9; }
        float viewW = getWidth(), viewH = getHeight();
        if (viewW <= 0) viewW = getContext().getResources().getDisplayMetrics().widthPixels;
        switch (type) {
            case PlayerConstants.SCREEN_SCALE_16_9:
                viewH = viewW * 9f / 16f; break;
            case PlayerConstants.SCREEN_SCALE_4_3:
                viewH = viewW * 3f / 4f; break;
            case PlayerConstants.SCREEN_SCALE_MATCH_PARENT:
                lp.width = LayoutParams.MATCH_PARENT;
                lp.height = LayoutParams.MATCH_PARENT;
                mTextureView.setLayoutParams(lp);
                return;
            case PlayerConstants.SCREEN_SCALE_ORIGINAL:
                lp.width = (int) vw; lp.height = (int) vh;
                mTextureView.setLayoutParams(lp);
                return;
            case PlayerConstants.SCREEN_SCALE_CENTER_CROP:
                lp.width = (int) viewW;
                lp.height = (int) viewH;
                mTextureView.setLayoutParams(lp);
                return;
            default:
                float scale = Math.min(viewW / vw, viewH / vh);
                lp.width = (int) (vw * scale);
                lp.height = (int) (vh * scale);
                mTextureView.setLayoutParams(lp);
                return;
        }
        lp.width = (int) viewW;
        lp.height = (int) viewH;
        mTextureView.setLayoutParams(lp);
    }

    // ── 全屏 ──

    public void startFullScreen() {
        mIsFullScreen = true;
        setPlayerState(PlayerConstants.PLAYER_FULL_SCREEN);
        Activity a = getPlayerActivity();
        if (a != null) {
            a.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    public void stopFullScreen() {
        mIsFullScreen = false;
        setPlayerState(PlayerConstants.PLAYER_NORMAL);
        Activity a = getPlayerActivity();
        if (a != null) {
            a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public boolean isFullScreen() { return mIsFullScreen; }

    // ── 画中画 / 小窗 ──

    public void startTinyScreen() {}
    public void stopTinyScreen() {}
    public boolean isTinyScreen() { return false; }

    // ── onBackPressed ──

    public boolean onBackPressed() {
        if (mController.isLocked()) {
            mController.show();
            return true;
        }
        if (mIsFullScreen) {
            stopFullScreen();
            return true;
        }
        return false;
    }

    // ── Activity ──

    public Activity getPlayerActivity() {
        if (mActivity != null) return mActivity;
        Context c = getContext();
        while (c instanceof android.content.ContextWrapper) {
            if (c instanceof Activity) { mActivity = (Activity) c; return mActivity; }
            c = ((android.content.ContextWrapper) c).getBaseContext();
        }
        return null;
    }

    private void checkVideoOrientation() {
        Activity a = getPlayerActivity();
        if (a == null || !mAutoRotate) return;
        if (mVideoWidth <= 0 || mVideoHeight <= 0) return;
        a.setRequestedOrientation(mVideoWidth > mVideoHeight
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    // ═══════════════════════════════════════════
    // addDefaultControlComponent
    // ═══════════════════════════════════════════

    public void addDefaultControlComponent(String title, boolean isLive) {
        mTitleView.setTitle(title);
    }

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
                @Override public void onFinish() { Activity a = getPlayerActivity(); if (a != null) a.finish(); }
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
    public void setEpisodeCustomContentView(View v) { mEpisodeView.setCustomContentView(v); }
    public void setEpisodeCustomContentView(int l) { mEpisodeView.setCustomContentView(l); }
    public void restoreEpisodeDefaultContent() { mEpisodeView.restoreDefaultContent(); }
    public android.widget.FrameLayout getEpisodeContentContainer() { return mEpisodeView.getContentContainer(); }
    public void showSettingsPanel() { mSettingsView.show(); }
    public void hideSettingsPanel() { mSettingsView.hide(); }
    public boolean isSettingsPanelShowing() { return mSettingsView.isSettingsShowing(); }

    // ═══════════════════════════════════════════
    // 按钮可见性
    // ═══════════════════════════════════════════

    public void setButtonVisible(PlayerButton btn, int visibility) { setButtonVisible(btn, visibility, visibility); }

    public void setButtonVisible(PlayerButton btn, int normalVis, int fullscreenVis) {
        switch (btn) {
            case SELECT:              mBottomView.setSelectButtonVisibilityNormal(normalVis);         mBottomView.setSelectButtonVisibilityFullscreen(fullscreenVis);         break;
            case SPEED:               mBottomView.setSpeedButtonVisibilityNormal(normalVis);           mBottomView.setSpeedButtonVisibilityFullscreen(fullscreenVis);           break;
            case PREV:                mBottomView.setPreviousButtonVisibilityNormal(normalVis);        mBottomView.setPreviousButtonVisibilityFullscreen(fullscreenVis);        break;
            case NEXT:                mBottomView.setNextButtonVisibilityNormal(normalVis);            mBottomView.setNextButtonVisibilityFullscreen(fullscreenVis);            break;
            case FULLSCREEN:          mBottomView.setFullscreenButtonVisibilityNormal(normalVis);      mBottomView.setFullscreenButtonVisibilityFullscreen(fullscreenVis);      break;
            case PORTRAIT_FULLSCREEN: mBottomView.setFullscreenPortraitButtonVisibilityNormal(normalVis);mBottomView.setFullscreenPortraitButtonVisibilityFullscreen(fullscreenVis);break;
            case BACK:      mTitleView.setBackButtonVisibility(normalVis);       break;
            case PIP:       mTitleView.setPipButtonVisibility(normalVis);        break;
            case CAST:      mTitleView.setScreenButtonVisibility(normalVis);     break;
            case SETTINGS:  mTitleView.setSettingsButtonVisibility(normalVis);   break;
            case SYS_TIME:  mTitleView.setSysTimeVisibility(normalVis);          break;
        }
    }

    // ── 旧 API 兼容 ──

    public void setVisibilityBottom(int sel, int spd, int prev, int next) { mBottomView.setBottomButtonsVisibility(sel, spd, prev, next); }
    public void setVisibilityBottom(int sel, int spd, int prev, int next, int full, int pf) { mBottomView.setBottomButtonsVisibility(sel, spd, prev, next, full, pf); }
    public void setVisibilityBottomNormal(int sel, int spd, int prev, int next) { mBottomView.setBottomButtonsVisibilityNormal(sel, spd, prev, next); }
    public void setVisibilityBottomNormal(int sel, int spd, int prev, int next, int full, int pf) { mBottomView.setBottomButtonsVisibilityNormal(sel, spd, prev, next, full, pf); }
    public void setVisibilityBottomFullscreen(int sel, int spd, int prev, int next) { mBottomView.setBottomButtonsVisibilityFullscreen(sel, spd, prev, next); }
    public void setVisibilityBottomFullscreen(int sel, int spd, int prev, int next, int full, int pf) { mBottomView.setBottomButtonsVisibilityFullscreen(sel, spd, prev, next, full, pf); }
    public void setVisibilityBottomAll(int sn, int spn, int pn, int nn, int fn, int pfn, int sf, int spf, int pf, int nf, int ff, int pff) { mBottomView.setBottomButtonsVisibilityAll(sn, spn, pn, nn, fn, pfn, sf, spf, pf, nf, ff, pff); }
    public void setSelectButtonVisibility(int v) { mBottomView.setSelectButtonVisibility(v); }
    public void setSelectButtonVisibilityNormal(int v) { mBottomView.setSelectButtonVisibilityNormal(v); }
    public void setSelectButtonVisibilityFullscreen(int v) { mBottomView.setSelectButtonVisibilityFullscreen(v); }
    public void setSpeedButtonVisibility(int v) { mBottomView.setSpeedButtonVisibility(v); }
    public void setSpeedButtonVisibilityNormal(int v) { mBottomView.setSpeedButtonVisibilityNormal(v); }
    public void setSpeedButtonVisibilityFullscreen(int v) { mBottomView.setSpeedButtonVisibilityFullscreen(v); }
    public void setPreviousButtonVisibility(int v) { mBottomView.setPreviousButtonVisibility(v); }
    public void setPreviousButtonVisibilityNormal(int v) { mBottomView.setPreviousButtonVisibilityNormal(v); }
    public void setPreviousButtonVisibilityFullscreen(int v) { mBottomView.setPreviousButtonVisibilityFullscreen(v); }
    public void setNextButtonVisibility(int v) { mBottomView.setNextButtonVisibility(v); }
    public void setNextButtonVisibilityNormal(int v) { mBottomView.setNextButtonVisibilityNormal(v); }
    public void setNextButtonVisibilityFullscreen(int v) { mBottomView.setNextButtonVisibilityFullscreen(v); }
    public void setFullscreenButtonVisibility(int v) { mBottomView.setFullscreenButtonVisibility(v); }
    public void setFullscreenButtonVisibilityNormal(int v) { mBottomView.setFullscreenButtonVisibilityNormal(v); }
    public void setFullscreenButtonVisibilityFullscreen(int v) { mBottomView.setFullscreenButtonVisibilityFullscreen(v); }
    public void setFullscreenPortraitButtonVisibility(int v) { mBottomView.setFullscreenPortraitButtonVisibility(v); }
    public void setFullscreenPortraitButtonVisibilityNormal(int v) { mBottomView.setFullscreenPortraitButtonVisibilityNormal(v); }
    public void setFullscreenPortraitButtonVisibilityFullscreen(int v) { mBottomView.setFullscreenPortraitButtonVisibilityFullscreen(v); }
    public void setOnFullscreenPortraitClickListener(StarBottomView.OnFullscreenPortraitClickListener l) { mBottomView.setOnFullscreenPortraitClickListener(l); }
    public void setTitleButtonsVisibility(int back, int pip, int screen, int settings) { mTitleView.setTitleButtonsVisibility(back, pip, screen, settings); }
    public void setBackButtonVisibility(int v) { mTitleView.setBackButtonVisibility(v); }
    public void setPipButtonVisibility(int v) { mTitleView.setPipButtonVisibility(v); }
    public void setScreenButtonVisibility(int v) { mTitleView.setScreenButtonVisibility(v); }
    public void setSettingsButtonVisibility(int v) { mTitleView.setSettingsButtonVisibility(v); }
    public void setSysTimeVisibility(int v) { mTitleView.setSysTimeVisibility(v); }

    // ── 颜色 ──

    public void setColor(PlayerColor item, int color) {
        if (item == PlayerColor.TITLE_ICON) mTitleView.setButtonIconTint(color);
        if (item == PlayerColor.BOTTOM_ICON) mBottomView.setButtonIconTint(color);
    }

    public void setTitleTextColor(int c) { mTitleView.setTitleTextColor(c); }
    public void setSysTimeTextColor(int c) { mTitleView.setSysTimeTextColor(c); }
    public void setButtonIconTint(int c) { setColor(PlayerColor.TITLE_ICON, c); }
    public void setTitleContainerBackground(int c) { mTitleView.setTitleContainerBackground(c); }
    public void setTimeTextColor(int c) { mBottomView.setTimeTextColor(c); }
    public void setBottomButtonIconTint(int c) { setColor(PlayerColor.BOTTOM_ICON, c); }
    public void setBottomContainerBackground(int c) { mBottomView.setBottomContainerBackground(c); }

    // ── 监听 ──

    public void setOnWindowClickListener(OnWindowClickListener l) { mOnWindowClickListener = l; }
    public void setOnScreenClickListener(OnScreenClickListener l) { mOnScreenClickListener = l; }
    public void setOnSelectClickListener(OnSelectClickListener l) { mOnSelectClickListener = l; }
    public void setOnUpSetClickListener(OnUpSetClickListener l) { mOnUpSetClickListener = l; }
    public void setOnDownSetClickListener(OnDownSetClickListener l) { mOnDownSetClickListener = l; }

    // ── 生命周期 ──

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelTimer();
        mHandler.removeCallbacks(mProgressRunnable);
    }

    // ── 工具 ──

    private String formatSkipTime(int seconds) {
        int min = seconds / 60, sec = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }

    // ═══════════════════════════════════════════
    // 激进缓冲 LoadControl
    // ═══════════════════════════════════════════

    @SuppressWarnings("deprecation")
    private static class EagerLoadControl extends DefaultLoadControl {
        EagerLoadControl() {
            super(
                new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MIN_BUFFER_FOR_LOCAL_PLAYBACK_MS,
                3_600_000,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_FOR_LOCAL_PLAYBACK_MS,
                50_000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_FOR_LOCAL_PLAYBACK_MS,
                50_000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_FOR_LOCAL_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
                DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
                DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS_FOR_LOCAL_PLAYBACK,
                DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS,
                DefaultLoadControl.DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME,
                com.google.common.collect.ImmutableMap.of()
            );
        }

        @Override
        public boolean shouldContinueLoading(long playbackPositionUs,
                                              long bufferedDurationUs,
                                              float playbackSpeed) {
            if (bufferedDurationUs < 3_600_000_000L) return true;
            return super.shouldContinueLoading(playbackPositionUs, bufferedDurationUs, playbackSpeed);
        }
    }
}

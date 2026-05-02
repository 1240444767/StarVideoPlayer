package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class StarBottomView extends FrameLayout implements IControlComponent {

    // ── 外部回调接口 ──
    public interface OnSelectClickListener       { void onClick(View view); }
    public interface OnSpeedOptionSelectedListener{ void onSpeedOptionSelected(float speed, String speedText); }
    public interface OnUpSetClickListener        { void onClick(View view); }
    public interface OnDownSetClickListener      { void onClick(View view); }
    public interface OnProgressListener          { void onProgress(); }
    public interface OnFullscreenPortraitClickListener { void onClick(View view); }

    // ── State ──
    private ControlWrapper mControlWrapper;
    private boolean mIsDragging;
    private boolean mIsShowBottomProgress = true;
    private boolean mIsFullScreen;
    private float mCurrentSpeed = 1.0f;

    // ── 容器 ──
    private View mContainerNormal, mContainerFullscreen;
    private TextView mTimeIndicator;
    private ProgressBar mBottomProgress;

    // ── 竖屏控件 ──
    private MaterialButton nPlay, nFullscreen, nFullscreenPortrait;
    private TextView nCurrTime, nTotalTime;
    private SeekBar nSeekBar;

    // ── 全屏控件 ──
    private MaterialButton fPlay, fSkipPrev, fSkipNext, fSpeed, fSelect, fFullscreen, fFullscreenPortrait;
    private TextView fCurrTime, fTotalTime;
    private SeekBar fSeekBar;

    // ── 可见性状态 ──
    private int mSelectVis = View.GONE, mSpeedVis = View.GONE;
    private int mPrevVis = View.GONE, mNextVis = View.GONE;
    private int mFullscreenVis = View.VISIBLE, mFullscreenPortraitVis = View.GONE;

    private int mSelectVisN = View.GONE, mSpeedVisN = View.GONE;
    private int mPrevVisN = View.GONE, mNextVisN = View.GONE;
    private int mFullscreenVisN = View.VISIBLE, mFullscreenPortraitVisN = View.GONE;

    private int mSelectVisF = View.GONE, mSpeedVisF = View.GONE;
    private int mPrevVisF = View.GONE, mNextVisF = View.GONE;
    private int mFullscreenVisF = View.VISIBLE, mFullscreenPortraitVisF = View.GONE;

    // ── 回调 ──
    private OnSelectClickListener mOnSelectClickListener;
    private OnSpeedOptionSelectedListener mOnSpeedOptionSelectedListener;
    private OnUpSetClickListener mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;
    private OnProgressListener mOnProgressListener;
    private OnFullscreenPortraitClickListener mOnFullscreenPortraitClickListener;

    public StarBottomView(@NonNull Context context)              { this(context, null); }
    public StarBottomView(@NonNull Context c, @Nullable AttributeSet a) { this(c, a, 0); }

    public StarBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_bottom_view, this, true);

        // 公共控件
        mTimeIndicator = (TextView) findViewById(R.id.time_indicator);
        mBottomProgress = findViewById(R.id.bottom_progress);
        if (mBottomProgress != null) mBottomProgress.setMax(1000);

        mContainerNormal = findViewById(R.id.container_normal);
        mContainerFullscreen = findViewById(R.id.container_fullscreen);

        // 竖屏引用缓存
        nPlay               = findViewById(R.id.normal_play);
        nCurrTime           = findViewById(R.id.normal_curr_time);
        nTotalTime          = findViewById(R.id.normal_total_time);
        nSeekBar            = findViewById(R.id.normal_seekbar);
        nFullscreen         = findViewById(R.id.normal_fullscreen);
        nFullscreenPortrait = findViewById(R.id.normal_fullscreen_portrait);

        // 全屏引用缓存
        fPlay               = findViewById(R.id.full_play);
        fSkipPrev           = findViewById(R.id.full_skip_previous);
        fSkipNext           = findViewById(R.id.full_skip_next);
        fSpeed              = findViewById(R.id.full_speed);
        fSelect             = findViewById(R.id.full_selected_writings);
        fFullscreen         = findViewById(R.id.full_fullscreen);
        fFullscreenPortrait = findViewById(R.id.full_fullscreen_portrait);
        fCurrTime           = findViewById(R.id.full_curr_time);
        fTotalTime          = findViewById(R.id.full_total_time);
        fSeekBar            = findViewById(R.id.full_seekbar);

        setupNormalListeners();
        setupFullscreenListeners();
    }

    // ═══════════════════════════════════════════════
    // 竖屏监听（只含竖屏有的按钮）
    // ═══════════════════════════════════════════════
    private void setupNormalListeners() {
        nPlay.setOnClickListener(v -> { if (mControlWrapper != null) mControlWrapper.togglePlay(); });
        nSeekBar.setOnSeekBarChangeListener(createSeekListener(nCurrTime));
        nFullscreen.setOnClickListener(v -> toggleFullScreen());
        nFullscreenPortrait.setOnClickListener(v -> {
            togglePortraitFullScreen();
            if (mOnFullscreenPortraitClickListener != null) mOnFullscreenPortraitClickListener.onClick(v);
        });
    }

    // ═══════════════════════════════════════════════
    // 全屏监听（所有按钮都有）
    // ═══════════════════════════════════════════════
    private void setupFullscreenListeners() {
        fPlay.setOnClickListener(v -> { if (mControlWrapper != null) mControlWrapper.togglePlay(); });
        fSeekBar.setOnSeekBarChangeListener(createSeekListener(fCurrTime));

        fSkipPrev.setOnClickListener(v -> { if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v); });
        fSkipNext.setOnClickListener(v -> { if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v); });
        fFullscreen.setOnClickListener(v -> toggleFullScreen());
        fFullscreenPortrait.setOnClickListener(v -> {
            togglePortraitFullScreen();
            if (mOnFullscreenPortraitClickListener != null) mOnFullscreenPortraitClickListener.onClick(v);
        });

        fSelect.setOnClickListener(v -> { if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(v); });
        fSpeed.setOnClickListener(v -> showSpeedPopup(v));
    }

    private SeekBar.OnSeekBarChangeListener createSeekListener(TextView currTimeView) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mControlWrapper != null) {
                    long duration = mControlWrapper.getDuration();
                    long seekPos = (long) (duration * progress / 1000f);
                    currTimeView.setText(PlayerUtils.stringForTime((int) seekPos));
                    if (mTimeIndicator != null) {
                        mTimeIndicator.setText(PlayerUtils.stringForTime((int) seekPos));
                        updateTimeIndicatorPosition(seekBar);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsDragging = true;
                if (mControlWrapper != null) {
                    mControlWrapper.stopProgress();
                    mControlWrapper.stopFadeOut();
                }
                if (mTimeIndicator != null) mTimeIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mControlWrapper != null) {
                    long duration = mControlWrapper.getDuration();
                    long seekPos = (long) (duration * seekBar.getProgress() / 1000f);
                    mControlWrapper.seekTo(seekPos);
                    mIsDragging = false;
                    mControlWrapper.startProgress();
                    mControlWrapper.startFadeOut();
                }
                if (mTimeIndicator != null) mTimeIndicator.setVisibility(View.GONE);
            }
        };
    }

    private void showSpeedPopup(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.popup_menu_speed, popup.getMenu());
        Menu menu = popup.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (Math.abs(parseSpeedText(item.getTitle().toString()) - mCurrentSpeed) < 0.01f) {
                item.setChecked(true);
                break;
            }
        }
        popup.setOnMenuItemClickListener(item -> {
            float speed = parseSpeedText(item.getTitle().toString());
            mCurrentSpeed = speed;
            if (mOnSpeedOptionSelectedListener != null) {
                mOnSpeedOptionSelectedListener.onSpeedOptionSelected(speed, item.getTitle().toString());
            }
            return true;
        });
        popup.show();
    }

    private float parseSpeedText(String text) {
        try { return Float.parseFloat(text.replace("x", "").replace("X", "")); }
        catch (NumberFormatException e) { return 1.0f; }
    }

    // ═══════════════════════════════════════════════
    // 全屏切换
    // ═══════════════════════════════════════════════
    private void toggleFullScreen() {
        if (mControlWrapper == null) return;
        Activity activity = PlayerUtils.scanForActivity(getContext());
        mControlWrapper.toggleFullScreen(activity);
    }

    private void togglePortraitFullScreen() {
        if (mControlWrapper == null) return;
        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mControlWrapper.startFullScreen();
    }

    private void updateTimeIndicatorPosition(SeekBar seekBar) {
        if (mTimeIndicator == null || seekBar.getWidth() == 0) return;

        float progress = seekBar.getProgress() / (float) seekBar.getMax();
        int[] barLoc = new int[2], selfLoc = new int[2];
        seekBar.getLocationOnScreen(barLoc);
        getLocationOnScreen(selfLoc);

        float thumbX = barLoc[0] + progress * seekBar.getWidth();
        float indicatorX = thumbX - selfLoc[0];

        mTimeIndicator.setX(indicatorX - mTimeIndicator.getWidth() / 2f);
        mTimeIndicator.setY(barLoc[1] - selfLoc[1] - mTimeIndicator.getHeight() - 10);
    }

    // ═══════════════════════════════════════════════
    // 可见性
    // ═══════════════════════════════════════════════
    private void applyButtonVisibility() {
        int sv = mIsFullScreen ? mSelectVisF : mSelectVisN;
        int spv = mIsFullScreen ? mSpeedVisF : mSpeedVisN;
        int pv = mIsFullScreen ? mPrevVisF : mPrevVisN;
        int nv = mIsFullScreen ? mNextVisF : mNextVisN;
        int fv = mIsFullScreen ? mFullscreenVisF : mFullscreenVisN;
        int fpv = mIsFullScreen ? mFullscreenPortraitVisF : mFullscreenPortraitVisN;

        if (mIsFullScreen) {
            fSelect.setVisibility(sv);
            fSpeed.setVisibility(spv);
            fSkipPrev.setVisibility(pv);
            fSkipNext.setVisibility(nv);
            fFullscreen.setVisibility(fv);
            fFullscreenPortrait.setVisibility(fpv);
        } else {
            nFullscreen.setVisibility(fv);
            nFullscreenPortrait.setVisibility(fpv);
        }
    }

    // ── 批量设置 ──
    public void setBottomButtonsVisibility(int sel, int spd, int prev, int next) {
        setBottomButtonsVisibilityNormal(sel, spd, prev, next);
        setBottomButtonsVisibilityFullscreen(sel, spd, prev, next);
        applyButtonVisibility();
    }

    public void setBottomButtonsVisibility(int sel, int spd, int prev, int next, int full, int fullPortrait) {
        setBottomButtonsVisibilityNormal(sel, spd, prev, next, full, fullPortrait);
        setBottomButtonsVisibilityFullscreen(sel, spd, prev, next, full, fullPortrait);
        applyButtonVisibility();
    }

    public void setBottomButtonsVisibilityNormal(int sel, int spd, int prev, int next) {
        mSelectVisN = sel; mSpeedVisN = spd; mPrevVisN = prev; mNextVisN = next;
        if (!mIsFullScreen) applyButtonVisibility();
    }

    public void setBottomButtonsVisibilityNormal(int sel, int spd, int prev, int next, int full, int fullPortrait) {
        mSelectVisN = sel; mSpeedVisN = spd; mPrevVisN = prev; mNextVisN = next;
        mFullscreenVisN = full; mFullscreenPortraitVisN = fullPortrait;
        if (!mIsFullScreen) applyButtonVisibility();
    }

    public void setBottomButtonsVisibilityFullscreen(int sel, int spd, int prev, int next) {
        mSelectVisF = sel; mSpeedVisF = spd; mPrevVisF = prev; mNextVisF = next;
        if (mIsFullScreen) applyButtonVisibility();
    }

    public void setBottomButtonsVisibilityFullscreen(int sel, int spd, int prev, int next, int full, int fullPortrait) {
        mSelectVisF = sel; mSpeedVisF = spd; mPrevVisF = prev; mNextVisF = next;
        mFullscreenVisF = full; mFullscreenPortraitVisF = fullPortrait;
        if (mIsFullScreen) applyButtonVisibility();
    }

    public void setBottomButtonsVisibilityAll(int selN, int spdN, int prevN, int nextN, int fullN, int fullPN,
                                              int selF, int spdF, int prevF, int nextF, int fullF, int fullPF) {
        mSelectVisN = selN; mSpeedVisN = spdN; mPrevVisN = prevN; mNextVisN = nextN;
        mFullscreenVisN = fullN; mFullscreenPortraitVisN = fullPN;
        mSelectVisF = selF; mSpeedVisF = spdF; mPrevVisF = prevF; mNextVisF = nextF;
        mFullscreenVisF = fullF; mFullscreenPortraitVisF = fullPF;
        applyButtonVisibility();
    }

    // ── 单独按钮 — 全局 ──
    public void setSelectButtonVisibility(int v)            { setSelectButtonVisibilityNormal(v); setSelectButtonVisibilityFullscreen(v); }
    public void setSpeedButtonVisibility(int v)              { setSpeedButtonVisibilityNormal(v);   setSpeedButtonVisibilityFullscreen(v); }
    public void setPreviousButtonVisibility(int v)           { setPreviousButtonVisibilityNormal(v); setPreviousButtonVisibilityFullscreen(v); }
    public void setNextButtonVisibility(int v)               { setNextButtonVisibilityNormal(v);     setNextButtonVisibilityFullscreen(v); }
    public void setFullscreenButtonVisibility(int v)         { setFullscreenButtonVisibilityNormal(v); setFullscreenButtonVisibilityFullscreen(v); }
    public void setFullscreenPortraitButtonVisibility(int v) { setFullscreenPortraitButtonVisibilityNormal(v); setFullscreenPortraitButtonVisibilityFullscreen(v); }

    // ── 单独按钮 — Normal ──
    public void setSelectButtonVisibilityNormal(int v)            { mSelectVisN = v;            if (!mIsFullScreen) nFullscreen.setVisibility(v); } // normal has no select button, ignore
    public void setSpeedButtonVisibilityNormal(int v)              { mSpeedVisN = v;             /* normal has no speed button */ }
    public void setPreviousButtonVisibilityNormal(int v)           { mPrevVisN = v;              /* normal has no prev button */ }
    public void setNextButtonVisibilityNormal(int v)               { mNextVisN = v;              /* normal has no next button */ }
    public void setFullscreenButtonVisibilityNormal(int v)         { mFullscreenVisN = v;        if (!mIsFullScreen) nFullscreen.setVisibility(v); }
    public void setFullscreenPortraitButtonVisibilityNormal(int v) { mFullscreenPortraitVisN = v;if (!mIsFullScreen) nFullscreenPortrait.setVisibility(v); }

    // ── 单独按钮 — Fullscreen ──
    public void setSelectButtonVisibilityFullscreen(int v)            { mSelectVisF = v;            if (mIsFullScreen) fSelect.setVisibility(v); }
    public void setSpeedButtonVisibilityFullscreen(int v)              { mSpeedVisF = v;             if (mIsFullScreen) fSpeed.setVisibility(v); }
    public void setPreviousButtonVisibilityFullscreen(int v)           { mPrevVisF = v;              if (mIsFullScreen) fSkipPrev.setVisibility(v); }
    public void setNextButtonVisibilityFullscreen(int v)               { mNextVisF = v;              if (mIsFullScreen) fSkipNext.setVisibility(v); }
    public void setFullscreenButtonVisibilityFullscreen(int v)         { mFullscreenVisF = v;        if (mIsFullScreen) fFullscreen.setVisibility(v); }
    public void setFullscreenPortraitButtonVisibilityFullscreen(int v) { mFullscreenPortraitVisF = v;if (mIsFullScreen) fFullscreenPortrait.setVisibility(v); }

    public void setCurrentSpeed(float speed) { mCurrentSpeed = speed; }
    public void setShowBottomProgress(boolean show) { mIsShowBottomProgress = show; }

    /** 直接应用 BottomBarConfig，Fluent API 入口 */
    public void applyConfig(com.star.play.BottomBarConfig c) {
        mSelectVisN = c.normal.select; mSpeedVisN = c.normal.speed;
        mPrevVisN = c.normal.prev; mNextVisN = c.normal.next;
        mFullscreenVisN = c.normal.fullscreen; mFullscreenPortraitVisN = c.normal.portraitFullscreen;
        mSelectVisF = c.fullscreen.select; mSpeedVisF = c.fullscreen.speed;
        mPrevVisF = c.fullscreen.prev; mNextVisF = c.fullscreen.next;
        mFullscreenVisF = c.fullscreen.fullscreen; mFullscreenPortraitVisF = c.fullscreen.portraitFullscreen;
        applyButtonVisibility();
    }

    private int mLastBufferedPercent;

    public void clearBufferedProgress() {
        mLastBufferedPercent = 0;
        nSeekBar.setSecondaryProgress(0);
        fSeekBar.setSecondaryProgress(0);
        if (mBottomProgress != null) mBottomProgress.setSecondaryProgress(0);
    }

    public void setBufferedProgress(int percent) {
        if (percent <= 0) {
            percent = mLastBufferedPercent;
        } else {
            mLastBufferedPercent = percent;
        }
        int sp = Math.min(percent, 100) * 10;
        nSeekBar.setSecondaryProgress(sp);
        fSeekBar.setSecondaryProgress(sp);
        if (mBottomProgress != null) {
            mBottomProgress.setSecondaryProgress(percent >= 95 ? mBottomProgress.getMax() : percent * 10);
        }
    }

    // ── 颜色 ──
    public void setTimeTextColor(int color) {
        nCurrTime.setTextColor(color); nTotalTime.setTextColor(color);
        fCurrTime.setTextColor(color); fTotalTime.setTextColor(color);
    }

    public void setButtonIconTint(int color) {
        ColorStateList tint = ColorStateList.valueOf(color);
        nPlay.setIconTint(tint); nFullscreen.setIconTint(tint); nFullscreenPortrait.setIconTint(tint);
        fPlay.setIconTint(tint); fSkipPrev.setIconTint(tint); fSkipNext.setIconTint(tint);
        fSpeed.setIconTint(tint); fSelect.setIconTint(tint);
        fFullscreen.setIconTint(tint); fFullscreenPortrait.setIconTint(tint);
    }

    public void setBottomContainerBackground(int color) {
        mContainerNormal.setBackgroundColor(color);
        mContainerFullscreen.setBackgroundColor(color);
    }

    // ── 回调设置 ──
    public void setOnSelectClickListener(OnSelectClickListener l)            { mOnSelectClickListener = l; }
    public void setOnSpeedOptionSelectedListener(OnSpeedOptionSelectedListener l) { mOnSpeedOptionSelectedListener = l; }
    public void setOnUpSetClickListener(OnUpSetClickListener l)              { mOnUpSetClickListener = l; }
    public void setOnDownSetClickListener(OnDownSetClickListener l)          { mOnDownSetClickListener = l; }
    public void setOnProgressListener(OnProgressListener l)                  { mOnProgressListener = l; }
    public void setOnFullscreenPortraitClickListener(OnFullscreenPortraitClickListener l) { mOnFullscreenPortraitClickListener = l; }

    // ═══════════════════════════════════════════════
    // IControlComponent
    // ═══════════════════════════════════════════════
    @Override public void attach(@NonNull ControlWrapper w) { mControlWrapper = w; }
    @Nullable @Override public View getView() { return this; }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mIsFullScreen) {
            mContainerFullscreen.setVisibility(isVisible ? VISIBLE : GONE);
            if (anim != null) mContainerFullscreen.startAnimation(anim);
        } else {
            mContainerNormal.setVisibility(isVisible ? VISIBLE : GONE);
            if (anim != null) mContainerNormal.startAnimation(anim);
        }

        if (mIsShowBottomProgress && mBottomProgress != null) {
            if (isVisible) {
                mBottomProgress.setVisibility(GONE);
            } else {
                mBottomProgress.setVisibility(VISIBLE);
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(300);
                mBottomProgress.startAnimation(fadeIn);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                if (mBottomProgress != null) { mBottomProgress.setProgress(0); mBottomProgress.setSecondaryProgress(0); }
                nSeekBar.setProgress(0); fSeekBar.setProgress(0);
                nPlay.setIconResource(R.drawable.play_arrow);
                fPlay.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_ERROR:
                setVisibility(GONE);
                nPlay.setIconResource(R.drawable.play_arrow);
                fPlay.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_PREPARING:
                nPlay.setIconResource(R.drawable.pause);
                fPlay.setIconResource(R.drawable.pause);
                if (mIsFullScreen) {
                    setVisibility(VISIBLE);
                    mContainerFullscreen.setVisibility(VISIBLE);

                } else {
                    setVisibility(GONE);
                }
                break;
            case VideoView.STATE_PREPARED:
                nPlay.setIconResource(R.drawable.play_arrow);
                fPlay.setIconResource(R.drawable.play_arrow);
                if (mIsFullScreen) {
                    setVisibility(VISIBLE);
                    mContainerFullscreen.setVisibility(VISIBLE);

                }
                break;
            case VideoView.STATE_PLAYING:
                nPlay.setIconResource(R.drawable.pause);
                fPlay.setIconResource(R.drawable.pause);
                if (mIsFullScreen) {
                    setVisibility(VISIBLE);
                    mContainerFullscreen.setVisibility(VISIBLE);

                    if (mIsShowBottomProgress && mBottomProgress != null) mBottomProgress.setVisibility(GONE);
                } else {
                    if (mIsShowBottomProgress) {
                        boolean ctrlShowing = mControlWrapper != null && mControlWrapper.isShowing();
                        if (mBottomProgress != null) mBottomProgress.setVisibility(ctrlShowing ? GONE : VISIBLE);
                        mContainerNormal.setVisibility(VISIBLE);
                    } else {
                        mContainerNormal.setVisibility(GONE);
                    }
                    setVisibility(VISIBLE);
                }
                if (mControlWrapper != null) mControlWrapper.startProgress();
                break;
            case VideoView.STATE_PAUSED:
                nPlay.setIconResource(R.drawable.play_arrow);
                fPlay.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_BUFFERING:
                nPlay.setIconResource(R.drawable.pause);
                fPlay.setIconResource(R.drawable.pause);
                if (mControlWrapper != null) mControlWrapper.stopProgress();
                break;
            case VideoView.STATE_BUFFERED:
                nPlay.setIconResource(mControlWrapper != null && mControlWrapper.isPlaying() ? R.drawable.pause : R.drawable.play_arrow);
                fPlay.setIconResource(mControlWrapper != null && mControlWrapper.isPlaying() ? R.drawable.pause : R.drawable.play_arrow);
                if (mControlWrapper != null) mControlWrapper.startProgress();
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            mIsFullScreen = true;
            mContainerNormal.setVisibility(GONE);
            mContainerFullscreen.setVisibility(VISIBLE);
            applyButtonVisibility();
        } else {
            mIsFullScreen = false;
            mContainerFullscreen.setVisibility(GONE);
            mContainerNormal.setVisibility(VISIBLE);
            applyButtonVisibility();
        }
    }

    @Override
    public void setProgress(int duration, int position) {
        if (mIsDragging) return;
        if (mOnProgressListener != null) mOnProgressListener.onProgress();

        if (duration > 0) {
            float progress = Math.max(0, Math.min(1000, (float) position / duration * 1000));
            nSeekBar.setProgress((int) progress);
            fSeekBar.setProgress((int) progress);
            if (mBottomProgress != null) mBottomProgress.setProgress((int) progress);
        } else {
            nSeekBar.setProgress(0); fSeekBar.setProgress(0);
            if (mBottomProgress != null) { mBottomProgress.setProgress(0); mBottomProgress.setSecondaryProgress(0); }
        }

        nTotalTime.setText(PlayerUtils.stringForTime(duration));
        fTotalTime.setText(PlayerUtils.stringForTime(duration));
        nCurrTime.setText(PlayerUtils.stringForTime(position));
        fCurrTime.setText(PlayerUtils.stringForTime(position));
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        onVisibilityChanged(!isLocked, null);
    }
}

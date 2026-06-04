package com.star.play.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IGestureComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class StarGestureView extends FrameLayout implements IGestureComponent {

    private ControlWrapper mControlWrapper;

    // ── 亮度 / 音量容器（icon + Slider）──
    private LinearLayout mCenterContainer;
    private MaterialButton mIconView;
    private Slider mProgressSlider;

    // ── 进度拖动容器（icon + 时间偏移 + 进度文字）──
    private LinearLayout mCenterContainer1;
    private MaterialButton mSeekIconView;
    private TextView mTimeOffsetText;
    private TextView mProgressText;
    // ── 进度背景填充 ──
    private View mProgressFill;

    public StarGestureView(@NonNull Context context) {
        this(context, null);
    }

    public StarGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_gesture_view, this, true);

        mCenterContainer = findViewById(R.id.center_container);
        mIconView = findViewById(R.id.iv_icon);
        mProgressSlider = findViewById(R.id.pro_percent);

        mCenterContainer1 = findViewById(R.id.center_container1);
        mSeekIconView = findViewById(R.id.btn_seek_icon);
        mTimeOffsetText = findViewById(R.id.tv_time_offset);
        mProgressText = findViewById(R.id.tv_progress);
        mProgressFill = findViewById(R.id.progress_fill);
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
    }

    @Override
    public void onStartSlide() {
        if (mControlWrapper != null) {
            mControlWrapper.hide();
        }
        showContainer(mCenterContainer);
    }

    @Override
    public void onStopSlide() {
        animateOutContainer(mCenterContainer);
        animateOutContainer(mCenterContainer1);
    }

    // ── 进度拖动 ──

    @Override
    public void onPositionChange(int slidePosition, int currentPosition, int duration) {
        showContainer(mCenterContainer1);
        hideContainer(mCenterContainer);

        if (mSeekIconView != null) {
            if (slidePosition > currentPosition) {
                mSeekIconView.setIconResource(R.drawable.fast_forward);
            } else {
                mSeekIconView.setIconResource(R.drawable.fast_rewind);
            }
        }
        if (mTimeOffsetText != null) {
            int offset = Math.abs(slidePosition - currentPosition);
            String sign = slidePosition > currentPosition ? "+ " : "- ";
            mTimeOffsetText.setText(sign + PlayerUtils.stringForTime(offset));
        }
        if (mProgressText != null) {
            mProgressText.setText(PlayerUtils.stringForTime(slidePosition) + " / " + PlayerUtils.stringForTime(duration));
        }
        if (mProgressFill != null && duration > 0) {
            int cardWidth = mCenterContainer1 != null ? mCenterContainer1.getWidth() : 0;
            if (cardWidth > 0) {
                float percent = (float) slidePosition / duration;
                mProgressFill.getLayoutParams().width = (int) (cardWidth * percent);
                mProgressFill.requestLayout();
            }
        }
    }

    // ── 亮度变化 ──

    @Override
    public void onBrightnessChange(int percent) {
        showContainer(mCenterContainer);
        hideContainer(mCenterContainer1);

        if (mProgressSlider != null) {
            mProgressSlider.setVisibility(VISIBLE);
            mProgressSlider.setValue(percent);
        }
        if (mIconView != null) {
            mIconView.setIconResource(R.drawable.brightness);
        }
    }

    // ── 音量变化 ──

    @Override
    public void onVolumeChange(int percent) {
        showContainer(mCenterContainer);
        hideContainer(mCenterContainer1);

        if (mProgressSlider != null) {
            mProgressSlider.setVisibility(VISIBLE);
            mProgressSlider.setValue(percent);
        }
        if (mIconView != null) {
            if (percent <= 0) {
                mIconView.setIconResource(R.drawable.volume_off);
            } else {
                mIconView.setIconResource(R.drawable.volume_up);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        if (playState == VideoView.STATE_IDLE
                || playState == VideoView.STATE_START_ABORT
                || playState == VideoView.STATE_PREPARING
                || playState == VideoView.STATE_PREPARED
                || playState == VideoView.STATE_ERROR
                || playState == VideoView.STATE_PLAYBACK_COMPLETED) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void setProgress(int duration, int position) {
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
    }

    // ── 容器显示/隐藏 ──

    private void showContainer(LinearLayout container) {
        if (container != null) {
            container.setVisibility(VISIBLE);
            container.setAlpha(1f);
        }
    }

    private void hideContainer(LinearLayout container) {
        if (container != null) {
            container.setVisibility(GONE);
        }
    }

    private void animateOutContainer(LinearLayout container) {
        if (container == null || container.getVisibility() != VISIBLE) return;
        container.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (container != null) {
                            container.setVisibility(GONE);
                        }
                    }
                })
                .start();
    }
}

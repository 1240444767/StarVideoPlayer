package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import com.star.play.controller.PlayerController;
import com.star.play.controller.IControlComponent;
import com.star.play.controller.PlayerConstants;
import com.star.play.controller.PlayerUtils;


public class StarTitleView extends FrameLayout implements IControlComponent {

    private PlayerController mPlayerController;

    private TextView mTitleView;
    private TextView mSysTimeView;
    private MaterialButton mBackView;
    private MaterialButton mPipView;
    private MaterialButton mScreenView;
    private MaterialButton mSettingsView;
    private LinearLayout mTitleContainer;

    private OnPipClickListener mOnPipClickListener;
    private OnScreenClickListener mOnScreenClickListener;
    private OnSettingsClickListener mOnSettingsClickListener;

    public StarTitleView(@NonNull Context context) {
        super(context);
        init();
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_title_view, this, true);

        mTitleContainer = findViewById(R.id.title_container);

        mBackView = findViewById(R.id.back);
        if (mBackView != null) {
            mBackView.setOnClickListener(view -> {
                if (mPlayerController == null) return;
                Activity activity = PlayerUtils.scanForActivity(getContext());
                if (activity != null && mPlayerController.isFullScreen()) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mPlayerController.stopFullScreen();
                }
            });
        }

        mTitleView = findViewById(R.id.title);
        mSysTimeView = findViewById(R.id.sys_time);

        mPipView = findViewById(R.id.pip);
        if (mPipView != null) {
            mPipView.setOnClickListener(view -> {
                if (mOnPipClickListener != null)
                    mOnPipClickListener.onPipClick(view);
            });
        }

        mScreenView = findViewById(R.id.screen);
        if (mScreenView != null) {
            mScreenView.setOnClickListener(view -> {
                if (mOnScreenClickListener != null)
                    mOnScreenClickListener.onScreenClick(view);
            });
        }

        mSettingsView = findViewById(R.id.settings);
        if (mSettingsView != null) {
            mSettingsView.setOnClickListener(view -> {
                if (mOnSettingsClickListener != null)
                    mOnSettingsClickListener.onSettingsClick(view);
            });
        }
    }

    public interface OnPipClickListener {
        void onPipClick(View view);
    }

    public interface OnScreenClickListener {
        void onScreenClick(View view);
    }

    public interface OnSettingsClickListener {
        void onSettingsClick(View view);
    }

    public void setOnPipClickListener(OnPipClickListener listener) {
        mOnPipClickListener = listener;
    }

    public void setOnScreenClickListener(OnScreenClickListener listener) {
        mOnScreenClickListener = listener;
    }

    public void setOnSettingsClickListener(OnSettingsClickListener listener) {
        mOnSettingsClickListener = listener;
    }

    public void setTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    public void setTitleButtonsVisibility(int backVisibility, int pipVisibility, 
                                          int screenVisibility, int settingsVisibility) {
        if (mBackView != null) {
            mBackView.setVisibility(backVisibility);
        }
        if (mPipView != null) {
            mPipView.setVisibility(pipVisibility);
        }
        if (mScreenView != null) {
            mScreenView.setVisibility(screenVisibility);
        }
        if (mSettingsView != null) {
            mSettingsView.setVisibility(settingsVisibility);
        }
    }

    public void setBackButtonVisibility(int visibility) {
        if (mBackView != null) {
            mBackView.setVisibility(visibility);
        }
    }

    public void setPipButtonVisibility(int visibility) {
        if (mPipView != null) {
            mPipView.setVisibility(visibility);
        }
    }

    public void setScreenButtonVisibility(int visibility) {
        if (mScreenView != null) {
            mScreenView.setVisibility(visibility);
        }
    }

    public void setSettingsButtonVisibility(int visibility) {
        if (mSettingsView != null) {
            mSettingsView.setVisibility(visibility);
        }
    }

    public void setSysTimeVisibility(int visibility) {
        if (mSysTimeView != null) {
            mSysTimeView.setVisibility(visibility);
        }
    }

    public void setTitleTextColor(int color) {
        if (mTitleView != null) {
            mTitleView.setTextColor(color);
        }
    }

    public void setSysTimeTextColor(int color) {
        if (mSysTimeView != null) {
            mSysTimeView.setTextColor(color);
        }
    }

    public void setButtonIconTint(int color) {
        if (mBackView != null) {
            mBackView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mPipView != null) {
            mPipView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mScreenView != null) {
            mScreenView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mSettingsView != null) {
            mSettingsView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
    }

    public void setTitleContainerBackground(int color) {
        if (mTitleContainer != null) {
            mTitleContainer.setBackgroundColor(color);
        }
    }

    @Override
    public void attach(@NonNull PlayerController controlWrapper) {
        mPlayerController = controlWrapper;
    }

    @Nullable
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mPlayerController == null || !mPlayerController.isFullScreen()) return;
        if (mPlayerController.isLocked()) return;

        if (isVisible) {
            if (getVisibility() == GONE) {
                if (mSysTimeView != null) {
                    mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
                }
                setVisibility(VISIBLE);
                if (anim != null) startAnimation(anim);
            }
        } else {
            if (getVisibility() == VISIBLE) {
                setVisibility(GONE);
                if (anim != null) startAnimation(anim);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case PlayerConstants.STATE_IDLE:
            case PlayerConstants.STATE_START_ABORT:
            case PlayerConstants.STATE_ERROR:
            case PlayerConstants.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                break;
            case PlayerConstants.STATE_PREPARING:
                if (mPlayerController != null && mPlayerController.isFullScreen()) {
                    setVisibility(VISIBLE);
                } else {
                    setVisibility(GONE);
                }
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (mPlayerController == null) return;

        if (playerState == PlayerConstants.PLAYER_FULL_SCREEN) {
            setVisibility(VISIBLE);
            if (mSysTimeView != null) {
                mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
            }
            if (mTitleView != null) mTitleView.setSelected(true);
        } else {
            setVisibility(GONE);
            if (mTitleView != null) mTitleView.setSelected(false);
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (mTitleContainer != null) {
            StarCutoutHelper.applyCutoutPadding(mTitleContainer, mPlayerController, activity);
        }
    }

    @Override
    public void setProgress(int duration, int position) {}

    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked) {
            setVisibility(GONE);
        } else {
            if (mPlayerController != null && mPlayerController.isFullScreen()) {
                setVisibility(VISIBLE);
                if (mSysTimeView != null) {
                    mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
                }
            }
        }
    }
}

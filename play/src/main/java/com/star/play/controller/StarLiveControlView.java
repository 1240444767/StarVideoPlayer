package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import com.star.play.controller.PlayerController;
import com.star.play.controller.IControlComponent;
import com.star.play.controller.PlayerConstants;
import com.star.play.controller.PlayerUtils;


/**
 * 直播底部控制栏
 * 包含播放/暂停、刷新、全屏按钮
 */
public class StarLiveControlView extends FrameLayout implements IControlComponent, View.OnClickListener {

    private PlayerController mPlayerController;

    private MaterialButton mPlayButton;       // 播放/暂停按钮
    private MaterialButton mRefreshButton;    // 刷新按钮
    private MaterialButton mFullscreenButton; // 全屏按钮
    private LinearLayout mBottomContainer;    // 底部容器（用于刘海适配）

    public StarLiveControlView(@NonNull Context context) {
        this(context, null);
    }

    public StarLiveControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarLiveControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化视图和事件监听
     */
    private void init() {
        // 初始隐藏，由控制器控制显示
        setVisibility(GONE);

        // 填充布局
        LayoutInflater.from(getContext()).inflate(R.layout.star_live_control_view, this, true);

        // 绑定控件
        mBottomContainer = findViewById(R.id.bottom_container);
        mPlayButton = findViewById(R.id.play);
        mRefreshButton = findViewById(R.id.refresh);
        mFullscreenButton = findViewById(R.id.fullscreen);

        // 设置点击事件
        mPlayButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);
        mFullscreenButton.setOnClickListener(this);
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
        if (isVisible) {
            if (getVisibility() == GONE) {
                setVisibility(VISIBLE);
                if (anim != null) {
                    startAnimation(anim);
                }
            }
        } else {
            if (getVisibility() == VISIBLE) {
                setVisibility(GONE);
                if (anim != null) {
                    startAnimation(anim);
                }
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case PlayerConstants.STATE_IDLE:
            case PlayerConstants.STATE_START_ABORT:
            case PlayerConstants.STATE_PREPARING:
            case PlayerConstants.STATE_PREPARED:
            case PlayerConstants.STATE_ERROR:
            case PlayerConstants.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                break;
            case PlayerConstants.STATE_PLAYING:
                mPlayButton.setIconResource(R.drawable.pause); // 切换为暂停图标
                break;
            case PlayerConstants.STATE_PAUSED:
                mPlayButton.setIconResource(R.drawable.play_arrow); // 切换为播放图标
                break;
            case PlayerConstants.STATE_BUFFERING:
            case PlayerConstants.STATE_BUFFERED:
                if (mPlayerController != null) {
                    mPlayButton.setIconResource(mPlayerController.isPlaying() ?
                            R.drawable.pause : R.drawable.play_arrow);
                }
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        switch (playerState) {
            case PlayerConstants.PLAYER_NORMAL:
                mFullscreenButton.setIconResource(R.drawable.fullscreen); // 进入全屏图标
                break;
            case PlayerConstants.PLAYER_FULL_SCREEN:
                mFullscreenButton.setIconResource(R.drawable.fullscreen_exit); // 退出全屏图标
                break;
        }

        // 刘海屏适配
        Activity activity = PlayerUtils.scanForActivity(getContext());
        StarCutoutHelper.applyCutoutPadding(mBottomContainer, mPlayerController, activity);
    }

    @Override
    public void setProgress(int duration, int position) {
        // 直播不需要进度条，空实现
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        // 锁屏状态变化时，同步控制栏显示/隐藏
        onVisibilityChanged(!isLocked, null);
    }

    @Override
    public void onClick(View v) {
        if (mPlayerController == null) return;

        int id = v.getId();
        if (id == R.id.play) {
            mPlayerController.togglePlay();
        } else if (id == R.id.refresh) {
            // 刷新直播：重新播放，参数 true 表示重置播放状态
            mPlayerController.replay(true);
        } else if (id == R.id.fullscreen) {
            toggleFullScreen();
        }
    }

    /**
     * 切换全屏模式
     */
    private void toggleFullScreen() {
        Activity activity = PlayerUtils.scanForActivity(getContext());
        mPlayerController.toggleFullScreen(activity);
    }
}
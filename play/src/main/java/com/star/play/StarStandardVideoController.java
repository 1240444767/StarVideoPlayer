package com.star.play;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.star.play.controller.IControlComponent;
import com.star.play.controller.PlayerConstants;
import com.star.play.controller.PlayerController;
import com.star.play.controller.StarBottomView;
import com.star.play.controller.StarCompleteView;
import com.star.play.controller.StarCutoutHelper;
import com.star.play.controller.StarErrorView;
import com.star.play.controller.StarGestureView;
import com.star.play.controller.StarLiveControlView;
import com.star.play.controller.StarPrepareView;
import com.star.play.controller.StarTitleView;

/**
 * 标准视频控制器，整合所有控制组件。
 * 支持点播/直播，包含锁屏、加载、长按倍速等功能。
 */
public class StarStandardVideoController extends FrameLayout implements View.OnClickListener, IControlComponent {

    private PlayerController mController;
    protected Activity mActivity;

    private MaterialButton mLockButton;
    private CircularProgressIndicator mLoadingIndicator;
    private TextView mTcpSpeedView;
    private LinearLayout mSpeedLayout;
    private TextView mSpeedTextView;

    private boolean mIsBuffering;

    private OnSpeedListener mOnSpeedListener;
    private OnCancelSpeedListener mOnCancelSpeedListener;

    public interface OnSpeedListener { void onSpeed(); }
    public interface OnCancelSpeedListener { void onCancelSpeed(); }

    public void setOnSpeedListener(OnSpeedListener l) { mOnSpeedListener = l; }
    public void setOnCancelSpeedListener(OnCancelSpeedListener l) { mOnCancelSpeedListener = l; }

    public StarStandardVideoController(@NonNull Context context) { this(context, null); }
    public StarStandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }

    public StarStandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(getContext(), R.layout.star_standard_video_controller, this);
        mLockButton = findViewById(R.id.lock);
        mLoadingIndicator = findViewById(R.id.loading);
        mTcpSpeedView = findViewById(R.id.TcpSpeed);
        mSpeedLayout = findViewById(R.id.speed_layout);
        mSpeedTextView = findViewById(R.id.speed_text);
        mLockButton.setOnClickListener(this);
        mLoadingIndicator.setVisibility(GONE);
    }

    // ── IControlComponent ──

    @Override public void attach(@NonNull PlayerController w) {
        mController = w;
        resolveActivity();
    }

    @Override public View getView() { return this; }

    @Override public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mController != null && mController.isFullScreen()) {
            if (isVisible) {
                if (mLockButton.getVisibility() == GONE) {
                    mLockButton.setVisibility(VISIBLE);
                    if (anim != null) mLockButton.startAnimation(anim);
                }
            } else {
                if (mLockButton.getVisibility() == VISIBLE) {
                    mLockButton.setVisibility(GONE);
                    if (anim != null) mLockButton.startAnimation(anim);
                }
            }
        }
    }

    @Override public void onPlayStateChanged(int playState) {
        switch (playState) {
            case PlayerConstants.STATE_IDLE:
            case PlayerConstants.STATE_START_ABORT:
                mLockButton.setSelected(false);
                mLoadingIndicator.setVisibility(GONE);
                break;
            case PlayerConstants.STATE_PLAYING:
            case PlayerConstants.STATE_PAUSED:
            case PlayerConstants.STATE_PREPARED:
            case PlayerConstants.STATE_ERROR:
            case PlayerConstants.STATE_BUFFERED:
                if (playState == PlayerConstants.STATE_BUFFERED) mIsBuffering = false;
                if (!mIsBuffering) mLoadingIndicator.setVisibility(GONE);
                break;
            case PlayerConstants.STATE_PREPARING:
                mLoadingIndicator.setVisibility(VISIBLE);
                if (mController != null && mController.isFullScreen()) {
                    if (mController != null) mController.show();
                }
                break;
            case PlayerConstants.STATE_BUFFERING:
                mLoadingIndicator.setVisibility(VISIBLE);
                mIsBuffering = true;
                break;
            case PlayerConstants.STATE_PLAYBACK_COMPLETED:
                mLoadingIndicator.setVisibility(GONE);
                mLockButton.setVisibility(GONE);
                mLockButton.setSelected(false);
                break;
        }
    }

    @Override public void onPlayerStateChanged(int playerState) {
        switch (playerState) {
            case PlayerConstants.PLAYER_NORMAL:
                mLockButton.setVisibility(GONE);
                break;
            case PlayerConstants.PLAYER_FULL_SCREEN:
                mLockButton.setVisibility(isShowing() ? VISIBLE : GONE);
                break;
        }
        if (mActivity != null) {
            int dp24 = dp2px(24);
            StarCutoutHelper.applyCutoutMargin(mLockButton, mController, mActivity, dp24, dp24);
        }
    }

    @Override public void setProgress(int duration, int position) {}
    @Override public void onLockStateChanged(boolean locked) {
        if (locked) mLockButton.setIconResource(R.drawable.lock);
        else mLockButton.setIconResource(R.drawable.lock_open);
    }

    // ── 便捷方法 ──

    public void setTcpSpeed(String speed) {
        if (mTcpSpeedView != null) mTcpSpeedView.setText(speed);
    }

    public void setSpeedLayoutVisibility(int v) { if (mSpeedLayout != null) mSpeedLayout.setVisibility(v); }
    public void setSpeedText(String t) { if (mSpeedTextView != null) mSpeedTextView.setText(t); }

    private boolean isShowing() { return mController != null && mController.isShowing(); }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.lock && mController != null) mController.toggleLockState();
    }

    @Override public void onLongPress(MotionEvent e) {
        if (mOnSpeedListener != null) mOnSpeedListener.onSpeed();
    }

    @Override public boolean onDoubleTap(MotionEvent e) {
        if (mController != null) mController.togglePlay();
        return false;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mOnCancelSpeedListener != null) mOnCancelSpeedListener.onCancelSpeed();
        }
        return super.onTouchEvent(event);
    }

    public boolean onBackPressed() {
        if (mController != null && mController.isLocked()) {
            if (mController != null) mController.show();
            Toast.makeText(getContext(), "控制器已锁定", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (mController != null && mController.isFullScreen()) {
            mController.stopFullScreen();
            return true;
        }
        return false;
    }

    private void resolveActivity() {
        Context c = getContext();
        while (c instanceof android.content.ContextWrapper) {
            if (c instanceof Activity) { mActivity = (Activity) c; return; }
            c = ((android.content.ContextWrapper) c).getBaseContext();
        }
    }

    private static int dp2px(int dp) {
        return (int) (dp * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }
}

package com.star.play.controller;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;

import com.star.play.StarVideoPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 替代 DKPlayer PlayerController，桥接 StarVideoPlayer 和所有控制器组件。
 */
public class PlayerController {

    private final StarVideoPlayer mPlayer;
    private final List<IControlComponent> mComponents = new CopyOnWriteArrayList<>();
    private boolean mLocked;
    private boolean mShowing = true;
    private int mCurrentPlayState = PlayerConstants.STATE_IDLE;
    private int mCurrentPlayerState = PlayerConstants.PLAYER_NORMAL;

    public PlayerController(@NonNull StarVideoPlayer player) {
        mPlayer = player;
    }

    // ── 组件管理 ──

    public void addControlComponent(IControlComponent... components) {
        for (IControlComponent c : components) {
            mComponents.add(c);
            c.attach(this);
            View v = c.getView();
            if (v != null) mPlayer.addView(v);
        }
    }

    public void setCanChangePosition(boolean can) {
        // 由手势组件处理，预留接口
    }

    // ── 播放控制 ──

    public void start() { mPlayer.start(); }
    public void pause() { mPlayer.pause(); }
    public void togglePlay() { if (mPlayer.isPlaying()) mPlayer.pause(); else mPlayer.start(); }
    public void seekTo(long pos) { mPlayer.seekTo(pos); }
    public long getDuration() { return mPlayer.getDuration(); }
    public long getCurrentPosition() { return mPlayer.getCurrentPosition(); }
    public boolean isPlaying() { return mPlayer.isPlaying(); }
    public int getBufferedPercentage() { return mPlayer.getBufferedPercentage(); }
    public void replay(boolean resetPosition) {
        if (resetPosition) mPlayer.seekTo(0);
        mPlayer.start();
    }

    // ── 全屏 ──

    public void startFullScreen() { mPlayer.startFullScreen(); }
    public void stopFullScreen() { mPlayer.stopFullScreen(); }
    public boolean isFullScreen() { return mPlayer.isFullScreen(); }

    public void toggleFullScreen(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        if (isFullScreen()) {
            mPlayer.stopFullScreen();
        } else {
            mPlayer.startFullScreen();
        }
    }

    public void toggleFullScreenByVideoSize(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        int[] size = getVideoSize();
        boolean wide = size[0] > size[1];
        if (isFullScreen()) {
            mPlayer.stopFullScreen();
        } else {
            mPlayer.startFullScreen();
        }
    }

    // ── 音视频属性 ──

    public void setSpeed(float speed) { mPlayer.setSpeed(speed); }
    public float getSpeed() { return mPlayer.getSpeed(); }
    public void setMute(boolean mute) { mPlayer.setMute(mute); }
    public boolean isMute() { return mPlayer.isMute(); }
    public void setScreenScaleType(int type) { mPlayer.setScreenScaleType(type); }
    public int[] getVideoSize() { return mPlayer.getVideoSize(); }
    public long getTcpSpeed() { return 0L; }

    // ── 控制器 UI ──

    public void show() {
        mShowing = true;
        for (IControlComponent c : mComponents) {
            c.onVisibilityChanged(true, null);
        }
    }

    public void hide() {
        mShowing = false;
        for (IControlComponent c : mComponents) {
            c.onVisibilityChanged(false, null);
        }
    }

    public boolean isShowing() { return mShowing; }

    public void toggleShowState() {
        if (isShowing()) hide(); else show();
    }

    public void startFadeOut() {
        // 预留：延迟 3 秒后 hide
    }

    public void stopFadeOut() {
        // 预留：取消延迟 hide
    }

    public void startProgress() {}
    public void stopProgress() {}

    // ── 锁定 ──

    public boolean isLocked() { return mLocked; }
    public void setLocked(boolean locked) {
        mLocked = locked;
        for (IControlComponent c : mComponents) {
            c.onLockStateChanged(locked);
        }
    }
    public void toggleLockState() { setLocked(!mLocked); }

    // ── 刘海屏 ──

    public boolean hasCutout() {
        Activity a = mPlayer.getPlayerActivity();
        if (a == null || android.os.Build.VERSION.SDK_INT < 28) return false;
        android.view.DisplayCutout cutout = a.getWindow().getDecorView().getRootWindowInsets() != null
                ? a.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout() : null;
        return cutout != null;
    }

    public int getCutoutHeight() {
        Activity a = mPlayer.getPlayerActivity();
        if (a == null || android.os.Build.VERSION.SDK_INT < 28) return 0;
        android.view.DisplayCutout cutout = a.getWindow().getDecorView().getRootWindowInsets() != null
                ? a.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout() : null;
        return cutout != null ? cutout.getSafeInsetTop() : 0;
    }

    // ── 状态分发 ──

    public void notifyPlayState(int state) {
        mCurrentPlayState = state;
        for (IControlComponent c : mComponents) {
            c.onPlayStateChanged(state);
        }
    }

    public void notifyPlayerState(int state) {
        mCurrentPlayerState = state;
        for (IControlComponent c : mComponents) {
            c.onPlayerStateChanged(state);
        }
    }

    public void notifyProgress(int duration, int position) {
        for (IControlComponent c : mComponents) {
            c.setProgress(duration, position);
        }
    }

    public int getCurrentPlayState() { return mCurrentPlayState; }
    public int getCurrentPlayerState() { return mCurrentPlayerState; }
}

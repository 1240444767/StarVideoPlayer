package com.star.play;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Encapsulates all SharedPreferences persistence for StarVideoPlayer.
 * Extracted to reduce God-class complexity in StarVideoPlayer.
 */
public class StarPlayerSettings {

    private static final String PREFS_NAME = "star_video_prefs";
    private static final String KEY_LONG_PRESS_SPEED = "long_press_speed";
    private static final String KEY_LONG_PRESS_SPEED_TEXT = "long_press_speed_text";
    static final String KEY_MUTE = "mute";
    static final String KEY_SKIP_START_PROGRESS = "skip_start_progress";
    static final String KEY_SKIP_END_PROGRESS = "skip_end_progress";
    static final String KEY_AUTO_NEXT = "auto_next";
    static final String KEY_HIDE_PROGRESS = "hide_progress";
    static final String KEY_AUTO_ROTATE = "auto_rotate";
    static final String KEY_SCREEN_SCALE = "screen_scale";

    private final SharedPreferences mPrefs;

    public StarPlayerSettings(Context context) {
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── Long press speed ──

    public float getLongPressSpeed() {
        return mPrefs.getFloat(KEY_LONG_PRESS_SPEED, 3.0f);
    }

    public String getLongPressSpeedText() {
        return mPrefs.getString(KEY_LONG_PRESS_SPEED_TEXT, "3.0x");
    }

    public void setLongPressSpeed(float speed) {
        String text = String.format(Locale.US, "%.1fX", speed);
        mPrefs.edit()
                .putFloat(KEY_LONG_PRESS_SPEED, speed)
                .putString(KEY_LONG_PRESS_SPEED_TEXT, text)
                .apply();
    }

    // ── Mute ──

    public boolean isMute() {
        return mPrefs.getBoolean(KEY_MUTE, false);
    }

    public void setMute(boolean mute) {
        mPrefs.edit().putBoolean(KEY_MUTE, mute).apply();
    }

    // ── Hide progress ──

    public boolean isHideProgress() {
        return mPrefs.getBoolean(KEY_HIDE_PROGRESS, false);
    }

    public void setHideProgress(boolean hide) {
        mPrefs.edit().putBoolean(KEY_HIDE_PROGRESS, hide).apply();
    }

    // ── Auto rotate ──

    public boolean isAutoRotate() {
        return mPrefs.getBoolean(KEY_AUTO_ROTATE, false);
    }

    public void setAutoRotate(boolean autoRotate) {
        mPrefs.edit().putBoolean(KEY_AUTO_ROTATE, autoRotate).apply();
    }

    // ── Skip start ──

    public int getSkipStartProgress() {
        return mPrefs.getInt(KEY_SKIP_START_PROGRESS, 0);
    }

    public void setSkipStartProgress(int progress) {
        mPrefs.edit().putInt(KEY_SKIP_START_PROGRESS, progress).apply();
    }

    // ── Skip end ──

    public int getSkipEndProgress() {
        return mPrefs.getInt(KEY_SKIP_END_PROGRESS, 0);
    }

    public void setSkipEndProgress(int progress) {
        mPrefs.edit().putInt(KEY_SKIP_END_PROGRESS, progress).apply();
    }

    // ── Auto next ──

    public boolean isAutoNext() {
        return mPrefs.getBoolean(KEY_AUTO_NEXT, true);
    }

    public void setAutoNext(boolean autoNext) {
        mPrefs.edit().putBoolean(KEY_AUTO_NEXT, autoNext).apply();
    }

    // ── Screen scale ──

    public int getScreenScale() {
        return mPrefs.getInt(KEY_SCREEN_SCALE, 0); // 0 = SCREEN_SCALE_DEFAULT
    }

    public void setScreenScale(int scaleType) {
        mPrefs.edit().putInt(KEY_SCREEN_SCALE, scaleType).apply();
    }

}

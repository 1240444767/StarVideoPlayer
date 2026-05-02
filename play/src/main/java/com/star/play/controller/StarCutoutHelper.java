package com.star.play.controller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;

import com.star.play.controller.PlayerController;

/**
 * Shared utility for applying notch/cutout-aware padding or margin to a view.
 * Extracted from duplicated adaptation logic across controller components.
 */
public final class StarCutoutHelper {

    private StarCutoutHelper() {
    }

    /**
     * Apply cutout-safe horizontal padding to {@code target}.
     * On landscape orientations the cutout side gets extra padding equal to the cutout height.
     *
     * @param target  the view whose padding will be set
     * @param wrapper the controller wrapper providing cutout info
     * @param activity the host activity
     */
    public static void applyCutoutPadding(View target, PlayerController wrapper, Activity activity) {
        if (activity == null || wrapper == null || !wrapper.hasCutout()) return;

        int orientation = activity.getRequestedOrientation();
        int cutoutHeight = wrapper.getCutoutHeight();

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            target.setPadding(0, 0, 0, 0);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            target.setPadding(cutoutHeight, 0, 0, 0);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            target.setPadding(0, 0, cutoutHeight, 0);
        }
    }

    /**
     * Apply cutout-safe horizontal margin to {@code target}.
     *
     * @param target  the view whose layout margins will be set
     * @param wrapper the controller wrapper providing cutout info
     * @param activity the host activity
     */
    public static void applyCutoutMargin(View target, PlayerController wrapper, Activity activity) {
        applyCutoutMargin(target, wrapper, activity, 0, 0);
    }

    /**
     * Apply cutout-safe horizontal margin with a uniform base margin on both sides.
     * On landscape orientations the cutout side adds the cutout height on top of the base.
     *
     * @param target    the view whose layout margins will be set
     * @param wrapper   the controller wrapper providing cutout info
     * @param activity  the host activity
     * @param baseStartPx  base margin in pixels for the start side (left in LTR)
     * @param baseEndPx    base margin in pixels for the end side (right in LTR)
     */
    public static void applyCutoutMargin(View target, PlayerController wrapper, Activity activity,
                                         int baseStartPx, int baseEndPx) {
        ViewGroup.LayoutParams lp = target.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) return;
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

        if (activity == null || wrapper == null || !wrapper.hasCutout()) {
            mlp.setMargins(baseStartPx, 0, baseEndPx, 0);
            target.setLayoutParams(mlp);
            return;
        }

        int orientation = activity.getRequestedOrientation();
        int cutoutHeight = wrapper.getCutoutHeight();

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mlp.setMargins(baseStartPx, 0, baseEndPx, 0);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mlp.setMargins(baseStartPx + cutoutHeight, 0, baseEndPx, 0);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            mlp.setMargins(baseStartPx, 0, baseEndPx + cutoutHeight, 0);
        }
        target.setLayoutParams(mlp);
    }

    /**
     * Apply cutout-safe horizontal padding with a base padding value on both sides.
     *
     * @param target    the view whose padding will be set
     * @param wrapper   the controller wrapper providing cutout info
     * @param activity  the host activity
     * @param basePx    base padding in pixels on the non-cutout sides
     */
    public static void applyCutoutPadding(View target, PlayerController wrapper, Activity activity,
                                          int basePx) {
        if (activity == null || wrapper == null || !wrapper.hasCutout()) {
            target.setPadding(basePx, 0, basePx, 0);
            return;
        }

        int orientation = activity.getRequestedOrientation();
        int cutoutHeight = wrapper.getCutoutHeight();

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            target.setPadding(basePx, 0, basePx, 0);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            target.setPadding(basePx + cutoutHeight, 0, basePx, 0);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            target.setPadding(basePx, 0, basePx + cutoutHeight, 0);
        }
    }
}

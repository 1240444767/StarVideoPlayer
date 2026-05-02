package com.star.play;

import android.view.View;

/**
 * 底部栏按钮可见性配置，Fluent Builder 风格。
 *
 * <pre>{@code
 * // 全屏和非全屏不同配置
 * videoView.bottomBar(bar -> bar
 *     .normal(n -> n
 *         .select(View.GONE)
 *         .speed(View.GONE)
 *         .prev(View.GONE)
 *         .next(View.GONE)
 *         .fullscreen(View.VISIBLE)
 *         .portraitFullscreen(View.GONE)
 *     )
 *     .fullscreen(f -> f
 *         .select(View.VISIBLE)
 *         .speed(View.VISIBLE)
 *         .prev(View.VISIBLE)
 *         .next(View.VISIBLE)
 *         .fullscreen(View.GONE)
 *         .portraitFullscreen(View.VISIBLE)
 *     )
 * );
 *
 * // 全局统一
 * videoView.bottomBar(bar -> bar
 *     .all(a -> a.select(View.GONE).speed(View.GONE).prev(View.GONE).next(View.GONE))
 * );
 *
 * // 只设一个模式
 * videoView.bottomBar(bar -> bar.fullscreen(f -> f.prev(View.VISIBLE)));
 * }</pre>
 */
public class BottomBarConfig {

    /** 按钮可见性配置 */
    public static class Buttons {
        public int select = View.GONE;
        public int speed = View.GONE;
        public int prev = View.GONE;
        public int next = View.GONE;
        public int fullscreen = View.VISIBLE;
        public int portraitFullscreen = View.GONE;

        public Buttons select(int v) { select = v; return this; }
        public Buttons speed(int v)  { speed = v; return this; }
        public Buttons prev(int v)   { prev = v; return this; }
        public Buttons next(int v)   { next = v; return this; }
        public Buttons fullscreen(int v)        { fullscreen = v; return this; }
        public Buttons portraitFullscreen(int v){ portraitFullscreen = v; return this; }
    }

    @FunctionalInterface
    public interface Block<T> { void configure(T t); }

    // ── 单一模式 ──

    public Buttons normal   = new Buttons();
    public Buttons fullscreen = new Buttons();

    /** 仅非全屏 */
    public BottomBarConfig normal(Block<Buttons> b) { b.configure(normal); return this; }
    /** 仅全屏 */
    public BottomBarConfig fullscreen(Block<Buttons> b) { b.configure(fullscreen); return this; }
    /** 全局（同时设两边） */
    public BottomBarConfig all(Block<Buttons> b) { Buttons shared = new Buttons(); b.configure(shared); normal = shared; fullscreen = shared; return this; }
}

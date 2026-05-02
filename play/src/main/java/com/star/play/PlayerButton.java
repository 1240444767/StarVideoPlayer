package com.star.play;

/**
 * 所有可控制可见性的播放器按钮。
 *
 * <pre>{@code
 * // 底部
 * SELECT, SPEED, PREV, NEXT, FULLSCREEN, PORTRAIT_FULLSCREEN
 * // 顶部
 * BACK, PIP, CAST, SETTINGS, SYS_TIME
 * }</pre>
 */
public enum PlayerButton {

    // ── 底部 ──
    /** 选集 */
    SELECT,
    /** 倍速 */
    SPEED,
    /** 上一集 */
    PREV,
    /** 下一集 */
    NEXT,
    /** 全屏 */
    FULLSCREEN,
    /** 竖屏全屏 */
    PORTRAIT_FULLSCREEN,

    // ── 顶部 ──
    /** 返回 */
    BACK,
    /** 画中画 */
    PIP,
    /** 投屏 */
    CAST,
    /** 设置 */
    SETTINGS,
    /** 系统时间 */
    SYS_TIME
}

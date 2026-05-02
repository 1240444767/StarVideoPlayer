package com.star.play;

/**
 * 播放器颜色项。
 *
 * <pre>{@code
 * player.setColor(TITLE_ICON, Color.WHITE);   // 标题栏全部按钮图标
 * player.setColor(BOTTOM_ICON, Color.WHITE);  // 底部栏全部按钮图标
 * }</pre>
 */
public enum PlayerColor {
    /** 标题栏全部按钮图标色（返回、小窗、投屏、设置） */
    TITLE_ICON,
    /** 底部栏全部按钮图标色（播放、上下集、倍速、选集、全屏、竖屏全屏） */
    BOTTOM_ICON
}

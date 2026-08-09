package com.davidblackcn.buildupmenubutton.client.profile;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutItem;

/**
 * 单个按钮在采集时刻的只读快照，供布局规划使用。
 *
 * <p>纯 Java 实现，不持有 Minecraft 控件引用：只保存发现顺序、类名、翻译键、图标形态、
 * 原始矩形与状态位。规划过程只读这些字段，最终由 {@code LayoutApplier} 依据相同的发现顺序
 * 把新几何写回真实控件。</p>
 */
public final class ButtonSnapshot implements LayoutItem {

    private final int index;
    private final String className;
    private final String translationKey;
    private final boolean iconLike;
    private final int originalX;
    private final int originalY;
    private final int width;
    private final int height;
    private final boolean active;
    private final boolean visible;

    public ButtonSnapshot(
            int index,
            String className,
            String translationKey,
            boolean iconLike,
            int originalX,
            int originalY,
            int width,
            int height,
            boolean active,
            boolean visible) {
        this.index = index;
        this.className = className;
        this.translationKey = translationKey;
        this.iconLike = iconLike;
        this.originalX = originalX;
        this.originalY = originalY;
        this.width = width;
        this.height = height;
        this.active = active;
        this.visible = visible;
    }

    /** 在采集结果列表中的顺序下标，与真实控件列表一一对应。 */
    public int index() {
        return index;
    }

    /** 按钮类的全限定名，用于指纹识别与告警输出。 */
    public String className() {
        return className;
    }

    /** 翻译键；非可翻译文本时为 {@code null}。 */
    @Override
    public String translationKey() {
        return translationKey;
    }

    @Override
    public boolean iconLike() {
        return iconLike;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    public int originalX() {
        return originalX;
    }

    public int originalY() {
        return originalY;
    }

    public boolean active() {
        return active;
    }

    public boolean visible() {
        return visible;
    }
}

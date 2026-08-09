package com.davidblackcn.buildupmenubutton.client;

import com.davidblackcn.buildupmenubutton.client.layout.Rect;

/**
 * 将 Realms 通知覆盖层原本固定的坐标基准换算为当前 Realms 按钮的坐标。
 *
 * <p>26.2 原版覆盖层使用按钮顶部前 48px 作为纵向基准，并使用按钮右边缘作为横向基准。
 * 当未能找到 Realms 按钮时，保留原版基准以实现 fail-open。</p>
 */
public final class RealmsNotificationAnchor {

    private static final int REALMS_BUTTON_Y_OFFSET = 48;

    private RealmsNotificationAnchor() {
    }

    public static int verticalBase(Rect realmsButton, int vanillaBase) {
        return realmsButton == null ? vanillaBase : realmsButton.y() - REALMS_BUTTON_Y_OFFSET;
    }

    public static int horizontalBase(Rect realmsButton, int vanillaBase) {
        return realmsButton == null ? vanillaBase : realmsButton.right();
    }
}

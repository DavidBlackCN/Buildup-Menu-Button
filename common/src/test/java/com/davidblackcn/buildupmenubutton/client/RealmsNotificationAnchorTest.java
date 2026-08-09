package com.davidblackcn.buildupmenubutton.client;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutTestSuite;
import com.davidblackcn.buildupmenubutton.client.layout.Rect;

/** Pure-Java regression tests for the Realms notification overlay coordinate conversion. */
public final class RealmsNotificationAnchorTest {

    private RealmsNotificationAnchorTest() {
    }

    public static void runAll() {
        followsRepositionedRealmsButton();
        fallsBackWhenRealmsButtonIsAbsent();
    }

    private static void followsRepositionedRealmsButton() {
        Rect realms = new Rect(600, 300, 204, 20);
        LayoutTestSuite.assertEquals(252, RealmsNotificationAnchor.verticalBase(realms, 138), "Realms overlay y base");
        LayoutTestSuite.assertEquals(804, RealmsNotificationAnchor.horizontalBase(realms, 740), "Realms overlay x base");
    }

    private static void fallsBackWhenRealmsButtonIsAbsent() {
        LayoutTestSuite.assertEquals(138, RealmsNotificationAnchor.verticalBase(null, 138), "missing Realms y fallback");
        LayoutTestSuite.assertEquals(740, RealmsNotificationAnchor.horizontalBase(null, 740), "missing Realms x fallback");
    }
}

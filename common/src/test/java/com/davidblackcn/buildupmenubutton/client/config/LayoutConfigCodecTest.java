package com.davidblackcn.buildupmenubutton.client.config;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutTestSuite;
import java.util.Properties;

/** Pure-Java coverage for defaults and the two independent layout switches. */
public final class LayoutConfigCodecTest {

    private LayoutConfigCodecTest() {
    }

    public static void runAll() {
        defaultsEnableBothLayouts();
        eachLayoutCanBeDisabledIndependently();
        malformedValuesFallBackToDefaults();
    }

    private static void defaultsEnableBothLayouts() {
        LayoutConfig config = LayoutConfigCodec.fromProperties(new Properties());
        LayoutTestSuite.assertTrue(config.titleScreenLayoutEnabled(), "title layout default enabled");
        LayoutTestSuite.assertTrue(config.pauseScreenLayoutEnabled(), "pause layout default enabled");
    }

    private static void eachLayoutCanBeDisabledIndependently() {
        Properties properties = new Properties();
        properties.setProperty(LayoutConfigCodec.TITLE_SCREEN_LAYOUT_KEY, "false");
        LayoutConfig titleDisabled = LayoutConfigCodec.fromProperties(properties);
        LayoutTestSuite.assertTrue(!titleDisabled.titleScreenLayoutEnabled(), "title layout disabled");
        LayoutTestSuite.assertTrue(titleDisabled.pauseScreenLayoutEnabled(), "pause layout remains enabled");

        properties.clear();
        properties.setProperty(LayoutConfigCodec.PAUSE_SCREEN_LAYOUT_KEY, "false");
        LayoutConfig pauseDisabled = LayoutConfigCodec.fromProperties(properties);
        LayoutTestSuite.assertTrue(pauseDisabled.titleScreenLayoutEnabled(), "title layout remains enabled");
        LayoutTestSuite.assertTrue(!pauseDisabled.pauseScreenLayoutEnabled(), "pause layout disabled");
    }

    private static void malformedValuesFallBackToDefaults() {
        Properties properties = new Properties();
        properties.setProperty(LayoutConfigCodec.TITLE_SCREEN_LAYOUT_KEY, "sometimes");
        properties.setProperty(LayoutConfigCodec.PAUSE_SCREEN_LAYOUT_KEY, "0");
        LayoutConfig config = LayoutConfigCodec.fromProperties(properties);
        LayoutTestSuite.assertTrue(config.titleScreenLayoutEnabled(), "malformed title value uses default");
        LayoutTestSuite.assertTrue(config.pauseScreenLayoutEnabled(), "malformed pause value uses default");
    }
}

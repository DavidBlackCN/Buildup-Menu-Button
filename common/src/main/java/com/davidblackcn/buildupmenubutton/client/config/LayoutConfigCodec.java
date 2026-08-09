package com.davidblackcn.buildupmenubutton.client.config;

import java.util.Properties;

/** Reads the supported layout switches while preserving safe defaults for absent or malformed values. */
public final class LayoutConfigCodec {

    public static final String TITLE_SCREEN_LAYOUT_KEY = "title_screen_layout_optimization";
    public static final String PAUSE_SCREEN_LAYOUT_KEY = "pause_screen_layout_optimization";

    private LayoutConfigCodec() {
    }

    public static LayoutConfig fromProperties(Properties properties) {
        return new LayoutConfig(
                readBoolean(properties, TITLE_SCREEN_LAYOUT_KEY, LayoutConfig.DEFAULT.titleScreenLayoutEnabled()),
                readBoolean(properties, PAUSE_SCREEN_LAYOUT_KEY, LayoutConfig.DEFAULT.pauseScreenLayoutEnabled()));
    }

    private static boolean readBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(value.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(value.trim())) {
            return false;
        }
        return defaultValue;
    }
}

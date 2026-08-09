package com.davidblackcn.buildupmenubutton;

import com.davidblackcn.buildupmenubutton.client.ScreenLayoutController;
import com.davidblackcn.buildupmenubutton.client.config.LayoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BuildupMenuButton {
    public static final String MOD_ID = "buildup_menu_button";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ScreenLayoutController CONTROLLER = new ScreenLayoutController();

    private BuildupMenuButton() {
    }

    public static void init() {
        CONTROLLER.initialize();
    }

    public static ScreenLayoutController getController() {
        return CONTROLLER;
    }

    public static LayoutConfig getConfig() {
        return CONTROLLER.config();
    }

    public static void updateConfig(LayoutConfig config) {
        CONTROLLER.updateConfig(config);
    }
}

package com.davidblackcn.buildupmenubutton;

import com.davidblackcn.buildupmenubutton.client.ScreenLayoutController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BuildupMenuButton {
    public static final String MOD_ID = "buildup_menu_button";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ScreenLayoutController CONTROLLER = new ScreenLayoutController();

    private BuildupMenuButton() {
    }

    public static void init() {
        // No loader-specific work is needed here; controller state is created lazily.
    }

    public static ScreenLayoutController getController() {
        return CONTROLLER;
    }
}

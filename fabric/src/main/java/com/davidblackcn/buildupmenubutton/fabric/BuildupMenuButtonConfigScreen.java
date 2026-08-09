package com.davidblackcn.buildupmenubutton.fabric;

import com.davidblackcn.buildupmenubutton.BuildupMenuButton;
import com.davidblackcn.buildupmenubutton.client.config.LayoutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small dependency-free configuration screen exposed through Mod Menu on Fabric. */
public final class BuildupMenuButtonConfigScreen extends Screen {

    private static final String TITLE_KEY = "config.buildup_menu_button.title";
    private static final String TITLE_LAYOUT_KEY = "config.buildup_menu_button.title_screen_layout_optimization";
    private static final String PAUSE_LAYOUT_KEY = "config.buildup_menu_button.pause_screen_layout_optimization";
    private static final String DONE_KEY = "config.buildup_menu_button.done";

    private final Screen parent;
    private boolean titleScreenLayoutEnabled;
    private boolean pauseScreenLayoutEnabled;
    private Button titleScreenButton;
    private Button pauseScreenButton;

    public BuildupMenuButtonConfigScreen(Screen parent) {
        super(Component.translatable(TITLE_KEY));
        this.parent = parent;
        LayoutConfig config = BuildupMenuButton.getConfig();
        titleScreenLayoutEnabled = config.titleScreenLayoutEnabled();
        pauseScreenLayoutEnabled = config.pauseScreenLayoutEnabled();
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 4 + 24;
        titleScreenButton = addRenderableWidget(Button.builder(titleScreenLabel(), ignored -> {
            titleScreenLayoutEnabled = !titleScreenLayoutEnabled;
            titleScreenButton.setMessage(titleScreenLabel());
        }).bounds(x, y, 200, 20).build());
        pauseScreenButton = addRenderableWidget(Button.builder(pauseScreenLabel(), ignored -> {
            pauseScreenLayoutEnabled = !pauseScreenLayoutEnabled;
            pauseScreenButton.setMessage(pauseScreenLabel());
        }).bounds(x, y + 24, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(DONE_KEY), ignored -> saveAndClose())
                .bounds(x, y + 56, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private Component titleScreenLabel() {
        return toggleLabel(TITLE_LAYOUT_KEY, titleScreenLayoutEnabled);
    }

    private Component pauseScreenLabel() {
        return toggleLabel(PAUSE_LAYOUT_KEY, pauseScreenLayoutEnabled);
    }

    private static Component toggleLabel(String key, boolean enabled) {
        return Component.translatable(key).append(": ")
                .append(Component.translatable(enabled ? "options.on" : "options.off"));
    }

    private void saveAndClose() {
        BuildupMenuButton.updateConfig(new LayoutConfig(titleScreenLayoutEnabled, pauseScreenLayoutEnabled));
        minecraft.gui.setScreen(parent);
    }
}

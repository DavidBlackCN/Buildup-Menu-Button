package com.davidblackcn.buildupmenubutton.client;

import com.davidblackcn.buildupmenubutton.client.layout.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** Locates the actual Realms button that the title-screen layout manager has repositioned. */
public final class TitleScreenRealmsButtonLocator {

    private static final String REALMS_KEY = "menu.online";

    private TitleScreenRealmsButtonLocator() {
    }

    public static Rect findRealmsButton() {
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null || screen.getClass() != TitleScreen.class) {
            return null;
        }
        for (Object listener : screen.children()) {
            if (listener instanceof Button button && button.visible && REALMS_KEY.equals(translationKey(button.getMessage()))) {
                return new Rect(button.getX(), button.getY(), button.getWidth(), button.getHeight());
            }
        }
        return null;
    }

    private static String translationKey(Component component) {
        if (component != null && component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        return null;
    }
}

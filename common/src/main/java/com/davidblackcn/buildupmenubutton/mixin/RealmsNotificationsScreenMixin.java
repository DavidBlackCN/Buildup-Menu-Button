package com.davidblackcn.buildupmenubutton.mixin;

import com.davidblackcn.buildupmenubutton.client.RealmsNotificationAnchor;
import com.davidblackcn.buildupmenubutton.client.TitleScreenRealmsButtonLocator;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Synchronizes the Realms notification overlay with a repositioned {@code menu.online} button. */
@Mixin(value = RealmsNotificationsScreen.class, remap = false)
public abstract class RealmsNotificationsScreenMixin {

    /**
     * Targets local slot 4 in 26.2's extractIcons: {@code height / 4 + 48}. The notification
     * sprites are subsequently drawn at this value plus 50, i.e. two pixels below the button top.
     */
    @ModifyVariable(
            method = "extractIcons(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("STORE"),
            index = 4,
            require = 1,
            remap = false)
    private int buildupMenuButton$syncNotificationY(int vanillaBaseY) {
        return RealmsNotificationAnchor.verticalBase(TitleScreenRealmsButtonLocator.findRealmsButton(), vanillaBaseY);
    }

    /**
     * Targets local slot 5 in 26.2's extractIcons: {@code width / 2 + 100}, the original Realms
     * button's right edge.
     */
    @ModifyVariable(
            method = "extractIcons(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("STORE"),
            index = 5,
            require = 1,
            remap = false)
    private int buildupMenuButton$syncNotificationX(int vanillaBaseX) {
        return RealmsNotificationAnchor.horizontalBase(TitleScreenRealmsButtonLocator.findRealmsButton(), vanillaBaseX);
    }
}

package com.davidblackcn.buildupmenubutton.fabric;

import com.davidblackcn.buildupmenubutton.BuildupMenuButton;
import com.davidblackcn.buildupmenubutton.client.ScreenLayoutController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.Screen;

/**
 * Fabric 客户端入口：在主菜单/暂停菜单初始化后请求布局，tick 时做轻量指纹检查，
 * 屏幕关闭时清理状态。仅注册原生 {@link net.minecraft.client.gui.screens.TitleScreen}
 * 与 {@link net.minecraft.client.gui.screens.PauseScreen}。
 */
public final class BuildupMenuButtonFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenLayoutController controller = BuildupMenuButton.getController();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!controller.accepts(screen)) {
                return;
            }
            controller.requestLayout(screen);
            // Screen.init（包含窗口缩放）会清空屏幕级事件；必须在每次 AFTER_INIT 中重新绑定。
            ScreenEvents.beforeExtract(screen).register((currentScreen, extractor, mouseX, mouseY, tickDelta) ->
                    controller.applyBeforeExtract(currentScreen));
            ScreenEvents.remove(screen).register(controller::onScreenRemoved);
        });
    }
}

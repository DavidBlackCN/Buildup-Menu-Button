package com.davidblackcn.buildupmenubutton.neoforge;

import com.davidblackcn.buildupmenubutton.BuildupMenuButton;
import com.davidblackcn.buildupmenubutton.client.ScreenLayoutController;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge 入口：仅客户端加载。主菜单/暂停菜单初始化后请求布局（LOWEST 优先级，
 * 保证其他 Mod 先加完按钮再采集），渲染后做轻量指纹检查，关闭时清理状态。
 */
@Mod(BuildupMenuButton.MOD_ID)
public final class BuildupMenuButtonNeoForge {

    public BuildupMenuButtonNeoForge() {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return;
        }
        ScreenLayoutController controller = BuildupMenuButton.getController();

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ScreenEvent.Init.Post.class, event -> {
            if (controller.accepts(event.getScreen())) {
                controller.requestLayout(event.getScreen());
            }
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ScreenEvent.Render.Post.class, event -> {
            if (controller.accepts(event.getScreen())) {
                controller.onFrame(event.getScreen());
            }
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ScreenEvent.Closing.class, event -> {
            if (controller.accepts(event.getScreen())) {
                controller.onScreenRemoved(event.getScreen());
            }
        });
    }
}

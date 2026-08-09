package com.davidblackcn.buildupmenubutton.client;

import com.davidblackcn.buildupmenubutton.client.profile.ButtonSnapshot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * 从 {@link Screen#children()} 采集所有可见 {@link Button}（排除版权 {@link PlainTextButton}），
 * 生成与真实控件列表一一对应的快照列表。
 *
 * <p>规划与指纹都基于采集结果：快照只读，真实控件引用与快照保持相同顺序，供
 * {@link LayoutApplier} 写回几何。</p>
 */
public final class ScreenButtonCollector {

    /**
     * @param screen 目标屏幕
     * @return 快照列表与真实控件列表（同序）
     */
    public CollectResult collect(Screen screen) {
        List<ButtonSnapshot> snapshots = new ArrayList<>();
        List<Button> widgets = new ArrayList<>();
        for (Object listener : screen.children()) {
            if (listener instanceof Button button && !(button instanceof PlainTextButton) && button.visible) {
                Component message = button.getMessage();
                ButtonSnapshot snapshot = new ButtonSnapshot(
                        snapshots.size(),
                        button.getClass().getName(),
                        translationKey(message),
                        button instanceof SpriteIconButton,
                        button.getX(),
                        button.getY(),
                        button.getWidth(),
                        button.getHeight(),
                        button.active,
                        true);
                snapshots.add(snapshot);
                widgets.add(button);
            }
        }
        return new CollectResult(List.copyOf(snapshots), List.copyOf(widgets));
    }

    private static String translationKey(Component component) {
        if (component == null) {
            return null;
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        return null;
    }

    /** 采集结果：快照与真实控件同序一一对应。 */
    public record CollectResult(List<ButtonSnapshot> snapshots, List<Button> widgets) {
    }
}

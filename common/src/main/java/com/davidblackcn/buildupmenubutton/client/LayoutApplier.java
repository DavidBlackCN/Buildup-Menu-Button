package com.davidblackcn.buildupmenubutton.client;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutPlan;
import com.davidblackcn.buildupmenubutton.client.layout.Rect;
import com.davidblackcn.buildupmenubutton.client.layout.WidgetPlacement;
import java.util.List;
import net.minecraft.client.gui.components.Button;

/**
 * 把已校验的 {@link LayoutPlan} 写回真实控件。只改几何（x/y/width/height），
 * 不触碰实例、回调、Tooltip、active/visible 等状态。
 */
public final class LayoutApplier {

    /**
     * @param widgets 与快照同序的真实控件列表
     * @param plan    已校验的布局计划
     * @return 是否完整写入
     */
    public boolean apply(List<Button> widgets, LayoutPlan plan) {
        if (plan.isFailedOpen() || !plan.isValid()) {
            return false;
        }
        for (WidgetPlacement placement : plan.placements()) {
            int index = placement.index();
            if (index < 0 || index >= widgets.size()) {
                return false;
            }
            Rect rect = placement.rect();
            Button widget = widgets.get(index);
            widget.setX(rect.x());
            widget.setY(rect.y());
            widget.setWidth(rect.width());
            widget.setHeight(rect.height());
        }
        return true;
    }
}

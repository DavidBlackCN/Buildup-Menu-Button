package com.davidblackcn.buildupmenubutton.client.profile;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutConstraints;
import com.davidblackcn.buildupmenubutton.client.layout.LayoutItem;
import com.davidblackcn.buildupmenubutton.client.layout.Rect;
import com.davidblackcn.buildupmenubutton.client.layout.WrapGridLayout;
import java.util.ArrayList;
import java.util.List;

public interface ScreenLayoutProfile {
    RoleAssignment classify(LayoutItem item);

    /**
     * Classifies one screen snapshot at a time. Most profiles can classify each item independently;
     * profiles with widgets whose role depends on a sibling may override this method.
     */
    default List<RoleAssignment> classifyAll(List<? extends LayoutItem> items) {
        List<RoleAssignment> assignments = new ArrayList<>(items.size());
        for (LayoutItem item : items) {
            assignments.add(classify(item));
        }
        return assignments;
    }

    LayoutConstraints constraints(int screenWidth, int screenHeight);

    Rect auxiliaryArea(
            int screenWidth,
            int screenHeight,
            LayoutConstraints constraints,
            Rect coreArea,
            int auxiliaryItemCount);

    int auxiliaryMaxWidth(LayoutConstraints constraints);

    /** Returns a per-screen adjustment to the normal core-menu vertical anchor. */
    default int coreHeaderOffset(List<? extends LayoutItem> items, LayoutConstraints constraints) {
        return 0;
    }

    /** Whether compact widgets should be placed around the core rows instead of in the generic grid. */
    default boolean compactButtonsUseCoreRows() {
        return false;
    }

    /** Whether compact buttons should fill pause-menu rows at the core's left and right sides. */
    default boolean compactButtonsUsePauseColumns() {
        return false;
    }

    /**
     * 两个核心行之间的垂直间距。标题界面的最后一个全宽主按钮与选项行之间
     * 使用原生经典菜单的较大间隔；其他 Screen 保持普通按钮间距。
     */
    default int coreRowSpacing(ButtonRole completedRole, ButtonRole nextRole, LayoutConstraints constraints) {
        return constraints.spacing();
    }

    /** Whether an overflowing core layout may reduce profile-specific large row gaps as a final fallback. */
    default boolean compactRoleGapsWhenCoreOverflows() {
        return false;
    }

    /** 辅助紧凑区网格的水平对齐方式，默认左对齐。 */
    default WrapGridLayout.HAlign auxiliaryHAlign() {
        return WrapGridLayout.HAlign.LEFT;
    }

    /** 辅助紧凑区网格的垂直对齐方式，默认顶部对齐。 */
    default WrapGridLayout.VAlign auxiliaryVAlign() {
        return WrapGridLayout.VAlign.TOP;
    }
}

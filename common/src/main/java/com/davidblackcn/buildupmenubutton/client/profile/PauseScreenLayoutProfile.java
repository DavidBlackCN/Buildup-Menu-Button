package com.davidblackcn.buildupmenubutton.client.profile;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutConstraints;
import com.davidblackcn.buildupmenubutton.client.layout.LayoutItem;
import com.davidblackcn.buildupmenubutton.client.layout.Rect;
import com.davidblackcn.buildupmenubutton.client.layout.WrapGridLayout;
import java.util.Map;
import java.util.Set;

/**
 * 暂停菜单（{@code PauseScreen}）的按钮识别与区域规划。
 *
 * <p>核心区按原生网格顺序识别：returnToGame 全宽，advancements/stats 半宽行，
 * options/multiplayerOptions 半宽（缺项时由布局管理器补全宽），returnToMenu/disconnect
 * 全宽。原生反馈、举报和社交小图标优先沿核心左侧按行排列；其他紧凑图标补满左列后，
 * 再从核心右侧顶部开始按行排列，
 * 服务器自定义对话框按钮等第三方普通按钮归为扩展区。</p>
 */
public final class PauseScreenLayoutProfile implements ScreenLayoutProfile {

    private static final String MOD_MENU_KEY = "modmenu.title";
    private static final Set<String> NATIVE_COMPACT_KEYS = Set.of(
            "menu.reportBugs",
            "menu.sendFeedback",
            "gui.friends.open",
            "menu.playerReporting");

    private static final Map<String, Integer> FULL_KEYS = Map.of(
            "menu.returnToGame", 0,
            "menu.returnToMenu", 6,
            "menu.disconnect", 6);

    private static final Map<String, Integer> HALF_KEYS = Map.of(
            "gui.advancements", 1,
            "gui.stats", 2,
            "menu.options", 5,
            "menu.multiplayerOptions.button", 5);

    @Override
    public RoleAssignment classify(LayoutItem item) {
        String key = item.translationKey();
        Integer order = key == null ? null : FULL_KEYS.get(key);
        if (order != null) {
            return new RoleAssignment(ButtonRole.CORE_FULL, order);
        }
        order = key == null ? null : HALF_KEYS.get(key);
        if (order != null) {
            return new RoleAssignment(ButtonRole.CORE_HALF, order);
        }
        if (key != null && NATIVE_COMPACT_KEYS.contains(key)) {
            return new RoleAssignment(ButtonRole.PAUSE_NATIVE_AUXILIARY, Integer.MAX_VALUE);
        }
        if (MOD_MENU_KEY.equals(key)) {
            if (item.iconLike() || (item.width() > 0 && item.width() <= 28 && item.height() > 0 && item.height() <= 28)) {
                return new RoleAssignment(ButtonRole.AUXILIARY_COMPACT, Integer.MAX_VALUE);
            }
            // Mod Menu's INSERT setting adds a 204x20 widget after the advancements/statistics row.
            return new RoleAssignment(ButtonRole.CORE_FULL, 3);
        }
        if (item.iconLike()) {
            return new RoleAssignment(ButtonRole.AUXILIARY_COMPACT, Integer.MAX_VALUE);
        }
        if (item.width() > 0 && item.height() > 0 && item.width() <= 28 && item.height() <= 28) {
            return new RoleAssignment(ButtonRole.AUXILIARY_COMPACT, Integer.MAX_VALUE);
        }
        return new RoleAssignment(ButtonRole.EXTENSION, Integer.MAX_VALUE);
    }

    @Override
    public LayoutConstraints constraints(int screenWidth, int screenHeight) {
        return LayoutConstraints.pauseScreen(screenWidth, screenHeight);
    }

    @Override
    public boolean compactButtonsUsePauseColumns() {
        return true;
    }

    @Override
    public Rect auxiliaryArea(
            int screenWidth,
            int screenHeight,
            LayoutConstraints constraints,
            Rect coreArea,
            int auxiliaryItemCount) {
        int maxW = auxiliaryMaxWidth(constraints);
        int x = coreArea.right() + constraints.spacing();
        int availW = screenWidth - constraints.safeMargin() - x;
        if (availW <= 0) {
            return new Rect(x, coreArea.y(), 0, 0);
        }
        int width = Math.min(maxW, availW);
        int y = coreArea.y();
        int height = screenHeight - constraints.footerReserve() - y;
        return new Rect(x, y, width, Math.max(1, height));
    }

    @Override
    public int auxiliaryMaxWidth(LayoutConstraints constraints) {
        int cell = constraints.targetHeight() + 4;
        return constraints.maxColumns() * cell + (constraints.maxColumns() - 1) * constraints.spacing();
    }

    @Override
    public WrapGridLayout.HAlign auxiliaryHAlign() {
        return WrapGridLayout.HAlign.LEFT;
    }

    @Override
    public WrapGridLayout.VAlign auxiliaryVAlign() {
        // 与“回到游戏”顶边对齐，再按 20px 高 + 4px 间距向下排列。
        return WrapGridLayout.VAlign.TOP;
    }
}

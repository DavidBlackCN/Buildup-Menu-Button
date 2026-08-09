package com.davidblackcn.buildupmenubutton.client.profile;

import com.davidblackcn.buildupmenubutton.client.layout.LayoutConstraints;
import com.davidblackcn.buildupmenubutton.client.layout.LayoutItem;
import com.davidblackcn.buildupmenubutton.client.layout.Rect;
import com.davidblackcn.buildupmenubutton.client.layout.WrapGridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主菜单（{@code TitleScreen}）的按钮识别与区域规划。
 *
 * <p>核心区识别普通/演示两种分支：singleplayer/multiplayer/online（演示为 playdemo/resetdemo）
 * 为全宽项，options/quit 为半宽行。语言、无障碍等小图标从主体菜单最底行的左右两侧开始，
 * 每行最多各放一个并在数量较多时逐行向上；其余第三方普通按钮归为扩展区。</p>
 */
public final class TitleScreenLayoutProfile implements ScreenLayoutProfile {

    private static final int CLASSIC_ACTION_ROW_GAP = 18;
    private static final String FRIENDS_KEY = "gui.friends.open";
    private static final String LANGUAGE_KEY = "options.language";
    private static final String ACCESSIBILITY_KEY = "options.accessibility";
    private static final String ACCESSIBILITY_ONBOARDING_KEY = "accessibility.onboarding.accessibility.button";
    private static final String MOD_MENU_KEY = "modmenu.title";

    private static final Map<String, Integer> FULL_KEYS = Map.of(
            "menu.singleplayer", 0,
            "menu.multiplayer", 1,
            "menu.online", 2,
            "menu.playdemo", 0,
            "menu.resetdemo", 1);

    private static final Map<String, Integer> HALF_KEYS = Map.of(
            "menu.options", 3,
            "menu.quit", 4);

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
        if (FRIENDS_KEY.equals(key)) {
            return new RoleAssignment(ButtonRole.TITLE_FRIENDS, Integer.MAX_VALUE);
        }
        if (LANGUAGE_KEY.equals(key)) {
            return new RoleAssignment(ButtonRole.TITLE_LANGUAGE, Integer.MAX_VALUE);
        }
        if (ACCESSIBILITY_KEY.equals(key) || ACCESSIBILITY_ONBOARDING_KEY.equals(key)) {
            return new RoleAssignment(ButtonRole.TITLE_ACCESSIBILITY, Integer.MAX_VALUE);
        }
        if (MOD_MENU_KEY.equals(key)) {
            return new RoleAssignment(isModMenuIcon(item) ? ButtonRole.TITLE_MOD_MENU_ICON : ButtonRole.CORE_FULL, 2);
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
    public List<RoleAssignment> classifyAll(List<? extends LayoutItem> items) {
        boolean hasOnlineButton = items.stream().anyMatch(item -> "menu.online".equals(item.translationKey()));
        boolean hasShrinkModMenuButton = hasOnlineButton && items.stream().anyMatch(this::isModMenuShrinkButton);
        List<RoleAssignment> assignments = new ArrayList<>(items.size());

        for (LayoutItem item : items) {
            String key = item.translationKey();
            if (MOD_MENU_KEY.equals(key)) {
                if (isModMenuIcon(item)) {
                    assignments.add(new RoleAssignment(ButtonRole.TITLE_MOD_MENU_ICON, Integer.MAX_VALUE));
                } else if (hasShrinkModMenuButton && isModMenuShrinkButton(item)) {
                    assignments.add(new RoleAssignment(ButtonRole.TITLE_MOD_MENU_SHRINK_BUTTON, 2));
                } else {
                    // With menu.online present this is Mod Menu's CLASSIC style; without it the
                    // widget has replaced Realms. Both occupy the third full-width menu slot.
                    assignments.add(new RoleAssignment(ButtonRole.CORE_FULL, 2));
                }
            } else if (hasShrinkModMenuButton && "menu.online".equals(key)) {
                assignments.add(new RoleAssignment(ButtonRole.TITLE_MOD_MENU_SHRINK_REALMS, 2));
            } else {
                assignments.add(classify(item));
            }
        }
        return assignments;
    }

    @Override
    public int coreHeaderOffset(List<? extends LayoutItem> items, LayoutConstraints constraints) {
        boolean hasOnlineButton = items.stream().anyMatch(item -> "menu.online".equals(item.translationKey()));
        boolean hasClassicModMenuButton = hasOnlineButton && items.stream().anyMatch(item ->
                MOD_MENU_KEY.equals(item.translationKey())
                        && !isModMenuIcon(item)
                        && !isModMenuShrinkButton(item));
        // Mod Menu CLASSIC shifts the original TitleScreen buttons upward by half a 24px row
        // before inserting its own full-width widget beneath Realms.
        return hasClassicModMenuButton ? -12 : 0;
    }

    @Override
    public LayoutConstraints constraints(int screenWidth, int screenHeight) {
        return LayoutConstraints.titleScreen(screenWidth, screenHeight);
    }

    @Override
    public Rect auxiliaryArea(
            int screenWidth,
            int screenHeight,
            LayoutConstraints constraints,
            Rect coreArea,
            int auxiliaryItemCount) {
        int cellWidth = constraints.targetHeight() + constraints.spacing();
        int preferredColumns = Math.max(1, Math.min(
                constraints.maxColumns(),
                (coreArea.width() + constraints.spacing()) / cellWidth));
        int rows = Math.max(1, (auxiliaryItemCount + preferredColumns - 1) / preferredColumns);
        int preferredHeight = rows * constraints.targetHeight() + (rows - 1) * constraints.spacing();
        int y = coreArea.bottom() + constraints.spacing() * 2;
        int availableHeight = screenHeight - constraints.footerReserve() - y;

        // 区域宽度与核心菜单一致，确保原生的三个图标按钮显示为主体下方的一行，
        // 而非被高区域诱导为右下角单列。额外按钮仍可在此区域按行换行。
        return new Rect(coreArea.x(), y, coreArea.width(), Math.max(1, Math.min(preferredHeight, availableHeight)));
    }

    @Override
    public int auxiliaryMaxWidth(LayoutConstraints constraints) {
        return constraints.coreWidth();
    }

    @Override
    public boolean compactButtonsUseCoreRows() {
        return true;
    }

    @Override
    public int coreRowSpacing(ButtonRole completedRole, ButtonRole nextRole, LayoutConstraints constraints) {
        if (completedRole == ButtonRole.TITLE_MOD_MENU_SHRINK_BUTTON && nextRole == ButtonRole.CORE_HALF) {
            return CLASSIC_ACTION_ROW_GAP;
        }
        if (completedRole == ButtonRole.CORE_FULL && nextRole == ButtonRole.CORE_HALF) {
            return CLASSIC_ACTION_ROW_GAP;
        }
        return constraints.spacing();
    }

    @Override
    public boolean compactRoleGapsWhenCoreOverflows() {
        return true;
    }

    @Override
    public WrapGridLayout.HAlign auxiliaryHAlign() {
        return WrapGridLayout.HAlign.CENTER;
    }

    @Override
    public WrapGridLayout.VAlign auxiliaryVAlign() {
        return WrapGridLayout.VAlign.TOP;
    }

    private boolean isModMenuIcon(LayoutItem item) {
        return item.iconLike() || (item.width() > 0 && item.width() <= 28 && item.height() > 0 && item.height() <= 28);
    }

    private boolean isModMenuShrinkButton(LayoutItem item) {
        return MOD_MENU_KEY.equals(item.translationKey())
                && !isModMenuIcon(item)
                && item.width() > 28
                && item.width() <= 100
                && item.height() > 0
                && item.height() <= 28;
    }
}

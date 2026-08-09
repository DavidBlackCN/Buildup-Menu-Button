package com.davidblackcn.buildupmenubutton.client.layout;

import com.davidblackcn.buildupmenubutton.client.profile.PauseScreenLayoutProfile;
import com.davidblackcn.buildupmenubutton.client.profile.RoleAssignment;
import com.davidblackcn.buildupmenubutton.client.profile.ScreenLayoutProfile;
import com.davidblackcn.buildupmenubutton.client.profile.TitleScreenLayoutProfile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 布局不变式测试：多种屏幕尺寸 x 按钮组合下，计划要么 fail-open，要么完全有效——
 * 无越界、无重叠、无重复放置、所有项恰好被放置或标记为 unmanaged 之一，
 * 且核心区保持 coreOrder 垂直顺序。
 */
public final class LayoutInvariantTest {

    private static final DynamicButtonLayoutManager MANAGER = new DynamicButtonLayoutManager();
    private static final ScreenLayoutProfile TITLE = new TitleScreenLayoutProfile();
    private static final ScreenLayoutProfile PAUSE = new PauseScreenLayoutProfile();

    private static final int[][] SIZES = {
            {854, 480}, {1280, 720}, {320, 180}, {640, 360},
            {1280, 220}, {720, 200}, {1920, 1080},
    };

    private LayoutInvariantTest() {
    }

    public static void runAll() {
        for (int[] size : SIZES) {
            check(TITLE, titleNormal(), size[0], size[1]);
            check(TITLE, titleDemo(), size[0], size[1]);
            check(TITLE, titleWithExtAndIcons(), size[0], size[1]);
            check(PAUSE, pauseSingle(), size[0], size[1]);
            check(PAUSE, pauseLan(), size[0], size[1]);
            check(PAUSE, pauseWithIconsAndExt(), size[0], size[1]);
            check(PAUSE, pauseManyIcons(), size[0], size[1]);
            check(TITLE, List.of(), size[0], size[1]);
            check(TITLE, onlyUnknown(), size[0], size[1]);
        }
        System.out.println("LayoutInvariantTest: " + SIZES.length * 9 + " scenarios checked.");
    }

    private static void check(ScreenLayoutProfile profile, List<LayoutTestSuite.TestItem> items, int width, int height) {
        LayoutPlan plan = MANAGER.plan(profile, items, width, height);
        LayoutTestSuite.assertTrue(
                plan.isFailedOpen() || plan.isValid(),
                "plan must be valid or fail open (w=" + width + " h=" + height + " items=" + items.size() + ")");
        if (plan.isFailedOpen()) {
            LayoutTestSuite.assertTrue(plan.placements().isEmpty(), "failed-open plan has no placements");
            return;
        }

        // 所有放置下标唯一且落在有效范围
        Set<Integer> placed = new HashSet<>();
        for (WidgetPlacement placement : plan.placements()) {
            LayoutTestSuite.assertTrue(placement.index() >= 0 && placement.index() < items.size(),
                    "placement index in range");
            LayoutTestSuite.assertTrue(placed.add(placement.index()), "no duplicate placement index");
        }

        // 放置与 unmanaged 不相交，且并集覆盖全部项
        Set<Integer> unmanaged = new HashSet<>(plan.unmanagedIndices());
        for (int index : unmanaged) {
            LayoutTestSuite.assertTrue(index >= 0 && index < items.size(), "unmanaged index in range");
            LayoutTestSuite.assertTrue(!placed.contains(index), "index cannot be placed and unmanaged");
        }
        LayoutTestSuite.assertEquals(items.size(), placed.size() + unmanaged.size(),
                "every item placed or unmanaged (w=" + width + " h=" + height + ")");

        // 核心区按 coreOrder 垂直堆叠（y 单调不减）
        List<WidgetPlacement> core = new ArrayList<>();
        for (WidgetPlacement placement : plan.placements()) {
            if (placement.region() == LayoutRegion.CORE) {
                core.add(placement);
            }
        }
        int previousOrder = Integer.MIN_VALUE;
        int previousY = Integer.MIN_VALUE;
        for (WidgetPlacement placement : core) {
            RoleAssignment role = profile.classify(items.get(placement.index()));
            LayoutTestSuite.assertTrue(role.coreOrder() >= previousOrder,
                    "core order non-decreasing by y");
            previousOrder = role.coreOrder();
            LayoutTestSuite.assertTrue(placement.rect().y() >= previousY, "core y must be non-decreasing");
            previousY = placement.rect().y();
        }
    }

    private static List<LayoutTestSuite.TestItem> titleNormal() {
        return List.of(
                item("menu.singleplayer", 200), item("menu.multiplayer", 200), item("menu.online", 200),
                item("menu.options", 98), item("menu.quit", 98));
    }

    private static List<LayoutTestSuite.TestItem> titleDemo() {
        return List.of(item("menu.playdemo", 200), item("menu.resetdemo", 200),
                item("menu.options", 98), item("menu.quit", 98));
    }

    private static List<LayoutTestSuite.TestItem> titleWithExtAndIcons() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(titleNormal());
        items.add(item("menu.mods", 200));
        items.add(item("menu.settings", 160));
        for (int i = 0; i < 4; i++) {
            items.add(item("menu.language", 20));
        }
        return items;
    }

    private static List<LayoutTestSuite.TestItem> pauseSingle() {
        return List.of(item("menu.returnToGame", 204), item("gui.advancements", 100),
                item("gui.stats", 100), item("menu.options", 204), item("menu.returnToMenu", 204));
    }

    private static List<LayoutTestSuite.TestItem> pauseLan() {
        return List.of(item("menu.returnToGame", 204), item("gui.advancements", 100),
                item("gui.stats", 100), item("menu.multiplayerOptions.button", 100),
                item("menu.options", 100), item("menu.disconnect", 204));
    }

    private static List<LayoutTestSuite.TestItem> pauseWithIconsAndExt() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(pauseSingle());
        for (int i = 0; i < 8; i++) {
            items.add(item("menu.feedback", 20));
        }
        items.add(item("menu.serverDialog", 150));
        items.add(item("menu.thirdParty", 180));
        return items;
    }

    private static List<LayoutTestSuite.TestItem> pauseManyIcons() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(pauseSingle());
        for (int i = 0; i < 12; i++) {
            items.add(item("menu.feedback", 20));
        }
        return items;
    }

    private static List<LayoutTestSuite.TestItem> onlyUnknown() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            items.add(item("menu.unknown" + i, 150));
        }
        return items;
    }

    private static LayoutTestSuite.TestItem item(String key, int width) {
        return new LayoutTestSuite.TestItem(key, false, width, 20);
    }
}

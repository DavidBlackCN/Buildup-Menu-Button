package com.davidblackcn.buildupmenubutton.client.layout;

import com.davidblackcn.buildupmenubutton.client.profile.PauseScreenLayoutProfile;
import com.davidblackcn.buildupmenubutton.client.profile.ScreenLayoutProfile;
import com.davidblackcn.buildupmenubutton.client.profile.TitleScreenLayoutProfile;
import java.util.ArrayList;
import java.util.List;

/** {@link DynamicButtonLayoutManager} 的布局行为测试。 */
public final class DynamicButtonLayoutManagerTest {

    private static final DynamicButtonLayoutManager MANAGER = new DynamicButtonLayoutManager();
    private static final ScreenLayoutProfile TITLE = new TitleScreenLayoutProfile();
    private static final ScreenLayoutProfile PAUSE = new PauseScreenLayoutProfile();

    private DynamicButtonLayoutManagerTest() {
    }

    public static void runAll() {
        titleNormalCore();
        titleDemoCore();
        pauseCore();
        loneHalfBecomesFullWidth();
        pauseNativeCompactButtonsFillLeftThenRight();
        pauseNullTranslationKeyIsAdditionalCompact();
        extensionPlacedBelowCore();
        titleAuxiliaryAlignsWithCore();
        titleCompactButtonsUseBottomRowsFirst();
        titleModMenuStyles();
        titleModMenuShrinkFitsDefaultSmallWindowHeight();
        auxiliaryPlacedInAuxiliaryRegion();
        pauseAuxiliaryStartsAtCoreTop();
        pauseModMenuStyles();
        screenTooSmallFailsOpen();
        coreTooTallFailsOpen();
        auxiliaryOverflowUnmanagedKeepsCore();
        extensionOverflowUnmanagedKeepsCore();
    }

    private static LayoutTestSuite.TestItem item(String key, boolean icon, int width, int height) {
        return new LayoutTestSuite.TestItem(key, icon, width, height);
    }

    private static WidgetPlacement placementFor(LayoutPlan plan, int index) {
        for (WidgetPlacement placement : plan.placements()) {
            if (placement.index() == index) {
                return placement;
            }
        }
        return null;
    }

    private static void assertRect(Rect expected, Rect actual, String message) {
        LayoutTestSuite.assertEquals(expected.x(), actual.x(), message + " x");
        LayoutTestSuite.assertEquals(expected.y(), actual.y(), message + " y");
        LayoutTestSuite.assertEquals(expected.width(), actual.width(), message + " width");
        LayoutTestSuite.assertEquals(expected.height(), actual.height(), message + " height");
    }

    private static void titleNormalCore() {
        List<LayoutTestSuite.TestItem> items = List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20));
        LayoutPlan plan = MANAGER.plan(TITLE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen(), "title normal must not fail open");
        LayoutTestSuite.assertTrue(plan.isValid(), "title normal plan valid");
        LayoutTestSuite.assertEquals(5, plan.placements().size(), "title normal placement count");
        LayoutTestSuite.assertTrue(plan.unmanagedIndices().isEmpty(), "title normal no unmanaged");

        WidgetPlacement singleplayer = placementFor(plan, 0);
        assertRect(new Rect(540, 228, 200, 20), singleplayer.rect(), "singleplayer");
        LayoutTestSuite.assertEquals(LayoutRegion.CORE, singleplayer.region(), "singleplayer region");
        WidgetPlacement options = placementFor(plan, 3);
        assertRect(new Rect(540, 314, 98, 20), options.rect(), "options half");
        WidgetPlacement quit = placementFor(plan, 4);
        assertRect(new Rect(642, 314, 98, 20), quit.rect(), "quit half");
    }

    private static void titleDemoCore() {
        List<LayoutTestSuite.TestItem> items = List.of(
                item("menu.playdemo", false, 200, 20),
                item("menu.resetdemo", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20));
        LayoutPlan plan = MANAGER.plan(TITLE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "title demo valid");
        WidgetPlacement playdemo = placementFor(plan, 0);
        assertRect(new Rect(540, 228, 200, 20), playdemo.rect(), "playdemo full");
        WidgetPlacement options = placementFor(plan, 2);
        assertRect(new Rect(540, 290, 98, 20), options.rect(), "demo options half");
    }

    private static void pauseCore() {
        List<LayoutTestSuite.TestItem> items = List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("menu.options", false, 204, 20),
                item("menu.returnToMenu", false, 204, 20));
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "pause valid");
        WidgetPlacement returnToGame = placementFor(plan, 0);
        assertRect(new Rect(538, 182, 204, 20), returnToGame.rect(), "returnToGame uses native pause top");
        WidgetPlacement advancements = placementFor(plan, 1);
        assertRect(new Rect(538, 206, 100, 20), advancements.rect(), "advancements use native pause top");
        WidgetPlacement stats = placementFor(plan, 2);
        assertRect(new Rect(642, 206, 100, 20), stats.rect(), "stats use native pause top");
        WidgetPlacement returnToMenu = placementFor(plan, 4);
        assertRect(new Rect(538, 254, 204, 20), returnToMenu.rect(), "returnToMenu uses native pause top");
    }

    private static void loneHalfBecomesFullWidth() {
        // 只有 menu.options 一个半宽项（无 multiplayerOptions 配对）-> 自动全宽
        List<LayoutTestSuite.TestItem> items = List.of(
                item("menu.returnToGame", false, 204, 20),
                item("menu.options", false, 204, 20),
                item("menu.disconnect", false, 204, 20));
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 720);
        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "lone half valid");
        WidgetPlacement options = placementFor(plan, 1);
        LayoutTestSuite.assertEquals(204, options.rect().width(), "lone half becomes full width");
    }

    private static void pauseNativeCompactButtonsFillLeftThenRight() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("menu.options", false, 100, 20),
                item("menu.multiplayerOptions.button", false, 100, 20),
                item("menu.returnToMenu", false, 204, 20),
                item("menu.reportBugs", true, 20, 20),
                item("menu.sendFeedback", true, 20, 20),
                item("gui.friends.open", true, 20, 20),
                item("menu.playerReporting", true, 20, 20),
                item("modmenu.title", true, 20, 20)));
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "pause native compact valid");
        assertRect(new Rect(514, 182, 20, 20), placementFor(plan, 6).rect(), "report bugs on left top");
        assertRect(new Rect(514, 206, 20, 20), placementFor(plan, 7).rect(), "feedback below report bugs");
        assertRect(new Rect(514, 230, 20, 20), placementFor(plan, 8).rect(), "friends below feedback");
        assertRect(new Rect(514, 254, 20, 20), placementFor(plan, 9).rect(), "player reporting on left bottom");
        assertRect(new Rect(746, 182, 20, 20), placementFor(plan, 10).rect(), "extra mod icon starts at right top");
    }

    private static void pauseNullTranslationKeyIsAdditionalCompact() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("menu.options", false, 100, 20),
                item("menu.multiplayerOptions.button", false, 100, 20),
                item("menu.returnToMenu", false, 204, 20)));
        items.add(item(null, true, 20, 20));
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "null translation key compact button valid");
        LayoutTestSuite.assertTrue(!plan.unmanagedIndices().contains(6), "null translation key compact button placed");
        assertRect(new Rect(514, 182, 20, 20), placementFor(plan, 6).rect(), "null translation key uses left top");
    }

    private static void extensionPlacedBelowCore() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20)));
        items.add(item("menu.foo", false, 200, 20)); // index 5, 第三方扩展
        LayoutPlan plan = MANAGER.plan(TITLE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "extension valid");
        WidgetPlacement ext = placementFor(plan, 5);
        LayoutTestSuite.assertTrue(ext != null, "extension placed");
        LayoutTestSuite.assertEquals(LayoutRegion.EXTENSION, ext.region(), "extension region");
        // 经典标题菜单的选项行顶部为 314，底部为 334；扩展区从 334 + 2*4 = 342 开始
        assertRect(new Rect(540, 342, 200, 20), ext.rect(), "extension below core");
    }

    private static void titleAuxiliaryAlignsWithCore() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20)));
        items.add(item("gui.friends.open", true, 20, 20));
        items.add(item("options.language", true, 20, 20));
        items.add(item("options.accessibility", true, 20, 20));
        LayoutPlan plan = MANAGER.plan(TITLE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "title aux valid");
        WidgetPlacement friends = placementFor(plan, 5);
        WidgetPlacement language = placementFor(plan, 6);
        WidgetPlacement accessibility = placementFor(plan, 7);
        LayoutTestSuite.assertEquals(LayoutRegion.AUXILIARY, friends.region(), "friends region");
        LayoutTestSuite.assertEquals(LayoutRegion.AUXILIARY, language.region(), "language region");
        LayoutTestSuite.assertEquals(LayoutRegion.AUXILIARY, accessibility.region(), "accessibility region");
        assertRect(new Rect(744, 314, 20, 20), friends.rect(), "friends on bottom right");
        assertRect(new Rect(516, 314, 20, 20), language.rect(), "language on bottom left");
        assertRect(new Rect(744, 276, 20, 20), accessibility.rect(), "accessibility moves to the next row");
    }

    private static void titleCompactButtonsUseBottomRowsFirst() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20)));
        for (int i = 0; i < 9; i++) {
            items.add(item("menu.titleIcon" + i, true, 20, 20));
        }
        LayoutPlan plan = MANAGER.plan(TITLE, items, 640, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "title compact bottom-up valid");
        assertRect(new Rect(196, 314, 20, 20), placementFor(plan, 5).rect(), "first compact uses bottom left");
        assertRect(new Rect(424, 314, 20, 20), placementFor(plan, 6).rect(), "second compact uses bottom right");
        assertRect(new Rect(196, 276, 20, 20), placementFor(plan, 7).rect(), "third compact moves to the next row up");
        assertRect(new Rect(424, 228, 20, 20), placementFor(plan, 12).rect(), "eighth compact uses the top core row");
        LayoutTestSuite.assertTrue(plan.unmanagedIndices().contains(13), "ninth compact stays unmanaged after all rows are used");
    }

    private static void titleModMenuStyles() {
        List<LayoutTestSuite.TestItem> classic = List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 200, 20),
                item("modmenu.title", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20),
                item("gui.friends.open", true, 20, 20));
        LayoutPlan classicPlan = MANAGER.plan(TITLE, classic, 1280, 720);
        LayoutTestSuite.assertTrue(!classicPlan.isFailedOpen() && classicPlan.isValid(), "Mod Menu classic valid");
        assertRect(new Rect(540, 216, 200, 20), placementFor(classicPlan, 0).rect(), "classic singleplayer");
        assertRect(new Rect(540, 288, 200, 20), placementFor(classicPlan, 3).rect(), "classic mods below realms");
        assertRect(new Rect(540, 326, 98, 20), placementFor(classicPlan, 4).rect(), "classic options");
        assertRect(new Rect(744, 326, 20, 20), placementFor(classicPlan, 6).rect(), "classic friends on bottom row");

        List<LayoutTestSuite.TestItem> replaceRealms = List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("modmenu.title", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20));
        LayoutPlan replacePlan = MANAGER.plan(TITLE, replaceRealms, 1280, 720);
        LayoutTestSuite.assertTrue(!replacePlan.isFailedOpen() && replacePlan.isValid(), "Mod Menu replace realms valid");
        assertRect(new Rect(540, 276, 200, 20), placementFor(replacePlan, 2).rect(), "mods replaces realms");
        assertRect(new Rect(540, 314, 98, 20), placementFor(replacePlan, 3).rect(), "replace realms options");

        List<LayoutTestSuite.TestItem> shrink = List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 98, 20),
                item("modmenu.title", false, 98, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20));
        LayoutPlan shrinkPlan = MANAGER.plan(TITLE, shrink, 1280, 720);
        LayoutTestSuite.assertTrue(!shrinkPlan.isFailedOpen() && shrinkPlan.isValid(), "Mod Menu shrink valid");
        assertRect(new Rect(540, 276, 98, 20), placementFor(shrinkPlan, 2).rect(), "shrink realms left");
        assertRect(new Rect(642, 276, 98, 20), placementFor(shrinkPlan, 3).rect(), "shrink mods right");
        assertRect(new Rect(540, 314, 98, 20), placementFor(shrinkPlan, 4).rect(), "shrink options");

        List<LayoutTestSuite.TestItem> icon = new ArrayList<>(List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20),
                item("gui.friends.open", true, 20, 20)));
        icon.add(item("modmenu.title", true, 20, 20));
        LayoutPlan iconPlan = MANAGER.plan(TITLE, icon, 1280, 720);
        LayoutTestSuite.assertTrue(!iconPlan.isFailedOpen() && iconPlan.isValid(), "Mod Menu icon valid");
        assertRect(new Rect(744, 314, 20, 20), placementFor(iconPlan, 5).rect(), "friends on bottom row");
        assertRect(new Rect(516, 314, 20, 20), placementFor(iconPlan, 6).rect(), "mods icon left of friends");
    }

    private static void titleModMenuShrinkFitsDefaultSmallWindowHeight() {
        List<LayoutTestSuite.TestItem> items = List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.multiplayer", false, 200, 20),
                item("menu.online", false, 98, 20),
                item("modmenu.title", false, 98, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20));
        // 854x480 在 GUI 缩放为 2 时约为 427x240；此前 18px 经典间距会让该组合 fail-open。
        LayoutPlan plan = MANAGER.plan(TITLE, items, 427, 240);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "small window Mod Menu shrink valid");
        assertRect(new Rect(113, 174, 99, 20), placementFor(plan, 4).rect(), "small window options use compact fallback");
        assertRect(new Rect(214, 174, 99, 20), placementFor(plan, 5).rect(), "small window quit use compact fallback");
    }

    private static void auxiliaryPlacedInAuxiliaryRegion() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("menu.returnToMenu", false, 204, 20)));
        // 6 个图标按钮，索引 4..9
        for (int i = 0; i < 6; i++) {
            items.add(item("menu.icon" + i, true, 20, 20));
        }
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "aux valid");
        int auxCount = 0;
        for (int i = 4; i < 10; i++) {
            WidgetPlacement p = placementFor(plan, i);
            LayoutTestSuite.assertTrue(p != null, "aux item " + i + " placed");
            LayoutTestSuite.assertEquals(LayoutRegion.AUXILIARY, p.region(), "aux region " + i);
            LayoutTestSuite.assertTrue(p.rect().x() == 514 || p.rect().x() == 746, "aux beside core x");
            auxCount++;
        }
        LayoutTestSuite.assertEquals(6, auxCount, "all aux placed");
    }

    private static void pauseAuxiliaryStartsAtCoreTop() {
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("menu.options", false, 204, 20),
                item("menu.returnToMenu", false, 204, 20)));
        for (int i = 0; i < 3; i++) {
            items.add(item("menu.pauseIcon" + i, true, 20, 20));
        }
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 720);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen() && plan.isValid(), "pause aux top aligned valid");
        assertRect(new Rect(514, 182, 20, 20), placementFor(plan, 5).rect(), "pause icon one");
        assertRect(new Rect(514, 206, 20, 20), placementFor(plan, 6).rect(), "pause icon two");
        assertRect(new Rect(514, 230, 20, 20), placementFor(plan, 7).rect(), "pause icon three");
    }

    private static void pauseModMenuStyles() {
        List<LayoutTestSuite.TestItem> insert = List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("modmenu.title", false, 204, 20),
                item("menu.options", false, 100, 20),
                item("menu.multiplayerOptions.button", false, 100, 20),
                item("menu.returnToMenu", false, 204, 20));
        LayoutPlan insertPlan = MANAGER.plan(PAUSE, insert, 1280, 720);
        LayoutTestSuite.assertTrue(!insertPlan.isFailedOpen() && insertPlan.isValid(), "Mod Menu pause insert valid");
        assertRect(new Rect(538, 230, 204, 20), placementFor(insertPlan, 3).rect(), "pause mods inserted after stats");
        assertRect(new Rect(538, 254, 100, 20), placementFor(insertPlan, 4).rect(), "pause options after mods");
        assertRect(new Rect(538, 278, 204, 20), placementFor(insertPlan, 6).rect(), "pause return after mods");

        List<LayoutTestSuite.TestItem> icon = new ArrayList<>(List.of(
                item("menu.returnToGame", false, 204, 20),
                item("gui.advancements", false, 100, 20),
                item("gui.stats", false, 100, 20),
                item("menu.options", false, 100, 20),
                item("menu.multiplayerOptions.button", false, 100, 20),
                item("menu.returnToMenu", false, 204, 20)));
        icon.add(item("modmenu.title", true, 20, 20));
        LayoutPlan iconPlan = MANAGER.plan(PAUSE, icon, 1280, 720);
        LayoutTestSuite.assertTrue(!iconPlan.isFailedOpen() && iconPlan.isValid(), "Mod Menu pause icon valid");
        assertRect(new Rect(514, 182, 20, 20), placementFor(iconPlan, 6).rect(), "pause mods icon at left top");
    }

    private static void screenTooSmallFailsOpen() {
        LayoutPlan plan = MANAGER.plan(TITLE, List.of(item("menu.singleplayer", false, 200, 20)), 300, 150);
        LayoutTestSuite.assertTrue(plan.isFailedOpen(), "below minimum must fail open");
        LayoutTestSuite.assertTrue(plan.failureReason() != null, "failure reason present");
        LayoutTestSuite.assertTrue(plan.placements().isEmpty(), "no placements on fail open");
    }

    private static void coreTooTallFailsOpen() {
        // 720x200：10 个全宽核心按钮超出可用高度 -> 整屏 fail-open
        List<LayoutTestSuite.TestItem> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            items.add(item("menu.singleplayer", false, 200, 20));
        }
        LayoutPlan plan = MANAGER.plan(TITLE, items, 720, 200);
        LayoutTestSuite.assertTrue(plan.isFailedOpen(), "core overflow must fail open");
    }

    private static void auxiliaryOverflowUnmanagedKeepsCore() {
        // 1280x220 短屏，30 个图标超出辅助区容量（4 列 x 7 行 = 28）-> 全部保留原位，核心仍有效
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.returnToGame", false, 204, 20),
                item("menu.options", false, 204, 20),
                item("menu.returnToMenu", false, 204, 20)));
        for (int i = 0; i < 30; i++) {
            items.add(item("menu.icon" + i, true, 20, 20));
        }
        LayoutPlan plan = MANAGER.plan(PAUSE, items, 1280, 220);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen(), "aux overflow must not fail core");
        LayoutTestSuite.assertTrue(plan.isValid(), "aux overflow plan valid");
        long placedAuxiliary = plan.placements().stream()
                .filter(p -> p.region() == LayoutRegion.AUXILIARY)
                .count();
        LayoutTestSuite.assertEquals(6L, placedAuxiliary, "two pause compact columns use all three core rows");
        LayoutTestSuite.assertEquals(24, plan.unmanagedIndices().size(), "remaining 24 aux unmanaged");
        LayoutTestSuite.assertTrue(plan.unmanagedIndices().containsAll(List.of(9, 15, 32)), "overflow aux indices unmanaged");
        LayoutTestSuite.assertEquals(9, plan.placements().size(), "core and fitted aux placements intact");
    }

    private static void extensionOverflowUnmanagedKeepsCore() {
        // 1280x300 短屏：扩展区高仅 160，20 个 200px 宽文本按钮即使降级为
        // minSpacing 单列也放不下（总高 20*20+19*2=438 > 160），多列则超宽 -> 全部保留原位
        List<LayoutTestSuite.TestItem> items = new ArrayList<>(List.of(
                item("menu.singleplayer", false, 200, 20),
                item("menu.options", false, 98, 20),
                item("menu.quit", false, 98, 20)));
        for (int i = 0; i < 20; i++) {
            items.add(item("menu.ext" + i, false, 200, 20));
        }
        LayoutPlan plan = MANAGER.plan(TITLE, items, 1280, 300);

        LayoutTestSuite.assertTrue(!plan.isFailedOpen(), "ext overflow must not fail core");
        LayoutTestSuite.assertTrue(plan.isValid(), "ext overflow plan valid");
        LayoutTestSuite.assertTrue(plan.placements().stream()
                .noneMatch(p -> p.region() == LayoutRegion.EXTENSION), "no ext placements when overflowing");
        LayoutTestSuite.assertEquals(20, plan.unmanagedIndices().size(), "all 20 ext unmanaged");
        LayoutTestSuite.assertEquals(3, plan.placements().size(), "core placements intact");
    }
}

package com.davidblackcn.buildupmenubutton.client.layout;

import com.davidblackcn.buildupmenubutton.client.profile.ButtonRole;
import com.davidblackcn.buildupmenubutton.client.profile.RoleAssignment;
import com.davidblackcn.buildupmenubutton.client.profile.ScreenLayoutProfile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 动态按钮布局管理器：输入有序快照与屏幕尺寸，输出各项的新位置（{@link LayoutPlan}）。
 *
 * <p>纯 Java 实现，不依赖 Minecraft，可独立测试。降级顺序：核心区先减小间距，
 * 仍放不下则整屏 fail-open；辅助区/扩展区放不下时保留该项原位并计入
 * {@code unmanagedIndices}，不拖垮核心区。扩展文本按钮放不下时允许按可收窄宽度重试。</p>
 */
public final class DynamicButtonLayoutManager {

    public LayoutPlan plan(
            ScreenLayoutProfile profile,
            List<? extends LayoutItem> items,
            int screenWidth,
            int screenHeight) {
        LayoutConstraints c = profile.constraints(screenWidth, screenHeight);
        if (screenWidth < c.minScreenWidth() || screenHeight < c.minScreenHeight()) {
            return LayoutPlan.failedOpen(screenWidth, screenHeight,
                    "screen smaller than minimum (" + c.minScreenWidth() + "x" + c.minScreenHeight() + ")");
        }

        List<RoleAssignment> assignments = profile.classifyAll(items);
        if (assignments.size() != items.size()) {
            return LayoutPlan.failedOpen(screenWidth, screenHeight, "profile returned a mismatched classification count");
        }

        List<CoreEntry> core = new ArrayList<>();
        List<TitleIconEntry> titleCompactButtons = new ArrayList<>();
        List<Entry> pauseNativeCompactButtons = new ArrayList<>();
        List<Entry> pauseAdditionalCompactButtons = new ArrayList<>();
        List<Entry> aux = new ArrayList<>();
        List<Entry> ext = new ArrayList<>();
        Set<Integer> unmanaged = new LinkedHashSet<>();
        for (int i = 0; i < items.size(); i++) {
            LayoutItem item = items.get(i);
            RoleAssignment ra = assignments.get(i);
            switch (ra.role()) {
                case CORE_FULL, CORE_HALF, TITLE_MOD_MENU_SHRINK_REALMS, TITLE_MOD_MENU_SHRINK_BUTTON ->
                        core.add(new CoreEntry(i, item, ra.role(), ra.coreOrder()));
                case TITLE_FRIENDS, TITLE_LANGUAGE, TITLE_ACCESSIBILITY, TITLE_MOD_MENU_ICON ->
                        titleCompactButtons.add(new TitleIconEntry(i, item, ra.role()));
                case PAUSE_NATIVE_AUXILIARY -> pauseNativeCompactButtons.add(new Entry(i, item));
                case AUXILIARY_COMPACT -> {
                    if (profile.compactButtonsUseCoreRows()) {
                        titleCompactButtons.add(new TitleIconEntry(i, item, ra.role()));
                    } else if (profile.compactButtonsUsePauseColumns()) {
                        pauseAdditionalCompactButtons.add(new Entry(i, item));
                    } else {
                        aux.add(new Entry(i, item));
                    }
                }
                case EXTENSION -> ext.add(new Entry(i, item));
                default -> unmanaged.add(i);
            }
        }
        core.sort(Comparator.comparingInt(CoreEntry::order));

        int headerOffset = profile.coreHeaderOffset(items, c);
        CorePlacement corePlacement = placeCore(
                profile, core, c, screenWidth, screenHeight, c.spacing(), headerOffset, false);
        if (corePlacement == null) {
            // 屏幕高度临界时，标题页的经典大间距（例如 Mod Menu 并排模式后的 18px）
            // 可能是唯一导致核心区 fail-open 的原因。仅在此回退路径压缩角色专属间距。
            corePlacement = placeCore(
                    profile, core, c, screenWidth, screenHeight, c.minSpacing(), headerOffset,
                    profile.compactRoleGapsWhenCoreOverflows());
        }
        if (corePlacement == null) {
            return LayoutPlan.failedOpen(screenWidth, screenHeight, "core column does not fit available height");
        }
        List<WidgetPlacement> placements = new ArrayList<>(corePlacement.placements());
        Rect coreArea = corePlacement.area();
        int spacing = corePlacement.spacing();

        placeTitleCompactButtons(
                titleCompactButtons, corePlacement, c, screenWidth, screenHeight, placements, unmanaged);
        placePauseCompactButtons(
                pauseNativeCompactButtons, pauseAdditionalCompactButtons, corePlacement, c,
                screenWidth, screenHeight, placements, unmanaged);

        if (!aux.isEmpty()) {
            Rect auxArea = profile.auxiliaryArea(screenWidth, screenHeight, c, coreArea, aux.size());
            List<Rect> rects = WrapGridLayout.place(
                    aux, auxArea, spacing, c.maxColumns(),
                    profile.auxiliaryHAlign(), profile.auxiliaryVAlign());
            if (rects == null) {
                rects = WrapGridLayout.place(
                        aux, auxArea, c.minSpacing(), c.maxColumns(),
                        profile.auxiliaryHAlign(), profile.auxiliaryVAlign());
            }
            if (rects == null) {
                aux.forEach(e -> unmanaged.add(e.index()));
            } else {
                for (int k = 0; k < aux.size(); k++) {
                    placements.add(new WidgetPlacement(aux.get(k).index(), rects.get(k), LayoutRegion.AUXILIARY));
                }
            }
        }

        Rect extArea = extensionArea(c, screenWidth, screenHeight, coreArea, placements);
        if (!ext.isEmpty() && extArea != null && extArea.height() > 0 && extArea.width() >= c.minItemWidth()) {
            int columns = Math.min(c.maxColumns(), 3);
            List<Rect> rects = WrapGridLayout.place(ext, extArea, spacing, columns,
                    WrapGridLayout.HAlign.CENTER, WrapGridLayout.VAlign.TOP);
            if (rects == null) {
                List<LayoutItem> shrunk = shrinkText(ext, extArea.width(), c.minItemWidth());
                rects = WrapGridLayout.place(shrunk, extArea, c.minSpacing(), columns,
                        WrapGridLayout.HAlign.CENTER, WrapGridLayout.VAlign.TOP);
            }
            if (rects == null) {
                ext.forEach(e -> unmanaged.add(e.index()));
            } else {
                for (int k = 0; k < ext.size(); k++) {
                    placements.add(new WidgetPlacement(ext.get(k).index(), rects.get(k), LayoutRegion.EXTENSION));
                }
            }
        } else if (!ext.isEmpty()) {
            ext.forEach(e -> unmanaged.add(e.index()));
        }

        dropOverlaps(placements, unmanaged);

        LayoutPlan plan = new LayoutPlan(placements, new ArrayList<>(unmanaged), screenWidth, screenHeight, null, false);
        if (!plan.isValid()) {
            return LayoutPlan.failedOpen(screenWidth, screenHeight, "placement validation failed");
        }
        return plan;
    }

    /** 放置核心区：居中列，全宽项依次堆叠，半宽项两两成行。返回 null 表示垂直放不下。 */
    private static CorePlacement placeCore(
            ScreenLayoutProfile profile,
            List<CoreEntry> core,
            LayoutConstraints c,
            int screenWidth,
            int screenHeight,
            int spacing,
            int headerOffset,
            boolean compactRoleGaps) {
        int x = (screenWidth - c.coreWidth()) / 2;
        if (x < c.safeMargin()) {
            return null;
        }
        int coreHeight = coreHeight(profile, core, c, spacing, compactRoleGaps);
        int y = c.headerReserve() + headerOffset;
        if (y < c.safeMargin()) {
            return null;
        }
        int top = y;
        int limit = screenHeight - c.footerReserve();
        int lastBottom = y;
        List<WidgetPlacement> out = new ArrayList<>();
        int i = 0;
        while (i < core.size()) {
            CoreEntry e = core.get(i);
            if (isCoreFull(e.role())) {
                int h = e.item().height() > 0 ? e.item().height() : c.targetHeight();
                if (y + h > limit) {
                    return null;
                }
                out.add(new WidgetPlacement(e.index(), new Rect(x, y, c.coreWidth(), h), LayoutRegion.CORE));
                i++;
                lastBottom = y + h;
                y = lastBottom + coreRowSpacing(
                        profile, e.role(), i < core.size() ? core.get(i).role() : null, c, spacing, compactRoleGaps);
            } else {
                if (i + 1 < core.size() && isCoreHalf(core.get(i + 1).role())) {
                    CoreEntry a = core.get(i);
                    CoreEntry b = core.get(i + 1);
                    int halfW = (c.coreWidth() - spacing) / 2;
                    int aH = a.item().height() > 0 ? a.item().height() : c.targetHeight();
                    int bH = b.item().height() > 0 ? b.item().height() : c.targetHeight();
                    int rowH = Math.max(aH, bH);
                    if (y + rowH > limit) {
                        return null;
                    }
                    out.add(new WidgetPlacement(a.index(), new Rect(x, y + (rowH - aH) / 2, halfW, aH),
                            LayoutRegion.CORE));
                    out.add(new WidgetPlacement(b.index(), new Rect(x + halfW + spacing, y + (rowH - bH) / 2, halfW, bH),
                            LayoutRegion.CORE));
                    i += 2;
                    lastBottom = y + rowH;
                    y = lastBottom + coreRowSpacing(
                            profile, b.role(), i < core.size() ? core.get(i).role() : null, c, spacing, compactRoleGaps);
                } else {
                    int h = e.item().height() > 0 ? e.item().height() : c.targetHeight();
                    if (y + h > limit) {
                        return null;
                    }
                    out.add(new WidgetPlacement(e.index(), new Rect(x, y, c.coreWidth(), h), LayoutRegion.CORE));
                    i++;
                    lastBottom = y + h;
                    y = lastBottom + coreRowSpacing(
                            profile, e.role(), i < core.size() ? core.get(i).role() : null, c, spacing, compactRoleGaps);
                }
            }
        }
        Rect area = new Rect(x, top, c.coreWidth(), Math.max(1, lastBottom - top));
        return new CorePlacement(out, area, spacing);
    }

    private static int coreHeight(
            ScreenLayoutProfile profile,
            List<CoreEntry> core,
            LayoutConstraints c,
            int spacing,
            boolean compactRoleGaps) {
        int height = 0;
        int i = 0;
        while (i < core.size()) {
            CoreEntry entry = core.get(i);
            int rowHeight;
            ButtonRole completedRole;
            if (isCoreFull(entry.role())) {
                rowHeight = itemHeight(entry.item(), c);
                completedRole = entry.role();
                i++;
            } else if (i + 1 < core.size() && isCoreHalf(core.get(i + 1).role())) {
                CoreEntry next = core.get(i + 1);
                rowHeight = Math.max(itemHeight(entry.item(), c), itemHeight(next.item(), c));
                completedRole = next.role();
                i += 2;
            } else {
                rowHeight = itemHeight(entry.item(), c);
                completedRole = entry.role();
                i++;
            }
            height += rowHeight;
            if (i < core.size()) {
                height += coreRowSpacing(profile, completedRole, core.get(i).role(), c, spacing, compactRoleGaps);
            }
        }
        return height;
    }

    private static int itemHeight(LayoutItem item, LayoutConstraints constraints) {
        return item.height() > 0 ? item.height() : constraints.targetHeight();
    }

    private static int coreRowSpacing(
            ScreenLayoutProfile profile,
            ButtonRole completedRole,
            ButtonRole nextRole,
            LayoutConstraints constraints,
            int spacing,
            boolean compactRoleGaps) {
        int requested = profile.coreRowSpacing(completedRole, nextRole, constraints);
        return compactRoleGaps ? Math.min(requested, spacing) : requested;
    }

    /** Pause-menu compact buttons fill the core's left column first, then the right column from its top. */
    private static void placePauseCompactButtons(
            List<Entry> nativeButtons,
            List<Entry> additionalButtons,
            CorePlacement corePlacement,
            LayoutConstraints constraints,
            int screenWidth,
            int screenHeight,
            List<WidgetPlacement> placements,
            Set<Integer> unmanaged) {
        if (nativeButtons.isEmpty() && additionalButtons.isEmpty()) {
            return;
        }

        List<Entry> ordered = new ArrayList<>(nativeButtons.size() + additionalButtons.size());
        ordered.addAll(nativeButtons);
        ordered.addAll(additionalButtons);
        List<Integer> rowYs = coreRowYsTopFirst(corePlacement.placements());
        int leftRow = 0;
        int rightRow = 0;
        for (Entry entry : ordered) {
            int leftX = corePlacement.area().x() - constraints.spacing() - entry.item().width();
            if (leftRow < rowYs.size()
                    && fitsTitleCompactButton(leftX, rowYs.get(leftRow), entry.item(), screenWidth, screenHeight, constraints)) {
                placements.add(new WidgetPlacement(entry.index(), new Rect(
                        leftX, rowYs.get(leftRow), entry.item().width(), entry.item().height()), LayoutRegion.AUXILIARY));
                leftRow++;
                continue;
            }

            int rightX = corePlacement.area().right() + constraints.spacing();
            if (rightRow < rowYs.size()
                    && fitsTitleCompactButton(rightX, rowYs.get(rightRow), entry.item(), screenWidth, screenHeight, constraints)) {
                placements.add(new WidgetPlacement(entry.index(), new Rect(
                        rightX, rowYs.get(rightRow), entry.item().width(), entry.item().height()), LayoutRegion.AUXILIARY));
                rightRow++;
                continue;
            }
            unmanaged.add(entry.index());
        }
    }

    /**
     * 主菜单紧凑按钮优先占用最底部主体行的左右槽位；每一行最多左右各一个，
     * 该行占满后再逐行向上，避免图标在同一侧无限横向堆叠。
     */
    private static void placeTitleCompactButtons(
            List<TitleIconEntry> titleCompactButtons,
            CorePlacement corePlacement,
            LayoutConstraints c,
            int screenWidth,
            int screenHeight,
            List<WidgetPlacement> placements,
            Set<Integer> unmanaged) {
        if (titleCompactButtons.isEmpty()) {
            return;
        }

        List<TitleIconEntry> left = orderedTitleCompactButtons(
                titleCompactButtons,
                ButtonRole.TITLE_MOD_MENU_ICON,
                ButtonRole.TITLE_LANGUAGE);
        List<TitleIconEntry> right = orderedTitleCompactButtons(
                titleCompactButtons,
                ButtonRole.TITLE_FRIENDS,
                ButtonRole.TITLE_ACCESSIBILITY);
        List<TitleIconEntry> floating = orderedTitleCompactButtons(titleCompactButtons, ButtonRole.AUXILIARY_COMPACT);
        List<Integer> rowYs = coreRowYsBottomFirst(corePlacement.placements());

        int leftIndex = 0;
        int rightIndex = 0;
        int floatingIndex = 0;
        boolean floatingUsesLeft = true;
        for (int rowY : rowYs) {
            int leftX = corePlacement.area().x() - c.spacing();
            boolean leftUsed = false;
            if (leftIndex < left.size()) {
                TitleIconEntry entry = left.get(leftIndex);
                int x = leftX - entry.item().width();
                if (fitsTitleCompactButton(x, rowY, entry.item(), screenWidth, screenHeight, c)) {
                    placements.add(new WidgetPlacement(entry.index(),
                            new Rect(x, rowY, entry.item().width(), entry.item().height()), LayoutRegion.AUXILIARY));
                    leftX = x - c.spacing();
                    leftIndex++;
                    leftUsed = true;
                }
            }

            int rightX = corePlacement.area().right() + c.spacing();
            boolean rightUsed = false;
            if (rightIndex < right.size()) {
                TitleIconEntry entry = right.get(rightIndex);
                if (fitsTitleCompactButton(rightX, rowY, entry.item(), screenWidth, screenHeight, c)) {
                    placements.add(new WidgetPlacement(entry.index(),
                            new Rect(rightX, rowY, entry.item().width(), entry.item().height()), LayoutRegion.AUXILIARY));
                    rightIndex++;
                    rightUsed = true;
                }
            }

            while (floatingIndex < floating.size() && (!leftUsed || !rightUsed)) {
                TitleIconEntry entry = floating.get(floatingIndex);
                int leftCandidateX = leftX - entry.item().width();
                boolean leftFits = !leftUsed
                        && fitsTitleCompactButton(leftCandidateX, rowY, entry.item(), screenWidth, screenHeight, c);
                boolean rightFits = !rightUsed
                        && fitsTitleCompactButton(rightX, rowY, entry.item(), screenWidth, screenHeight, c);
                boolean useLeft = leftFits && (!rightFits || floatingUsesLeft);
                boolean useRight = rightFits && (!leftFits || !floatingUsesLeft);
                if (useLeft) {
                    placements.add(new WidgetPlacement(entry.index(),
                            new Rect(leftCandidateX, rowY, entry.item().width(), entry.item().height()), LayoutRegion.AUXILIARY));
                    leftUsed = true;
                    floatingUsesLeft = false;
                    floatingIndex++;
                } else if (useRight) {
                    placements.add(new WidgetPlacement(entry.index(),
                            new Rect(rightX, rowY, entry.item().width(), entry.item().height()), LayoutRegion.AUXILIARY));
                    rightUsed = true;
                    floatingUsesLeft = true;
                    floatingIndex++;
                } else {
                    break;
                }
            }
            if (leftIndex == left.size() && rightIndex == right.size() && floatingIndex == floating.size()) {
                return;
            }
        }

        while (leftIndex < left.size()) unmanaged.add(left.get(leftIndex++).index());
        while (rightIndex < right.size()) unmanaged.add(right.get(rightIndex++).index());
        while (floatingIndex < floating.size()) unmanaged.add(floating.get(floatingIndex++).index());
    }

    private static List<TitleIconEntry> orderedTitleCompactButtons(
            List<TitleIconEntry> source,
            ButtonRole... roles) {
        List<TitleIconEntry> ordered = new ArrayList<>();
        for (ButtonRole role : roles) {
            for (TitleIconEntry entry : source) {
                if (entry.role() == role) {
                    ordered.add(entry);
                }
            }
        }
        return ordered;
    }

    private static List<Integer> coreRowYsBottomFirst(List<WidgetPlacement> corePlacements) {
        return corePlacements.stream()
                .map(placement -> placement.rect().y())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private static List<Integer> coreRowYsTopFirst(List<WidgetPlacement> corePlacements) {
        return corePlacements.stream()
                .map(placement -> placement.rect().y())
                .distinct()
                .sorted()
                .toList();
    }

    private static boolean fitsTitleCompactButton(
            int x,
            int y,
            LayoutItem item,
            int screenWidth,
            int screenHeight,
            LayoutConstraints constraints) {
        return x >= constraints.safeMargin()
                && y >= constraints.safeMargin()
                && x + item.width() <= screenWidth - constraints.safeMargin()
                && y + item.height() <= screenHeight - constraints.footerReserve();
    }

    private static boolean isCoreFull(ButtonRole role) {
        return role == ButtonRole.CORE_FULL;
    }

    private static boolean isCoreHalf(ButtonRole role) {
        return role == ButtonRole.CORE_HALF
                || role == ButtonRole.TITLE_MOD_MENU_SHRINK_REALMS
                || role == ButtonRole.TITLE_MOD_MENU_SHRINK_BUTTON;
    }

    /** 计算扩展区：核心下方居中，宽度向不与其交叠的最左非核心放置收缩。 */
    private static Rect extensionArea(
            LayoutConstraints c,
            int screenWidth,
            int screenHeight,
            Rect coreArea,
            List<WidgetPlacement> placements) {
        int top = coreArea.bottom() + c.spacing() * 2;
        // TitleScreen 的紧凑按钮区位于主体正下方。只有该区与主体横向重叠时，
        // 扩展文本按钮才需要整体下移；PauseScreen 右侧辅助列不会占用扩展区高度。
        for (WidgetPlacement p : placements) {
            if (p.region() != LayoutRegion.CORE
                    && p.rect().x() < coreArea.right()
                    && coreArea.x() < p.rect().right()) {
                top = Math.max(top, p.rect().bottom() + c.spacing() * 2);
            }
        }
        int bottom = screenHeight - c.footerReserve();
        if (top >= bottom) {
            return null;
        }
        int rightLimit = screenWidth - c.safeMargin();
        for (WidgetPlacement p : placements) {
            if (p.region() != LayoutRegion.CORE && p.rect().bottom() > top && p.rect().y() < bottom) {
                rightLimit = Math.min(rightLimit, p.rect().x() - c.spacing());
            }
        }
        int width = Math.min(c.coreWidth(), rightLimit - coreArea.x());
        if (width < c.minItemWidth()) {
            return null;
        }
        return new Rect(coreArea.x(), top, width, bottom - top);
    }

    /** 收窄降级：非图标文本按钮宽度超出可用区时压到可用宽度。 */
    private static List<LayoutItem> shrinkText(List<Entry> entries, int maxWidth, int minWidth) {
        if (maxWidth < minWidth) {
            return List.copyOf(entries);
        }
        List<LayoutItem> out = new ArrayList<>(entries.size());
        for (Entry e : entries) {
            out.add(e.item().iconLike() || e.item().width() <= maxWidth ? e.item() : new ShrunkItem(e.item(), maxWidth));
        }
        return out;
    }

    /** 按处理顺序丢弃与非核心区域交叠的放置；被丢弃项保留原位并计入 unmanaged。 */
    private static void dropOverlaps(List<WidgetPlacement> placements, Set<Integer> unmanaged) {
        List<WidgetPlacement> accepted = new ArrayList<>(placements.size());
        placements.removeIf(placement -> {
            for (WidgetPlacement other : accepted) {
                if (placement.rect().intersects(other.rect())) {
                    unmanaged.add(placement.index());
                    return true;
                }
            }
            accepted.add(placement);
            return false;
        });
    }

    private record Entry(int index, LayoutItem item) implements LayoutItem {
        @Override
        public String translationKey() {
            return item.translationKey();
        }

        @Override
        public boolean iconLike() {
            return item.iconLike();
        }

        @Override
        public int width() {
            return item.width();
        }

        @Override
        public int height() {
            return item.height();
        }
    }

    private record CoreEntry(int index, LayoutItem item, ButtonRole role, int order) {
    }

    private record TitleIconEntry(int index, LayoutItem item, ButtonRole role) {
    }

    private record ShrunkItem(LayoutItem delegate, int maxWidth) implements LayoutItem {
        @Override
        public String translationKey() {
            return delegate.translationKey();
        }

        @Override
        public boolean iconLike() {
            return delegate.iconLike();
        }

        @Override
        public int width() {
            return Math.min(delegate.width(), maxWidth);
        }

        @Override
        public int height() {
            return delegate.height();
        }
    }

    private record CorePlacement(List<WidgetPlacement> placements, Rect area, int spacing) {
    }
}

package com.davidblackcn.buildupmenubutton.client.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * 行优先、随高度自动换列的紧凑网格布局器。
 *
 * <p>规则：先由可用高度计算每列最多容纳的行数（取最大项高做上界），再计算容纳全部项目所需的列数
 * （最小列数优先，最多不超过 {@code maxColumns}），随后行优先填充、逐列校验总宽度。
 * 奇数项最后一行以组为单位在网格占用宽度内居中。支持整块水平/垂直对齐。</p>
 *
 * <p>纯 Java 实现，不依赖 Minecraft，可独立测试。返回的 {@link Rect} 列表与输入顺序一致；
 * 无法在可用矩形内放下全部项目时返回 {@code null}。</p>
 */
public final class WrapGridLayout {

    public enum HAlign { LEFT, CENTER, RIGHT }

    public enum VAlign { TOP, CENTER, BOTTOM }

    private WrapGridLayout() {
    }

    /**
     * 以左上对齐放置项目。
     *
     * @return 与输入同序的矩形列表；放不下时返回 {@code null}
     */
    public static List<Rect> place(List<? extends LayoutItem> items, Rect area, int spacing, int maxColumns) {
        return place(items, area, spacing, maxColumns, HAlign.LEFT, VAlign.TOP);
    }

    /**
     * 以指定对齐方式放置项目。
     *
     * @return 与输入同序的矩形列表；放不下时返回 {@code null}
     */
    public static List<Rect> place(
            List<? extends LayoutItem> items,
            Rect area,
            int spacing,
            int maxColumns,
            HAlign hAlign,
            VAlign vAlign) {
        int n = items.size();
        if (n == 0) {
            return List.of();
        }
        if (area.width() <= 0 || area.height() <= 0 || spacing < 0 || maxColumns < 1) {
            return null;
        }
        int maxItemHeight = 0;
        for (LayoutItem item : items) {
            maxItemHeight = Math.max(maxItemHeight, item.height());
        }
        int maxRows = Math.max(1, (area.height() + spacing) / (maxItemHeight + spacing));
        int minColumns = Math.max(1, Math.min(maxColumns, (int) Math.ceil((double) n / maxRows)));

        for (int columns = minColumns; columns <= maxColumns; columns++) {
            List<Rect> result = tryPlace(items, area, spacing, columns, hAlign, vAlign);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static List<Rect> tryPlace(
            List<? extends LayoutItem> items,
            Rect area,
            int spacing,
            int columns,
            HAlign hAlign,
            VAlign vAlign) {
        int n = items.size();
        int rows = (n + columns - 1) / columns;
        int[] rowHeights = new int[rows];
        int[] columnWidths = new int[columns];
        for (int k = 0; k < n; k++) {
            int row = k / columns;
            int col = k % columns;
            rowHeights[row] = Math.max(rowHeights[row], items.get(k).height());
            columnWidths[col] = Math.max(columnWidths[col], items.get(k).width());
        }

        int totalWidth = 0;
        for (int width : columnWidths) {
            totalWidth += width;
        }
        totalWidth += (columns - 1) * spacing;
        int totalHeight = 0;
        for (int height : rowHeights) {
            totalHeight += height;
        }
        totalHeight += (rows - 1) * spacing;
        if (totalWidth > area.width() || totalHeight > area.height()) {
            return null;
        }

        int blockShiftX = switch (hAlign) {
            case LEFT -> 0;
            case CENTER -> (area.width() - totalWidth) / 2;
            case RIGHT -> area.width() - totalWidth;
        };
        int blockShiftY = switch (vAlign) {
            case TOP -> 0;
            case CENTER -> (area.height() - totalHeight) / 2;
            case BOTTOM -> area.height() - totalHeight;
        };
        int originX = area.x() + blockShiftX;
        int originY = area.y() + blockShiftY;

        int[] columnX = new int[columns];
        int cursorX = 0;
        for (int col = 0; col < columns; col++) {
            columnX[col] = cursorX;
            cursorX += columnWidths[col] + spacing;
        }
        int[] rowY = new int[rows];
        int cursorY = 0;
        for (int row = 0; row < rows; row++) {
            rowY[row] = cursorY;
            cursorY += rowHeights[row] + spacing;
        }

        List<Rect> out = new ArrayList<>(n);
        int lastRowStart = rows > 0 ? (rows - 1) * columns : 0;
        int lastRowCount = n - lastRowStart;
        int lastRowGroupWidth = 0;
        for (int k = lastRowStart; k < n; k++) {
            lastRowGroupWidth += items.get(k).width();
        }
        lastRowGroupWidth += Math.max(0, lastRowCount - 1) * spacing;
        int lastRowShift = (totalWidth - lastRowGroupWidth) / 2;

        for (int k = 0; k < n; k++) {
            int row = k / columns;
            int col = k % columns;
            int w = items.get(k).width();
            int h = items.get(k).height();
            int x;
            if (row == rows - 1 && lastRowCount < columns) {
                int running = 0;
                for (int m = lastRowStart; m < k; m++) {
                    running += items.get(m).width() + spacing;
                }
                x = originX + lastRowShift + running;
            } else {
                x = originX + columnX[col] + (columnWidths[col] - w) / 2;
            }
            int y = originY + rowY[row] + (rowHeights[row] - h) / 2;
            out.add(new Rect(x, y, w, h));
        }
        return out;
    }
}

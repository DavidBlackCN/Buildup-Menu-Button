package com.davidblackcn.buildupmenubutton.client.layout;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** {@link WrapGridLayout} 的确定性坐标测试。 */
public final class WrapGridLayoutTest {

    private WrapGridLayoutTest() {
    }

    public static void runAll() {
        emptyItems();
        singleItem();
        twoColumnsRowMajor();
        threeColumnsWhenMany();
        oddLastRowCentered();
        rightBottomAlign();
        centerAlign();
        columnWrapOverflowReturnsNull();
        widthOverflowReturnsNull();
        mixedSizesPerColumnWidth();
        tallerGridThreeColumnsFit();
    }

    private static LayoutTestSuite.TestItem item(int width, int height) {
        return new LayoutTestSuite.TestItem("item", false, width, height);
    }

    private static List<LayoutTestSuite.TestItem> items(int count, int width, int height) {
        return IntStream.range(0, count)
                .mapToObj(i -> item(width, height))
                .collect(Collectors.toList());
    }

    private static void assertRect(Rect expected, Rect actual, String message) {
        LayoutTestSuite.assertEquals(expected.x(), actual.x(), message + " x");
        LayoutTestSuite.assertEquals(expected.y(), actual.y(), message + " y");
        LayoutTestSuite.assertEquals(expected.width(), actual.width(), message + " width");
        LayoutTestSuite.assertEquals(expected.height(), actual.height(), message + " height");
    }

    private static void emptyItems() {
        List<Rect> result = WrapGridLayout.place(List.of(), new Rect(0, 0, 100, 100), 4, 4);
        LayoutTestSuite.assertTrue(result.isEmpty(), "empty input must return empty list");
    }

    private static void singleItem() {
        List<Rect> result = WrapGridLayout.place(
                List.of(item(20, 20)), new Rect(0, 0, 100, 100), 4, 4);
        LayoutTestSuite.assertEquals(1, result.size(), "single item size");
        assertRect(new Rect(0, 0, 20, 20), result.get(0), "single item");
    }

    private static void twoColumnsRowMajor() {
        // 高 44 只够 2 行 -> 4 项应排成 2 列：A B / C D
        List<Rect> result = WrapGridLayout.place(
                items(4, 20, 20), new Rect(0, 0, 100, 44), 4, 4);
        LayoutTestSuite.assertEquals(4, result.size(), "four items size");
        assertRect(new Rect(0, 0, 20, 20), result.get(0), "A");
        assertRect(new Rect(24, 0, 20, 20), result.get(1), "B");
        assertRect(new Rect(0, 24, 20, 20), result.get(2), "C");
        assertRect(new Rect(24, 24, 20, 20), result.get(3), "D");
    }

    private static void threeColumnsWhenMany() {
        // 高 44 只够 2 行 -> 6 项应排成 3 列：A B C / D E F
        List<Rect> result = WrapGridLayout.place(
                items(6, 20, 20), new Rect(0, 0, 100, 44), 4, 4);
        LayoutTestSuite.assertEquals(6, result.size(), "six items size");
        assertRect(new Rect(0, 0, 20, 20), result.get(0), "A");
        assertRect(new Rect(24, 0, 20, 20), result.get(1), "B");
        assertRect(new Rect(48, 0, 20, 20), result.get(2), "C");
        assertRect(new Rect(0, 24, 20, 20), result.get(3), "D");
        assertRect(new Rect(24, 24, 20, 20), result.get(4), "E");
        assertRect(new Rect(48, 24, 20, 20), result.get(5), "F");
    }

    private static void oddLastRowCentered() {
        // 5 项 3 列：A B C / D E，最后一行 E 前只剩 D 与 E，需整体居中
        List<Rect> result = WrapGridLayout.place(
                items(5, 20, 20), new Rect(0, 0, 100, 44), 4, 4);
        LayoutTestSuite.assertEquals(5, result.size(), "five items size");
        assertRect(new Rect(0, 0, 20, 20), result.get(0), "A");
        assertRect(new Rect(24, 0, 20, 20), result.get(1), "B");
        assertRect(new Rect(48, 0, 20, 20), result.get(2), "C");
        assertRect(new Rect(12, 24, 20, 20), result.get(3), "D centered");
        assertRect(new Rect(36, 24, 20, 20), result.get(4), "E centered");
    }

    private static void rightBottomAlign() {
        // 整块右下对齐：2 项单列，应贴到区域右/下边缘
        List<Rect> result = WrapGridLayout.place(
                items(2, 20, 20), new Rect(0, 0, 100, 44), 4, 4,
                WrapGridLayout.HAlign.RIGHT, WrapGridLayout.VAlign.BOTTOM);
        assertRect(new Rect(80, 0, 20, 20), result.get(0), "right-bottom first");
        assertRect(new Rect(80, 24, 20, 20), result.get(1), "right-bottom second");
    }

    private static void centerAlign() {
        List<Rect> result = WrapGridLayout.place(
                List.of(item(20, 20)), new Rect(0, 0, 100, 44), 4, 4,
                WrapGridLayout.HAlign.CENTER, WrapGridLayout.VAlign.CENTER);
        assertRect(new Rect(40, 12, 20, 20), result.get(0), "center single");
    }

    private static void columnWrapOverflowReturnsNull() {
        // 10 项 20x20，高 44 只够 2 行，最多 3 列仍放不下（需 5 列）-> null
        List<Rect> result = WrapGridLayout.place(
                items(10, 20, 20), new Rect(0, 0, 100, 44), 4, 3);
        LayoutTestSuite.assertTrue(result == null, "overflow must return null");
    }

    private static void widthOverflowReturnsNull() {
        // 6 项 50x30，区域宽 40，单列即超宽（50 > 40）-> null
        List<Rect> result = WrapGridLayout.place(
                items(6, 50, 30), new Rect(0, 0, 40, 200), 4, 4);
        LayoutTestSuite.assertTrue(result == null, "width overflow must return null");
    }

    private static void mixedSizesPerColumnWidth() {
        // 每列取该列最大宽，每行取该行最大高；不同高度垂直居中
        List<Rect> result = WrapGridLayout.place(
                List.of(item(30, 20), item(20, 20), item(20, 30)),
                new Rect(0, 0, 100, 60), 4, 4);
        LayoutTestSuite.assertEquals(3, result.size(), "mixed size count");
        assertRect(new Rect(0, 5, 30, 20), result.get(0), "tall-width item centered vertically");
        assertRect(new Rect(34, 5, 20, 20), result.get(1), "short item centered vertically");
        assertRect(new Rect(58, 0, 20, 30), result.get(2), "tall item at row top");
    }

    private static void tallerGridThreeColumnsFit() {
        // 高 100 可容纳 4 行 -> 10 项排 3 列：A B C / D E F / G H I / J（J 居中）
        List<Rect> result = WrapGridLayout.place(
                items(10, 20, 20), new Rect(0, 0, 100, 100), 4, 4);
        LayoutTestSuite.assertEquals(10, result.size(), "ten items size");
        assertRect(new Rect(0, 0, 20, 20), result.get(0), "A");
        assertRect(new Rect(48, 0, 20, 20), result.get(2), "C");
        assertRect(new Rect(48, 48, 20, 20), result.get(8), "I");
        assertRect(new Rect(24, 72, 20, 20), result.get(9), "J centered");
    }
}

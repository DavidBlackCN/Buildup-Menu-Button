package com.davidblackcn.buildupmenubutton.client.layout;

import com.davidblackcn.buildupmenubutton.client.RealmsNotificationAnchorTest;

/**
 * 纯 Java 布局测试的入口与共享断言/测试项工具。
 *
 * <p>不依赖 Minecraft、JUnit 或其他外部依赖，由 Gradle {@code layoutTest}（JavaExec）
 * 直接运行；任何断言失败会抛出 {@link AssertionError} 并使任务失败。</p>
 */
public final class LayoutTestSuite {

    public static void main(String[] args) {
        runAll();
        System.out.println("All layout tests passed.");
    }

    public static void runAll() {
        WrapGridLayoutTest.runAll();
        DynamicButtonLayoutManagerTest.runAll();
        LayoutInvariantTest.runAll();
        RealmsNotificationAnchorTest.runAll();
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    /** 测试用最小实现，可表达翻译键、图标形态与尺寸。 */
    public record TestItem(String key, boolean icon, int width, int height) implements LayoutItem {
        @Override
        public String translationKey() {
            return key;
        }

        @Override
        public boolean iconLike() {
            return icon;
        }

        @Override
        public int width() {
            return width;
        }

        @Override
        public int height() {
            return height;
        }
    }
}

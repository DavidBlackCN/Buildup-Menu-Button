package com.davidblackcn.buildupmenubutton.client.layout;

public record LayoutConstraints(
        int safeMargin,
        int spacing,
        int minSpacing,
        int coreWidth,
        int targetHeight,
        int maxColumns,
        int minItemWidth,
        int minScreenWidth,
        int minScreenHeight,
        int headerReserve,
        int footerReserve) {

    public static LayoutConstraints titleScreen(int screenWidth, int screenHeight) {
        int safe = Math.max(8, Math.min(16, screenWidth / 40));
        int coreWidth = Math.min(200, Math.max(120, screenWidth - safe * 2));
        // TitleScreen 26.2 的原生普通菜单从 height / 4 + 48 开始。以此为锚点，
        // 既为 Logo/Splash 留出空间，也避免最大化窗口时主体按钮贴到 Logo 下方。
        int header = screenHeight / 4 + 48;
        int footer = Math.max(28, screenHeight / 12);
        return new LayoutConstraints(safe, 4, 2, coreWidth, 20, 4, 80, 320, 180, header, footer);
    }

    public static LayoutConstraints pauseScreen(int screenWidth, int screenHeight) {
        int safe = Math.max(8, Math.min(16, screenWidth / 40));
        int coreWidth = Math.min(204, Math.max(120, screenWidth - safe * 2));
        // PauseScreen 的“回到游戏”按钮以 height / 4 + 2 为原版基准；增加的
        // Mod Menu 大按钮应从此基准向下扩展，而不是把整个菜单推向屏幕上方。
        int header = Math.max(36, screenHeight / 4 + 2);
        int footer = Math.max(28, screenHeight / 12);
        return new LayoutConstraints(safe, 4, 2, coreWidth, 20, 4, 80, 320, 180, header, footer);
    }
}

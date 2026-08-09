package com.davidblackcn.buildupmenubutton.client.layout;

import java.util.List;

public final class LayoutPlan {
    private final List<WidgetPlacement> placements;
    private final List<Integer> unmanagedIndices;
    private final int screenWidth;
    private final int screenHeight;
    private final String failureReason;
    private final boolean failedOpen;

    public LayoutPlan(
            List<WidgetPlacement> placements,
            List<Integer> unmanagedIndices,
            int screenWidth,
            int screenHeight,
            String failureReason,
            boolean failedOpen) {
        this.placements = List.copyOf(placements);
        this.unmanagedIndices = List.copyOf(unmanagedIndices);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.failureReason = failureReason;
        this.failedOpen = failedOpen;
    }

    public static LayoutPlan failedOpen(int screenWidth, int screenHeight, String reason) {
        return new LayoutPlan(List.of(), List.of(), screenWidth, screenHeight, reason, true);
    }

    public List<WidgetPlacement> placements() {
        return placements;
    }

    public List<Integer> unmanagedIndices() {
        return unmanagedIndices;
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public String failureReason() {
        return failureReason;
    }

    public boolean isFailedOpen() {
        return failedOpen;
    }

    public boolean isValid() {
        if (failedOpen) {
            return false;
        }
        for (int i = 0; i < placements.size(); i++) {
            Rect rect = placements.get(i).rect();
            if (rect.width() <= 0 || rect.height() <= 0) {
                return false;
            }
            if (rect.x() < 0 || rect.y() < 0 || rect.right() > screenWidth || rect.bottom() > screenHeight) {
                return false;
            }
            for (int j = i + 1; j < placements.size(); j++) {
                if (rect.intersects(placements.get(j).rect())) {
                    return false;
                }
            }
        }
        return true;
    }
}

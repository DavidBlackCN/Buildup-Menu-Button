package com.davidblackcn.buildupmenubutton.client.layout;

public record Rect(int x, int y, int width, int height) {
    public Rect {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Rect dimensions must not be negative");
        }
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public boolean intersects(Rect other) {
        return x < other.right() && other.x < right() && y < other.bottom() && other.y < bottom();
    }
}

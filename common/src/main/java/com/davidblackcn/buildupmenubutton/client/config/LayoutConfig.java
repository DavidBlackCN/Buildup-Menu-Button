package com.davidblackcn.buildupmenubutton.client.config;

/** Client-side switches for the two screen layout optimizations. */
public record LayoutConfig(boolean titleScreenLayoutEnabled, boolean pauseScreenLayoutEnabled) {

    public static final LayoutConfig DEFAULT = new LayoutConfig(true, true);
}

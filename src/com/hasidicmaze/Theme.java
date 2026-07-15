package com.hasidicmaze;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Visual identity for Hasidic Maze — ink navy, warm gold, soft parchment.
 * Window size is fixed so every card opens at the same preferred frame.
 */
public final class Theme {
    public static final Color INK = new Color(18, 24, 38);
    public static final Color INK_SOFT = new Color(28, 36, 54);
    public static final Color NAVY = new Color(32, 48, 78);
    public static final Color GOLD = new Color(212, 168, 75);
    public static final Color GOLD_BRIGHT = new Color(240, 198, 96);
    public static final Color CREAM = new Color(236, 228, 210);
    public static final Color MUTED = new Color(160, 168, 184);
    public static final Color ACCENT = new Color(196, 72, 58);
    public static final Color PANEL = new Color(24, 32, 48, 230);
    public static final Color BUTTON = new Color(40, 56, 86);
    public static final Color BUTTON_HOVER = new Color(56, 78, 118);
    public static final Color BUTTON_BORDER = new Color(212, 168, 75, 180);

    /** Fixed startup frame — matches the preferred in-game window size. */
    public static final int WINDOW_WIDTH = 733;
    public static final int WINDOW_HEIGHT = 713;
    public static final int TILE_SIZE = 42;

    public static Dimension windowSize() {
        return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private static Font displayFont;
    private static Font bodyFont;
    private static Font monoFont;

    private Theme() {}

    public static Font display(int size) {
        return displayFont().deriveFont(Font.BOLD, (float) size);
    }

    public static Font body(int size) {
        return bodyFont().deriveFont(Font.PLAIN, (float) size);
    }

    public static Font bodyBold(int size) {
        return bodyFont().deriveFont(Font.BOLD, (float) size);
    }

    public static Font mono(int size) {
        return monoFont().deriveFont(Font.BOLD, (float) size);
    }

    private static Font displayFont() {
        if (displayFont == null) {
            displayFont = pick("Segoe UI Semibold", "Segoe UI", "Tahoma", "SansSerif");
        }
        return displayFont;
    }

    private static Font bodyFont() {
        if (bodyFont == null) {
            bodyFont = pick("Segoe UI", "Tahoma", "SansSerif");
        }
        return bodyFont;
    }

    private static Font monoFont() {
        if (monoFont == null) {
            monoFont = pick("Consolas", "Cascadia Mono", "Monospaced");
        }
        return monoFont;
    }

    private static Font pick(String... names) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String name : names) {
            if (Arrays.asList(available).contains(name)) {
                return new Font(name, Font.PLAIN, 14);
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    }
}

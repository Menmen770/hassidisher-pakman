package com.hasidicmaze;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Palette locked to menu BG art:
 * f78f39, daa264, 1e325d, 83563f, fbc55c, 526b90, 9a360f, b37d52, 4d251c
 */
public final class Theme {
    public static final Color BG_ORANGE = new Color(0xf7, 0x8f, 0x39);
    public static final Color BG_TAN = new Color(0xda, 0xa2, 0x64);
    public static final Color BG_NAVY = new Color(0x1e, 0x32, 0x5d);
    public static final Color BG_WOOD = new Color(0x83, 0x56, 0x3f);
    public static final Color BG_GOLD = new Color(0xfb, 0xc5, 0x5c);
    public static final Color BG_STEEL = new Color(0x52, 0x6b, 0x90);
    public static final Color BG_RUST = new Color(0x9a, 0x36, 0x0f);
    public static final Color BG_STONE = new Color(0xb3, 0x7d, 0x52);
    public static final Color BG_BARK = new Color(0x4d, 0x25, 0x1c);

    public static final Color PAC_YELLOW = BG_GOLD;
    public static final Color PAC_YELLOW_SOFT = new Color(0xfc, 0xd4, 0x7e);
    public static final Color PAC_ORANGE = BG_ORANGE;

    public static final Color LOGO_BLUE = BG_STEEL;
    public static final Color LOGO_BLUE_SOFT = new Color(0x6e, 0x86, 0xa8);
    public static final Color LOGO_BLUE_DEEP = BG_NAVY;

    public static final Color MAZE_BLUE = BG_STEEL;
    public static final Color MAZE_BLUE_SOFT = LOGO_BLUE_SOFT;
    public static final Color GHOST_RED = BG_RUST;
    public static final Color GHOST_PINK = new Color(220, 150, 170);
    public static final Color GHOST_CYAN = BG_STEEL;

    public static final Color INK = BG_NAVY;
    public static final Color INK_SOFT = new Color(0x28, 0x3e, 0x6a);
    public static final Color INK_LIFT = BG_STEEL;
    public static final Color STONE = BG_STONE;
    public static final Color LIMESTONE = BG_TAN;
    public static final Color MORTAR = BG_WOOD;

    public static final Color WOOD_DEEP = BG_BARK;
    public static final Color WOOD = BG_WOOD;
    public static final Color WOOD_MID = BG_STONE;
    public static final Color WOOD_LIGHT = BG_TAN;
    public static final Color WOOD_EDGE = BG_STONE;

    public static final Color NAVY = INK_SOFT;
    public static final Color NAVY_DEEP = INK;
    public static final Color TITLE_BAR = new Color(0x16, 0x26, 0x48);

    public static final Color GOLD = PAC_YELLOW;
    public static final Color GOLD_BRIGHT = PAC_YELLOW_SOFT;
    public static final Color GOLD_DIM = BG_ORANGE;
    public static final Color BRASS = BG_ORANGE;

    public static final Color CREAM = new Color(0xfc, 0xf0, 0xd8);
    public static final Color CREAM_SOFT = BG_TAN;
    public static final Color PARCHMENT = BG_GOLD;
    public static final Color TEXT = CREAM;
    public static final Color TEXT_SOFT = CREAM_SOFT;
    public static final Color MUTED = BG_STEEL;
    public static final Color MUTED_DARK = new Color(0x3a, 0x4e, 0x6e);
    public static final Color HAIRLINE = BG_TAN;

    public static final Color ACCENT = BG_RUST;
    public static final Color SUCCESS = new Color(88, 148, 96);
    public static final Color WARNING = BG_ORANGE;
    public static final Color BRICK = BG_RUST;

    public static final Color PANEL = new Color(0x1e, 0x32, 0x5d, 240);
    public static final Color PANEL_SOLID = INK_SOFT;
    public static final Color BUTTON = INK_SOFT;
    public static final Color BUTTON_HOVER = BG_STEEL;
    public static final Color BUTTON_BORDER = BG_GOLD;
    public static final Color PRIMARY_FILL = BG_GOLD;
    public static final Color PRIMARY_HOVER = PAC_YELLOW_SOFT;
    public static final Color PRIMARY_TEXT = BG_BARK;
    public static final Color LIGHT_ON_DARK = CREAM;
    public static final Color PELLET = BG_STEEL;

    public static final int WINDOW_WIDTH = 733;
    public static final int WINDOW_HEIGHT = 713;

    public static Dimension windowSize() {
        return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private static Font displayFont;
    private static Font bodyFont;
    private static Font monoFont;

    private Theme() {}

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

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

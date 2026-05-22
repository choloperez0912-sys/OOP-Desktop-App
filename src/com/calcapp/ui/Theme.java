package com.calcapp.ui;

import java.awt.Color;
import java.awt.Font;

/** Centralized design tokens. */
public final class Theme {

    // ── Colors ────────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(18,  18,  24);
    public static final Color BG_CARD       = new Color(28,  28,  38);
    public static final Color BG_INPUT      = new Color(38,  38,  52);
    public static final Color ACCENT        = new Color(99,  102, 241); // indigo
    public static final Color ACCENT_HOVER  = new Color(79,  82,  220);
    public static final Color TEXT_PRIMARY  = new Color(240, 240, 255);
    public static final Color TEXT_MUTED    = new Color(140, 140, 165);
    public static final Color BTN_NUM       = new Color(45,  45,  62);
    public static final Color BTN_OP        = new Color(60,  60,  85);
    public static final Color BTN_EQUALS    = new Color(99,  102, 241);
    public static final Color BTN_CLEAR     = new Color(220, 60,  80);
    public static final Color SUCCESS       = new Color(52,  211, 153);
    public static final Color ERROR         = new Color(248, 113, 113);
    public static final Color BORDER        = new Color(55,  55,  75);

    // ── Fonts ─────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,   22);
    public static final Font FONT_DISPLAY = new Font("Segoe UI", Font.PLAIN,  32);
    public static final Font FONT_EXPR    = new Font("Segoe UI", Font.PLAIN,  14);
    public static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD,   18);
    public static final Font FONT_BTN_SM  = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_FIELD   = new Font("Segoe UI", Font.PLAIN,  14);

    private Theme() {}
}

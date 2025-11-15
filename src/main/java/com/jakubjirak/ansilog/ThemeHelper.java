package com.jakubjirak.ansilog;

import java.awt.Color;

public class ThemeHelper {
    
    public static Color getInvisibleColor() {
        boolean isDark = AnsiLogSettingsState.getInstance().isDarkTheme();
        return isDark ? new Color(30, 30, 30) : new Color(255, 255, 255);
    }
    
    public static Color getHintColor() {
        boolean isDark = AnsiLogSettingsState.getInstance().isDarkTheme();
        return isDark ? new Color(100, 100, 100) : new Color(150, 150, 150);
    }
    
    public static Color getHighlightColor(int ansiCode) {
        return switch(ansiCode) {
            case 31, 1 -> new Color(255, 0, 0);
            case 32 -> new Color(0, 255, 0);
            case 33 -> new Color(255, 255, 0);
            case 34 -> new Color(0, 0, 255);
            case 35 -> new Color(255, 0, 255);
            case 36 -> new Color(0, 255, 255);
            case 37 -> new Color(255, 255, 255);
            case 90 -> new Color(128, 128, 128);
            case 91 -> new Color(255, 128, 128);
            case 92 -> new Color(128, 255, 128);
            case 93 -> new Color(255, 255, 128);
            case 94 -> new Color(128, 128, 255);
            case 95 -> new Color(255, 128, 255);
            case 96 -> new Color(128, 255, 255);
            default -> new Color(192, 192, 192);
        };
    }
}

package com.jakubjirak.ansilog;

import java.util.regex.Pattern;

public class AnsiPatternUtil {
    // Matches both actual escape bytes (\u001B or \x1B) and literal \u001B strings in text
    public static final Pattern ANSI_PATTERN = Pattern.compile("(?:\u001B|\\\\u001B)\\[[0-9;]*m");

    private AnsiPatternUtil() {}

    public static Pattern getAnsiPattern() {
        return ANSI_PATTERN;
    }
}

package com.jakubjirak.ansilog;

import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AnsiCodeGutterRenderer extends GutterIconRenderer {
    private final String colorName;
    
    public AnsiCodeGutterRenderer(String colorName) {
        this.colorName = colorName;
    }

    @Override
    public @Nullable Icon getIcon() {
        return createColorIcon();
    }

    private Icon createColorIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Color color = parseAnsiColor(colorName);
                g.setColor(color);
                g.fillRect(x + 2, y + 3, 10, 10);
                g.setColor(Color.BLACK);
                g.drawRect(x + 2, y + 3, 10, 10);
            }

            @Override
            public int getIconWidth() {
                return 14;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    private Color parseAnsiColor(String colorName) {
        return switch(colorName) {
            case "red" -> new Color(255, 0, 0);
            case "green" -> new Color(0, 255, 0);
            case "yellow" -> new Color(255, 255, 0);
            case "blue" -> new Color(0, 0, 255);
            case "magenta" -> new Color(255, 0, 255);
            case "cyan" -> new Color(0, 255, 255);
            default -> Color.GRAY;
        };
    }

    @Override
    public @Nullable String getTooltipText() {
        return "ANSI: " + colorName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AnsiCodeGutterRenderer)) return false;
        return colorName.equals(((AnsiCodeGutterRenderer) obj).colorName);
    }

    @Override
    public int hashCode() {
        return colorName.hashCode();
    }
}

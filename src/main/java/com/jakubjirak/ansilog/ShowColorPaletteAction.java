package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ShowColorPaletteAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StringBuilder palette = new StringBuilder("ANSI Color Codes:\n\n");
        palette.append("Standard Colors (30-37):\n");
        palette.append("  30=Black, 31=Red, 32=Green, 33=Yellow\n");
        palette.append("  34=Blue, 35=Magenta, 36=Cyan, 37=White\n\n");
        
        palette.append("Bright Colors (90-97):\n");
        palette.append("  90=Bright Black, 91=Bright Red, 92=Bright Green\n");
        palette.append("  93=Bright Yellow, 94=Bright Blue, 95=Bright Magenta\n");
        palette.append("  96=Bright Cyan, 97=Bright White\n\n");
        
        palette.append("Background (40-47, 100-107):\n");
        palette.append("  Add 10 to foreground codes (e.g., 41=Red BG)\n\n");
        
        palette.append("Text Styles:\n");
        palette.append("  0=Reset, 1=Bold, 3=Italic, 4=Underline\n\n");
        
        palette.append("256-Color: [38;5;Nm (N=0-255)\n");
        palette.append("Truecolor: [38;2;R;G;Bm\n");
        
        Messages.showInfoMessage(e.getProject(), palette.toString(), "ANSI Color Palette");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(true);
    }
}

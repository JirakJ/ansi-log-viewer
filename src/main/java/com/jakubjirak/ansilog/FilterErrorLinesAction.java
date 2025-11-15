package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class FilterErrorLinesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        StringBuilder errorLines = new StringBuilder();
        for (String line : lines) {
            // Lines with error color code (31 = red foreground)
            if ((line.contains("\u001B[") || line.contains("\\u001B[")) && (line.contains("31m") || line.contains("1;31m"))) {
                errorLines.append(line).append("\n");
            }
        }
        
        if (errorLines.length() == 0) {
            Messages.showInfoMessage(e.getProject(), "No error lines found.", "ANSI Log Viewer");
        } else {
            Messages.showInfoMessage(e.getProject(), "Error lines:\n\n" + errorLines.toString(), "ANSI Log Viewer");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
    }
}

package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class LogLevelStatisticsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        int errorCount = 0;
        int warnCount = 0;
        int infoCount = 0;
        int debugCount = 0;
        int traceCount = 0;
        
        for (String line : lines) {
            String upperLine = line.toUpperCase();
            if (upperLine.contains("ERROR")) errorCount++;
            else if (upperLine.contains("WARN")) warnCount++;
            else if (upperLine.contains("INFO")) infoCount++;
            else if (upperLine.contains("DEBUG")) debugCount++;
            else if (upperLine.contains("TRACE")) traceCount++;
        }
        
        String stats = String.format(
                "Log Level Statistics:\n\n" +
                "ERROR: %d (%.1f%%)\n" +
                "WARN:  %d (%.1f%%)\n" +
                "INFO:  %d (%.1f%%)\n" +
                "DEBUG: %d (%.1f%%)\n" +
                "TRACE: %d (%.1f%%)\n" +
                "\nTotal lines: %d",
                errorCount, (errorCount * 100.0) / lines.length,
                warnCount, (warnCount * 100.0) / lines.length,
                infoCount, (infoCount * 100.0) / lines.length,
                debugCount, (debugCount * 100.0) / lines.length,
                traceCount, (traceCount * 100.0) / lines.length,
                lines.length
        );
        
        Messages.showInfoMessage(e.getProject(), stats, "Log Level Statistics");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;
import java.util.regex.Matcher;

public class AnsiCodeStatisticsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        Matcher matcher = AnsiPatternUtil.ANSI_PATTERN.matcher(text);
        
        int totalCodes = 0;
        int totalLines = editor.getDocument().getLineCount();
        int linesWithCodes = 0;
        
        while (matcher.find()) {
            totalCodes++;
        }
        
        if (totalCodes > 0) {
            for (int i = 0; i < totalLines; i++) {
                int lineStart = editor.getDocument().getLineStartOffset(i);
                int lineEnd = editor.getDocument().getLineEndOffset(i);
                String lineText = editor.getDocument().getText(new com.intellij.openapi.util.TextRange(lineStart, lineEnd));
                if (lineText.contains("\u001B") || lineText.contains("\\u001B")) {
                    linesWithCodes++;
                }
            }
        }
        
        String message = String.format("ANSI Code Statistics:\n\n" +
                "Total ANSI codes: %d\n" +
                "Lines with codes: %d / %d\n" +
                "File size: %.1f KB\n" +
                "Codes per line: %.1f",
                totalCodes, linesWithCodes, totalLines,
                text.length() / 1024.0,
                totalCodes > 0 ? (double) totalCodes / linesWithCodes : 0);
        
        Messages.showInfoMessage(e.getProject(), message, "ANSI Log Viewer Statistics");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
    }
}

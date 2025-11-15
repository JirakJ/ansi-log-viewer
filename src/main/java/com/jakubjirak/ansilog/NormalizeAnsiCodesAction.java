package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NormalizeAnsiCodesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        
        String normalized = text.replaceAll("(?:\\u001B|\\\\u001B)\\[38;5;\\d+m", "\u001B[0m");
        
        int originalSize = text.length();
        int normalizedSize = normalized.length();
        int reduced = originalSize - normalizedSize;
        
        String message = String.format(
                "Normalized ANSI codes:\n\n" +
                "Size reduction: %d bytes (%.1f%%)\n" +
                "Original: %d bytes\n" +
                "Normalized: %d bytes",
                reduced, (reduced * 100.0 / originalSize), originalSize, normalizedSize);
        
        int result = Messages.showYesNoDialog(
                e.getProject(),
                message + "\n\nApply changes?",
                "Normalize ANSI Codes",
                "Apply",
                "Cancel",
                null
        );
        
        if (result == Messages.YES) {
            WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
                editor.getDocument().setText(normalized);
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
    }
}

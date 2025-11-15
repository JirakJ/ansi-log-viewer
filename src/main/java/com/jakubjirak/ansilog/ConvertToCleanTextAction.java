package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class ConvertToCleanTextAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String cleanText = text.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
        
        if (text.equals(cleanText)) {
            Messages.showInfoMessage(e.getProject(), "No ANSI codes found.", "ANSI Log Viewer");
            return;
        }
        
        int originalSize = text.length();
        int cleanSize = cleanText.length();
        int removed = originalSize - cleanSize;
        
        String message = String.format("Removed %d characters (%d%% reduction)\n\nPreview (first 200 chars):\n%s",
                removed, (removed * 100) / originalSize, 
                cleanText.length() > 200 ? cleanText.substring(0, 200) + "..." : cleanText);
        
        Messages.showInfoMessage(e.getProject(), message, "Clean Text Preview");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
    }
}

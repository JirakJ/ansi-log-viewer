package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.jetbrains.annotations.NotNull;

public class StripAnsiCodesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String cleanText = text.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
        
        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
            editor.getDocument().setText(cleanText);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
    }
}

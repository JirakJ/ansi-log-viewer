package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ToggleAnsiVisibilityAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(com.intellij.openapi.actionSystem.PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        AnsiLogSettingsState settings = AnsiLogSettingsState.getInstance();
        settings.setHideAnsiCodes(!settings.isHideAnsiCodes());
        
        AnsiLogFileOpenListener listener = new AnsiLogFileOpenListener();
        listener.applyAnsiHighlighting(editor);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(com.intellij.openapi.actionSystem.PlatformDataKeys.EDITOR);
        boolean enabled = editor != null;
        e.getPresentation().setEnabled(enabled);
        
        String text = AnsiLogSettingsState.getInstance().isHideAnsiCodes() ? "Show ANSI Codes" : "Hide ANSI Codes";
        e.getPresentation().setText(text);
    }
}

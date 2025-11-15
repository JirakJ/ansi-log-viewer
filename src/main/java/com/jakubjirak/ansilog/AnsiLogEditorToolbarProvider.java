package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

/**
 * Provides toolbar with ANSI visibility toggle for log files.
 * Uses the new EditorNotificationProvider API.
 */
public class AnsiLogEditorToolbarProvider implements EditorNotificationProvider {
    private static final Key<JComponent> KEY = Key.create("AnsiLogToolbar");

    @Override
    public @NotNull Function<? super FileEditor, ? extends JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        String name = file.getName();
        List<String> exts = AnsiLogSettingsState.getInstance().getExtensions();
        boolean match = exts.stream().anyMatch(ext -> name.endsWith("." + ext));
        
        if (!match) {
            return fileEditor -> null;
        }
        
        return fileEditor -> {
            if (!(fileEditor instanceof TextEditor)) return null;
            
            Editor editor = ((TextEditor) fileEditor).getEditor();
            DefaultActionGroup group = new DefaultActionGroup();
            group.add(new ToggleAnsiVisibilityAction());
            
            ActionManager actionManager = ActionManager.getInstance();
            ActionToolbar toolbar = actionManager.createActionToolbar("AnsiLogToolbar", group, true);
            
            JPanel panel = new JPanel();
            panel.add(toolbar.getComponent());
            return panel;
        };
    }
}

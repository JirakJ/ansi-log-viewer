package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FilterByLogLevelAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String[] levels = {"ERROR", "WARN", "INFO", "DEBUG", "TRACE"};
        int choice = showChoiceDialog(e, "Select log level to display:", "Filter by Log Level", levels, 0);
        
        if (choice < 0) return;
        
        String selectedLevel = levels[choice];
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        StringBuilder filtered = new StringBuilder();
        int count = 0;
        
        for (String line : lines) {
            if (line.contains(selectedLevel) || line.contains(selectedLevel.toLowerCase())) {
                filtered.append(line).append("\n");
                count++;
            }
        }
        
        String result = String.format("Found %d lines with '%s' level\n\nPreview:\n%s",
                count,
                selectedLevel,
                filtered.length() > 500 ? filtered.substring(0, 500) + "..." : filtered.toString());
        
        Messages.showInfoMessage(e.getProject(), result, "Log Level Filter Result");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }

    private int showChoiceDialog(AnActionEvent e, String message, String title, String[] options, int defaultIndex) {
        JBList<String> list = new JBList<>(options);
        list.setSelectedIndex(defaultIndex);
        
        DialogWrapper dialog = new DialogWrapper(e.getProject(), false) {
            {
                init();
                setTitle(title);
            }
            
            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new JLabel(message));
                panel.add(Box.createVerticalStrut(10));
                panel.add(new JScrollPane(list));
                return panel;
            }
        };
        
        if (dialog.showAndGet()) {
            return list.getSelectedIndex();
        }
        return -1;
    }
}

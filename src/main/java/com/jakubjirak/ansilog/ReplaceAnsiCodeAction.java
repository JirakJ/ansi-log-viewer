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

public class ReplaceAnsiCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String[] options = {
            "Replace [31m (red) → [32m (green)",
            "Replace [33m (yellow) → [36m (cyan)",
            "Replace bold codes with normal",
            "Remove background colors",
            "Custom replacement"
        };
        
        int choice = showChoiceDialog(e, "Select replacement pattern:", "Replace ANSI Codes", options, 0);
        
        if (choice < 0) return;
        
        String text = editor.getDocument().getText();
        String result = processReplacement(choice, text, e);
        
        if (!result.equals(text)) {
            int count = (text.length() - result.length()) / 3;
            Messages.showInfoMessage(
                    e.getProject(),
                    String.format("Replaced %d code sequences", count),
                    "Replacement Complete"
            );
        }
    }

    private String processReplacement(int choice, String text, AnActionEvent e) {
        return switch(choice) {
            case 0 -> text.replaceAll("\u001B\\[1;?31m", "\u001B[32m");
            case 1 -> text.replaceAll("\u001B\\[33m", "\u001B[36m");
            case 2 -> text.replaceAll("\u001B\\[1;", "\u001B[");
            case 3 -> text.replaceAll("\u001B\\[(10[0-7]|4[0-7])m", "\u001B[0m");
            case 4 -> handleCustomReplacement(text, e);
            default -> text;
        };
    }

    private String handleCustomReplacement(String text, AnActionEvent e) {
        String find = Messages.showInputDialog(
                e.getProject(),
                "Enter ANSI code to find:",
                "Find",
                null
        );
        
        if (find == null) return text;
        
        String replace = Messages.showInputDialog(
                e.getProject(),
                "Enter replacement ANSI code:",
                "Replace with",
                null
        );
        
        if (replace == null) return text;
        
        return text.replace(find, replace);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
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

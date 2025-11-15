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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

public class SearchAnsiPatternAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String[] patterns = {
            "All ANSI codes",
            "Color codes (30-37)",
            "Bright colors (90-97)",
            "Bold codes [1m",
            "Reset codes [0m",
            "Background colors (40-47)",
            "256-color codes",
            "Truecolor codes"
        };
        
        int choice = showChoiceDialog(e, "Select pattern to search:", "Search ANSI Patterns", patterns, 0);
        
        if (choice < 0) return;
        
        String text = editor.getDocument().getText();
        String pattern = switch(choice) {
            case 0 -> "\u001B\\[[0-9;]*m";
            case 1 -> "\u001B\\[(3[0-7]|9[0-7])m";
            case 2 -> "\u001B\\[9[0-7]m";
            case 3 -> "\u001B\\[1[;m]";
            case 4 -> "\u001B\\[0m";
            case 5 -> "\u001B\\[(4[0-7]|10[0-7])m";
            case 6 -> "\u001B\\[38;5;\\d+m";
            case 7 -> "\u001B\\[38;2;\\d+;\\d+;\\d+m";
            default -> "";
        };
        
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        
        int count = 0;
        while (m.find()) {
            count++;
        }
        
        String patternName = patterns[choice];
        String result = String.format("Found %d matches for '%s'", count, patternName);
        Messages.showInfoMessage(e.getProject(), result, "Search Results");
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

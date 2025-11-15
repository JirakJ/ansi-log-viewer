package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Pattern;

public class SmartErrorGroupingAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        Map<String, Integer> errorGroups = new LinkedHashMap<>();
        Map<String, List<Integer>> errorLineNumbers = new HashMap<>();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("\u001B\\[[0-9;]*m", "");
            if (line.toUpperCase().contains("ERROR")) {
                // Extract error type (first meaningful part after ERROR)
                String errorKey = extractErrorType(line);
                errorGroups.put(errorKey, errorGroups.getOrDefault(errorKey, 0) + 1);
                
                errorLineNumbers.computeIfAbsent(errorKey, k -> new ArrayList<>()).add(i + 1);
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("ERROR_GROUP_ANALYSIS:\n\n");
        report.append(String.format("Total unique error types: %d\n\n", errorGroups.size()));
        
        errorGroups.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    report.append(String.format("[%dx] %s\n", entry.getValue(), entry.getKey()));
                    report.append("     Lines: ").append(errorLineNumbers.get(entry.getKey())).append("\n");
                });
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(report.toString()), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                report.toString() + "\n\nCopied to clipboard for Copilot!",
                "Error Grouping Analysis"
        );
    }

    private String extractErrorType(String line) {
        String upper = line.toUpperCase();
        int errorIdx = upper.indexOf("ERROR");
        if (errorIdx < 0) return "Unknown";
        
        String afterError = line.substring(Math.min(errorIdx + 5, line.length()));
        String[] parts = afterError.trim().split("[:\\n]");
        
        if (parts.length > 0 && !parts[0].isEmpty()) {
            String type = parts[0].replaceAll("[0-9]+", "X").trim();
            return type.length() > 100 ? type.substring(0, 100) + "..." : type;
        }
        
        return "Generic Error";
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && text.contains("ERROR"));
    }
}

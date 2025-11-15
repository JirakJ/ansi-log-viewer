package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class LogSummaryAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        // Build comprehensive summary
        StringBuilder summary = new StringBuilder();
        summary.append("LOG_SUMMARY_FOR_COPILOT:\n\n");
        summary.append("Please analyze this log:\n\n");
        
        int errorCount = 0, warnCount = 0, infoCount = 0;
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> keywords = new HashSet<>();
        
        for (String line : lines) {
            String clean = line.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "").trim();
            if (clean.isEmpty()) continue;
            
            String upper = clean.toUpperCase();
            if (upper.contains("ERROR")) {
                errorCount++;
                errors.add(clean);
            } else if (upper.contains("WARN")) {
                warnCount++;
                warnings.add(clean);
            } else if (upper.contains("INFO")) {
                infoCount++;
            }
            
            // Extract keywords
            String[] words = clean.split("\\s+");
            for (String word : words) {
                if (word.length() > 6 && !isCommon(word)) {
                    keywords.add(word);
                }
            }
        }
        
        summary.append("STATISTICS:\n");
        summary.append("- Total lines: ").append(lines.length).append("\n");
        summary.append("- Errors: ").append(errorCount).append("\n");
        summary.append("- Warnings: ").append(warnCount).append("\n");
        summary.append("- Info: ").append(infoCount).append("\n\n");
        
        if (!errors.isEmpty()) {
            summary.append("ERRORS:\n");
            errors.stream().limit(5).forEach(error -> summary.append("  ").append(error).append("\n"));
            if (errors.size() > 5) summary.append("  ... and ").append(errors.size() - 5).append(" more\n");
            summary.append("\n");
        }
        
        if (!warnings.isEmpty()) {
            summary.append("WARNINGS:\n");
            warnings.stream().limit(3).forEach(w -> summary.append("  ").append(w).append("\n"));
            if (warnings.size() > 3) summary.append("  ... and ").append(warnings.size() - 3).append(" more\n");
            summary.append("\n");
        }
        
        summary.append("KEY_TERMS: ");
        keywords.stream().limit(10).forEach(k -> summary.append(k).append(", "));
        summary.append("\n\n");
        
        summary.append("QUESTIONS_FOR_ANALYSIS:\n");
        summary.append("1. What caused these errors?\n");
        summary.append("2. Are there any patterns?\n");
        summary.append("3. What actions should be taken?\n");
        
        String summaryText = summary.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(summaryText), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                "Summary copied to clipboard!\n\n" +
                "Errors: " + errorCount + "\n" +
                "Warnings: " + warnCount + "\n" +
                "Total lines: " + lines.length + "\n\n" +
                "Paste into Copilot Chat for AI analysis.",
                "Log Summary"
        );
    }

    private boolean isCommon(String word) {
        String lower = word.toLowerCase();
        return lower.matches("^(the|and|for|from|with|that|this|java|com|org).*");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

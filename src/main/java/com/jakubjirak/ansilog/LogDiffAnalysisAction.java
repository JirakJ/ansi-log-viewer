package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class LogDiffAnalysisAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        // Get filter criteria
        String[] criteria = {
            "Error vs Warning lines",
            "First half vs Second half",
            "Lines with timestamps vs without",
            "Short lines vs Long lines"
        };
        
        int choice = Messages.showChooseDialog(
                e.getProject(),
                "Select comparison type:",
                "Log Diff Analysis",
                Messages.getQuestionIcon(),
                criteria,
                criteria[0]
        );
        
        if (choice < 0) return;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("LOG_DIFF_ANALYSIS:\n\n");
        
        switch(choice) {
            case 0 -> {
                int errors = 0, warnings = 0;
                for (String line : lines) {
                    if (line.contains("ERROR")) errors++;
                    else if (line.contains("WARN")) warnings++;
                }
                analysis.append(String.format("Errors: %d\n", errors));
                analysis.append(String.format("Warnings: %d\n", warnings));
                analysis.append(String.format("Ratio: %.2f%% errors\n", (errors * 100.0 / (errors + warnings))));
            }
            case 1 -> {
                int half = lines.length / 2;
                List<String> firstHalf = Arrays.asList(Arrays.copyOfRange(lines, 0, half));
                List<String> secondHalf = Arrays.asList(Arrays.copyOfRange(lines, half, lines.length));
                
                int errors1 = countOccurrences(firstHalf, "ERROR");
                int errors2 = countOccurrences(secondHalf, "ERROR");
                int warns1 = countOccurrences(firstHalf, "WARN");
                int warns2 = countOccurrences(secondHalf, "WARN");
                
                analysis.append("FIRST_HALF:\n");
                analysis.append(String.format("  Errors: %d, Warnings: %d\n", errors1, warns1));
                analysis.append("SECOND_HALF:\n");
                analysis.append(String.format("  Errors: %d, Warnings: %d\n", errors2, warns2));
                analysis.append(String.format("\nTrend: %s\n", 
                        errors2 > errors1 ? "⚠️ Error count increased" : "✓ Error count decreased"));
            }
            case 2 -> {
                long withTime = Arrays.stream(lines).filter(l -> l.matches(".*\\d{1,2}:\\d{2}:\\d{2}.*")).count();
                long withoutTime = lines.length - withTime;
                analysis.append(String.format("Lines with timestamps: %d (%.1f%%)\n", 
                        withTime, (withTime * 100.0 / lines.length)));
                analysis.append(String.format("Lines without timestamps: %d (%.1f%%)\n", 
                        withoutTime, (withoutTime * 100.0 / lines.length)));
            }
            case 3 -> {
                int avgLength = (int) Arrays.stream(lines).mapToInt(String::length).average().orElse(0);
                long shortLines = Arrays.stream(lines).filter(l -> l.length() < avgLength / 2).count();
                long longLines = Arrays.stream(lines).filter(l -> l.length() > avgLength * 2).count();
                
                analysis.append(String.format("Average line length: %d chars\n", avgLength));
                analysis.append(String.format("Short lines (<%d): %d\n", avgLength / 2, shortLines));
                analysis.append(String.format("Long lines (>%d): %d\n", avgLength * 2, longLines));
            }
        }
        
        String result = analysis.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard!",
                "Log Diff Analysis"
        );
    }

    private int countOccurrences(List<String> lines, String keyword) {
        return (int) lines.stream().filter(l -> l.contains(keyword)).count();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

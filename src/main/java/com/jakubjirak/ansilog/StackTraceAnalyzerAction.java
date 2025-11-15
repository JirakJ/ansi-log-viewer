package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class StackTraceAnalyzerAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("STACK_TRACE_ANALYSIS:\n\n");
        
        List<String> stackTraces = new ArrayList<>();
        StringBuilder currentTrace = new StringBuilder();
        
        for (String line : lines) {
            String clean = line.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "");
            
            if (clean.contains("at ") && clean.contains("(")) {
                currentTrace.append(clean).append("\n");
            } else if (!currentTrace.isEmpty() && !clean.trim().isEmpty()) {
                stackTraces.add(currentTrace.toString());
                currentTrace = new StringBuilder();
            }
        }
        
        if (!currentTrace.isEmpty()) {
            stackTraces.add(currentTrace.toString());
        }
        
        analysis.append(String.format("Found %d stack traces\n\n", stackTraces.size()));
        
        if (!stackTraces.isEmpty()) {
            analysis.append("STACK_TRACE_SUMMARY:\n");
            Set<String> uniqueExceptions = new HashSet<>();
            
            for (String trace : stackTraces) {
                String[] traceParts = trace.split("\n");
                if (traceParts.length > 0) {
                    String root = traceParts[0].trim();
                    uniqueExceptions.add(root);
                    
                    // Extract method names
                    for (String part : traceParts) {
                        if (part.contains("at ") && part.contains("(")) {
                            String method = part.substring(part.indexOf("at ") + 3).split("\\(")[0];
                            analysis.append("  • ").append(method).append("\n");
                        }
                    }
                }
            }
            
            analysis.append("\nUNIQUE_EXCEPTIONS:\n");
            uniqueExceptions.forEach(ex -> analysis.append("  - ").append(ex).append("\n"));
        }
        
        String result = analysis.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard!",
                "Stack Trace Analysis"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && text.contains("at "));
    }
}

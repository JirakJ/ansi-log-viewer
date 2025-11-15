package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class ErrorTrendAnalysisAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("ERROR_TREND_ANALYSIS:\n\n");
        
        // Divide into chunks and analyze
        int chunkSize = Math.max(1, lines.length / 5);
        int[] chunkErrors = new int[5];
        
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("ERROR")) {
                int chunkIndex = Math.min(4, i / chunkSize);
                chunkErrors[chunkIndex]++;
            }
        }
        
        analysis.append("ERROR_COUNT_BY_CHUNK:\n");
        String[] chunks = {"1st 20%", "2nd 20%", "3rd 20%", "4th 20%", "5th 20%"};
        for (int i = 0; i < 5; i++) {
            analysis.append(String.format("  %s: %d errors\n", chunks[i], chunkErrors[i]));
        }
        
        // Detect trend
        analysis.append("\nTREND_ANALYSIS:\n");
        int trend = 0;
        for (int i = 1; i < 5; i++) {
            if (chunkErrors[i] > chunkErrors[i-1]) trend++;
            else if (chunkErrors[i] < chunkErrors[i-1]) trend--;
        }
        
        if (trend > 2) {
            analysis.append("  ⚠️ INCREASING: Error count is rising over time\n");
            analysis.append("  → Suggests deteriorating system health\n");
        } else if (trend < -2) {
            analysis.append("  ✓ IMPROVING: Error count is declining\n");
            analysis.append("  → System is stabilizing or recovering\n");
        } else {
            analysis.append("  ➡️ STABLE: Error count is consistent\n");
            analysis.append("  → System state remains constant\n");
        }
        
        analysis.append("\n");
        analysis.append("DISTRIBUTION_PATTERN:\n");
        
        // Find which chunk has most errors
        int maxChunk = 0;
        int maxErrors = 0;
        for (int i = 0; i < 5; i++) {
            if (chunkErrors[i] > maxErrors) {
                maxErrors = chunkErrors[i];
                maxChunk = i;
            }
        }
        
        if (maxChunk == 0) {
            analysis.append("  🔴 Early failure - errors concentrated at start\n");
        } else if (maxChunk == 4) {
            analysis.append("  ⚠️ Late failures - errors increasing at end\n");
        } else {
            analysis.append("  📊 Distributed errors - throughout the log\n");
        }
        
        String result = analysis.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard!",
                "Error Trend Analysis"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && text.contains("ERROR"));
    }
}

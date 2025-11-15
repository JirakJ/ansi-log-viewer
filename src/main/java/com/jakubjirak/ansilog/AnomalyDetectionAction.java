package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Pattern;

public class AnomalyDetectionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("ANOMALY_DETECTION_RESULTS:\n\n");
        
        // Detect sudden error spikes
        int[] errorWindow = new int[Math.min(50, lines.length)];
        for (int i = 0; i < errorWindow.length; i++) {
            if (lines[i].toUpperCase().contains("ERROR")) {
                errorWindow[i] = 1;
            }
        }
        
        int errorSpikes = 0;
        for (int i = 1; i < errorWindow.length - 1; i++) {
            if (errorWindow[i] == 1 && errorWindow[i-1] == 0 && errorWindow[i+1] == 1) {
                errorSpikes++;
            }
        }
        
        analysis.append("1. ERROR_SPIKES: ").append(errorSpikes).append("\n");
        if (errorSpikes > 2) {
            analysis.append("   ⚠️ Multiple error bursts detected - possible cascading failures\n");
        }
        analysis.append("\n");
        
        // Detect repeated errors
        Map<String, Integer> errorPatterns = new HashMap<>();
        Pattern errorPattern = Pattern.compile("ERROR[^\\n]*");
        for (String line : lines) {
            String clean = line.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "");
            if (clean.contains("ERROR")) {
                String key = clean.replaceAll("[0-9]+", "X").toLowerCase();
                errorPatterns.put(key, errorPatterns.getOrDefault(key, 0) + 1);
            }
        }
        
        analysis.append("2. REPEATED_ERRORS:\n");
        int repeatedCount = 0;
        for (Map.Entry<String, Integer> entry : errorPatterns.entrySet()) {
            if (entry.getValue() > 3) {
                analysis.append("   • \"").append(entry.getKey(), 0, Math.min(60, entry.getKey().length()))
                        .append("...\" (").append(entry.getValue()).append("x)\n");
                repeatedCount++;
            }
        }
        if (repeatedCount == 0) {
            analysis.append("   ✓ No repeated error patterns found\n");
        }
        analysis.append("\n");
        
        // Detect unusual timing gaps
        analysis.append("3. TIME_GAP_ANALYSIS:\n");
        Pattern timePattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
        List<Integer> timestamps = new ArrayList<>();
        for (String line : lines) {
            java.util.regex.Matcher m = timePattern.matcher(line);
            if (m.find()) {
                String time = m.group();
                String[] parts = time.split(":");
                int seconds = Integer.parseInt(parts[0]) * 3600 + 
                             Integer.parseInt(parts[1]) * 60 + 
                             Integer.parseInt(parts[2]);
                timestamps.add(seconds);
            }
        }
        
        if (timestamps.size() > 1) {
            int maxGap = 0, avgGap = 0;
            for (int i = 1; i < timestamps.size(); i++) {
                int gap = timestamps.get(i) - timestamps.get(i-1);
                maxGap = Math.max(maxGap, gap);
                avgGap += gap;
            }
            avgGap /= (timestamps.size() - 1);
            analysis.append("   • Max gap: ").append(maxGap).append("s\n");
            analysis.append("   • Avg gap: ").append(avgGap).append("s\n");
            if (maxGap > avgGap * 3) {
                analysis.append("   ⚠️ Unusual time gaps detected\n");
            }
        }
        analysis.append("\n");
        
        // Log level distribution anomaly
        analysis.append("4. SEVERITY_DISTRIBUTION:\n");
        int total = lines.length;
        int errors = 0, warns = 0;
        for (String line : lines) {
            if (line.contains("ERROR")) errors++;
            else if (line.contains("WARN")) warns++;
        }
        analysis.append("   • Errors: ").append((errors * 100 / total)).append("%\n");
        analysis.append("   • Warnings: ").append((warns * 100 / total)).append("%\n");
        if (errors * 100 / total > 20) {
            analysis.append("   ⚠️ HIGH ERROR RATE - Investigate immediately\n");
        }
        
        // Copy to clipboard for Copilot
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(analysis.toString()), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                analysis.toString() + "\n\n" +
                "Full analysis copied to clipboard!\n" +
                "Paste into Copilot for deeper investigation.",
                "Anomaly Detection"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

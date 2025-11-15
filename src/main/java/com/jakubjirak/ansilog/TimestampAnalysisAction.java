package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampAnalysisAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("TIMESTAMP_ANALYSIS:\n\n");
        
        // Find timestamps
        Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2}):(\\d{2})");
        List<Integer> timestamps = new ArrayList<>();
        List<String> firstAndLastLines = new ArrayList<>();
        
        Integer firstTime = null, lastTime = null;
        
        for (String line : lines) {
            Matcher m = timePattern.matcher(line);
            if (m.find()) {
                String time = m.group();
                String[] parts = time.split(":");
                int seconds = Integer.parseInt(parts[0]) * 3600 + 
                             Integer.parseInt(parts[1]) * 60 + 
                             Integer.parseInt(parts[2]);
                timestamps.add(seconds);
                
                if (firstTime == null) {
                    firstTime = seconds;
                    firstAndLastLines.add("START: " + time + " - " + line.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", ""));
                }
                lastTime = seconds;
            }
        }
        
        if (firstTime == null) {
            Messages.showInfoMessage(e.getProject(), "No timestamps found in log", "Timestamp Analysis");
            return;
        }
        
        if (!firstAndLastLines.isEmpty()) {
            firstAndLastLines.add("END: " + formatTime(lastTime) + " - Last line with timestamp");
        }
        
        int duration = lastTime - firstTime;
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        
        analysis.append("Duration: ").append(hours).append("h ")
                .append(minutes).append("m ").append(seconds).append("s\n");
        analysis.append("Total events: ").append(timestamps.size()).append("\n\n");
        
        // Analyze gaps
        if (timestamps.size() > 1) {
            List<Integer> gaps = new ArrayList<>();
            for (int i = 1; i < timestamps.size(); i++) {
                gaps.add(timestamps.get(i) - timestamps.get(i - 1));
            }
            
            Collections.sort(gaps);
            
            int avgGap = gaps.stream().mapToInt(Integer::intValue).sum() / gaps.size();
            int maxGap = gaps.stream().mapToInt(Integer::intValue).max().orElse(0);
            int minGap = gaps.stream().mapToInt(Integer::intValue).min().orElse(0);
            
            analysis.append("TIMING METRICS:\n");
            analysis.append("  Avg gap: ").append(avgGap).append("s\n");
            analysis.append("  Max gap: ").append(maxGap).append("s\n");
            analysis.append("  Min gap: ").append(minGap).append("s\n\n");
            
            // Find suspicious gaps
            List<Integer> suspiciousGaps = new ArrayList<>();
            for (int gap : gaps) {
                if (gap > avgGap * 2) {
                    suspiciousGaps.add(gap);
                }
            }
            
            if (!suspiciousGaps.isEmpty()) {
                analysis.append("UNUSUAL_GAPS:\n");
                analysis.append("  Count: ").append(suspiciousGaps.size()).append("\n");
                analysis.append("  ⚠️ Possible processing delays or pauses\n\n");
            }
        }
        
        analysis.append("TIME_BOUNDARIES:\n");
        firstAndLastLines.forEach(line -> analysis.append("  ").append(line).append("\n"));
        
        String result = analysis.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard!",
                "Timestamp Analysis"
        );
    }

    private String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        boolean hasTime = text.matches(".*\\d{1,2}:\\d{2}:\\d{2}.*");
        e.getPresentation().setEnabled(hasTime);
    }
}

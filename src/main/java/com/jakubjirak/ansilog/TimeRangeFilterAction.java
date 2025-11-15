package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeRangeFilterAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String startTime = Messages.showInputDialog(
                e.getProject(),
                "Enter start time (HH:mm:ss or leave empty):",
                "Start Time",
                null
        );
        
        String endTime = Messages.showInputDialog(
                e.getProject(),
                "Enter end time (HH:mm:ss or leave empty):",
                "End Time",
                null
        );
        
        if ((startTime == null || startTime.isEmpty()) && (endTime == null || endTime.isEmpty())) {
            Messages.showInfoMessage(e.getProject(), "No time range specified", "Time Filter");
            return;
        }
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2}):(\\d{2})");
        List<String> filtered = new ArrayList<>();
        
        for (String line : lines) {
            Matcher matcher = timePattern.matcher(line);
            if (matcher.find()) {
                String time = matcher.group();
                if (isInTimeRange(time, startTime, endTime)) {
                    filtered.add(line);
                }
            }
        }
        
        String result = String.format("Found %d lines in time range [%s - %s]\n\nPreview:\n%s",
                filtered.size(),
                startTime != null ? startTime : "00:00:00",
                endTime != null ? endTime : "23:59:59",
                filtered.stream()
                        .limit(10)
                        .reduce("", (a, b) -> a + b + "\n"));
        
        Messages.showInfoMessage(e.getProject(), result, "Time Range Filter");
    }

    private boolean isInTimeRange(String time, String start, String end) {
        int t = timeToSeconds(time);
        int s = start != null && !start.isEmpty() ? timeToSeconds(start) : 0;
        int e = end != null && !end.isEmpty() ? timeToSeconds(end) : Integer.MAX_VALUE;
        return t >= s && t <= e;
    }

    private int timeToSeconds(String time) {
        String[] parts = time.split(":");
        if (parts.length != 3) return 0;
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            return hours * 3600 + minutes * 60 + seconds;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

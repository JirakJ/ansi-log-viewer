package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

public class CopilotContextBuilder {
    
    public static String buildLogContext(@NotNull String text, int maxLines) {
        String[] lines = text.split("\n");
        StringBuilder context = new StringBuilder();
        
        context.append("LOG_FILE_ANALYSIS:\n");
        context.append("Total lines: ").append(lines.length).append("\n");
        
        List<String> errorLines = new ArrayList<>();
        List<String> warnLines = new ArrayList<>();
        List<String> infoLines = new ArrayList<>();
        int ansiCodeCount = 0;
        
        for (String line : lines) {
            String cleanLine = line.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
            ansiCodeCount += (int) AnsiPatternUtil.ANSI_PATTERN.matcher(line).results().count();
            
            if (cleanLine.toUpperCase().contains("ERROR")) errorLines.add(cleanLine);
            else if (cleanLine.toUpperCase().contains("WARN")) warnLines.add(cleanLine);
            else if (cleanLine.toUpperCase().contains("INFO")) infoLines.add(cleanLine);
        }
        
        context.append("Error lines: ").append(errorLines.size()).append("\n");
        context.append("Warning lines: ").append(warnLines.size()).append("\n");
        context.append("Info lines: ").append(infoLines.size()).append("\n");
        context.append("Total ANSI codes: ").append(ansiCodeCount).append("\n\n");
        
        if (!errorLines.isEmpty()) {
            context.append("RECENT_ERRORS:\n");
            errorLines.stream()
                    .skip(Math.max(0, errorLines.size() - 5))
                    .forEach(line -> context.append("- ").append(line).append("\n"));
            context.append("\n");
        }
        
        if (!warnLines.isEmpty()) {
            context.append("RECENT_WARNINGS:\n");
            warnLines.stream()
                    .skip(Math.max(0, warnLines.size() - 3))
                    .forEach(line -> context.append("- ").append(line).append("\n"));
            context.append("\n");
        }
        
        context.append("SAMPLE_LINES:\n");
        int sampleSize = Math.min(10, lines.length);
        for (int i = 0; i < sampleSize; i++) {
            String clean = lines[i].replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
            context.append(i + 1).append(": ").append(clean).append("\n");
        }
        
        return context.toString();
    }
    
    public static String buildSelectedContext(@NotNull String selectedText) {
        StringBuilder context = new StringBuilder();
        context.append("SELECTED_LOG_SECTION:\n");
        context.append(selectedText.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", ""));
        return context.toString();
    }
    
    public static String buildErrorContext(@NotNull String text) {
        String[] lines = text.split("\n");
        StringBuilder context = new StringBuilder();
        
        context.append("ERROR_ANALYSIS_CONTEXT:\n");
        
        List<String> errorLines = new ArrayList<>();
        for (String line : lines) {
            String clean = line.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
            if (clean.toUpperCase().contains("ERROR")) {
                errorLines.add(clean);
            }
        }
        
        context.append("Total errors: ").append(errorLines.size()).append("\n\n");
        
        errorLines.stream()
                .limit(20)
                .forEach(line -> context.append("• ").append(line).append("\n"));
        
        return context.toString();
    }
}

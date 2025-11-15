package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.security.MessageDigest;
import java.util.*;

public class ExtendedContextBuilder {
    
    public static String buildFullAnalysisContext(@NotNull String text) {
        StringBuilder context = new StringBuilder();
        context.append("FULL_LOG_ANALYSIS_CONTEXT:\n\n");
        
        // Generate log hash for caching
        String logHash = generateHash(text);
        context.append("LOG_HASH: ").append(logHash).append("\n");
        context.append("TIMESTAMP: ").append(System.currentTimeMillis()).append("\n\n");
        
        // Statistics
        String[] lines = text.split("\n");
        context.append("STATISTICS:\n");
        context.append("  Lines: ").append(lines.length).append("\n");
        context.append("  Size: ").append(formatBytes(text.length())).append("\n");
        
        // Error analysis
        long errors = Arrays.stream(lines).filter(l -> l.contains("ERROR")).count();
        long warnings = Arrays.stream(lines).filter(l -> l.contains("WARN")).count();
        long info = Arrays.stream(lines).filter(l -> l.contains("INFO")).count();
        
        context.append("  Errors: ").append(errors).append("\n");
        context.append("  Warnings: ").append(warnings).append("\n");
        context.append("  Info: ").append(info).append("\n\n");
        
        // Timeline
        context.append("TIMELINE:\n");
        Optional<String> first = Arrays.stream(lines).findFirst();
        first.ifPresent(f -> context.append("  Start: ").append(f).append("\n"));
        Optional<String> last = Arrays.stream(lines).reduce((a, b) -> b);
        last.ifPresent(l -> context.append("  End: ").append(l).append("\n"));
        context.append("\n");
        
        // Severity breakdown
        context.append("SEVERITY_BREAKDOWN:\n");
        context.append(String.format("  Critical: %d%%\n", (int)((errors * 100) / lines.length)));
        context.append(String.format("  Warning: %d%%\n", (int)((warnings * 100) / lines.length)));
        context.append(String.format("  Normal: %d%%\n", (int)((info * 100) / lines.length)));
        context.append("\n");
        
        // Sample lines
        context.append("SAMPLE_CONTENT:\n");
        int sampleCount = Math.min(20, lines.length);
        for (int i = 0; i < sampleCount; i++) {
            String clean = lines[i].replaceAll("\u001B\\[[0-9;]*m", "");
            context.append(String.format("%d: %s\n", i + 1, clean.length() > 100 ? clean.substring(0, 100) + "..." : clean));
        }
        
        return context.toString();
    }

    private static String generateHash(@NotNull String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.substring(0, 16);
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}

package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.security.MessageDigest;
import java.util.*;

public class LogFingerprintAnalyzer {
    
    public static class LogFingerprint {
        public String hash;
        public long fileSize;
        public int lineCount;
        public Map<String, Integer> errorFrequency;
        public int uniqueErrors;
        public long lastModified;
        public String framework;
        public List<String> suspiciousPatterns;
        
        public LogFingerprint() {
            this.errorFrequency = new LinkedHashMap<>();
            this.suspiciousPatterns = new ArrayList<>();
        }
    }
    
    public static LogFingerprint generateFingerprint(@NotNull String text) {
        LogFingerprint fp = new LogFingerprint();
        
        // Basic info
        fp.hash = generateHash(text);
        fp.fileSize = text.length();
        fp.lastModified = System.currentTimeMillis();
        
        String[] lines = text.split("\n");
        fp.lineCount = lines.length;
        
        // Error analysis
        Map<String, Integer> errorMap = new HashMap<>();
        for (String line : lines) {
            if (line.contains("ERROR") || line.contains("Exception")) {
                String cleanLine = line.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
                String error = extractErrorType(cleanLine);
                errorMap.put(error, errorMap.getOrDefault(error, 0) + 1);
            }
        }
        fp.errorFrequency = errorMap;
        fp.uniqueErrors = errorMap.size();
        
        // Detect framework
        fp.framework = detectFramework(text);
        
        // Find suspicious patterns
        fp.suspiciousPatterns = findSuspiciousPatterns(text);
        
        return fp;
    }
    
    public static boolean isLogIdentical(@NotNull String hash1, @NotNull String hash2) {
        return hash1.equals(hash2);
    }
    
    public static double calculateSimilarity(@NotNull String text1, @NotNull String text2) {
        String[] lines1 = text1.split("\n");
        String[] lines2 = text2.split("\n");
        
        int matches = 0;
        int total = Math.max(lines1.length, lines2.length);
        
        for (int i = 0; i < Math.min(lines1.length, lines2.length); i++) {
            String clean1 = cleanLine(lines1[i]);
            String clean2 = cleanLine(lines2[i]);
            if (clean1.equals(clean2)) {
                matches++;
            }
        }
        
        return (matches * 100.0) / total;
    }
    
    public static List<String> findAnomalies(@NotNull String text, @NotNull LogFingerprint baseline) {
        List<String> anomalies = new ArrayList<>();
        LogFingerprint current = generateFingerprint(text);
        
        // Check error count change
        int currentErrorCount = current.errorFrequency.values().stream().mapToInt(Integer::intValue).sum();
        int baselineErrorCount = baseline.errorFrequency.values().stream().mapToInt(Integer::intValue).sum();
        
        if (currentErrorCount > baselineErrorCount * 1.5) {
            anomalies.add(String.format("Error count increased: %d → %d (+%.0f%%)", 
                baselineErrorCount, currentErrorCount, 
                ((currentErrorCount - baselineErrorCount) * 100.0 / baselineErrorCount)));
        }
        
        // Check for new error types
        for (String errorType : current.errorFrequency.keySet()) {
            if (!baseline.errorFrequency.containsKey(errorType)) {
                anomalies.add("New error type detected: " + errorType);
            }
        }
        
        // Check file size changes
        if (current.fileSize > baseline.fileSize * 2) {
            anomalies.add(String.format("File size doubled: %.2f MB → %.2f MB",
                baseline.fileSize / 1_000_000.0, current.fileSize / 1_000_000.0));
        }
        
        // Check suspicious patterns
        if (!current.suspiciousPatterns.isEmpty()) {
            anomalies.add("Suspicious patterns detected: " + String.join(", ", current.suspiciousPatterns));
        }
        
        return anomalies;
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
    
    private static String extractErrorType(String line) {
        if (line.contains("NullPointerException")) return "NullPointerException";
        if (line.contains("OutOfMemoryError")) return "OutOfMemoryError";
        if (line.contains("ClassNotFoundException")) return "ClassNotFoundException";
        if (line.contains("SQLException")) return "SQLException";
        if (line.contains("IOException")) return "IOException";
        if (line.contains("TimeoutException")) return "TimeoutException";
        if (line.contains("ConnectionRefused")) return "ConnectionRefused";
        
        int errorIdx = line.indexOf("ERROR");
        if (errorIdx >= 0) {
            String after = line.substring(errorIdx + 5).trim();
            return after.length() > 50 ? after.substring(0, 50) : after;
        }
        
        return "Unknown Error";
    }
    
    private static String detectFramework(@NotNull String text) {
        if (text.contains("Spring") || text.contains("org.springframework")) return "Spring Boot";
        if (text.contains("Hibernate")) return "Hibernate";
        if (text.contains("Quarkus")) return "Quarkus";
        if (text.contains("Vert.x")) return "Vert.x";
        return "Custom/Unknown";
    }
    
    private static List<String> findSuspiciousPatterns(@NotNull String text) {
        List<String> patterns = new ArrayList<>();
        
        if (text.contains("OutOfMemory")) patterns.add("Memory overflow");
        if (text.contains("StackOverflow")) patterns.add("Stack overflow");
        if (text.contains("DeadLock")) patterns.add("Potential deadlock");
        if (text.contains("Security") && text.contains("breach")) patterns.add("Security issue");
        if (countOccurrences(text, "ERROR") > 100) patterns.add("Very high error rate");
        
        return patterns;
    }
    
    private static int countOccurrences(String text, String word) {
        return (int) text.split(word, -1).length - 1;
    }
    
    private static String cleanLine(String line) {
        return line.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "").trim();
    }
}

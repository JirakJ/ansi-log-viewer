package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class MultiFileLogComparator {
    
    public static class ComparisonResult {
        public List<String> commonLines;
        public List<String> uniqueToFile1;
        public List<String> uniqueToFile2;
        public Map<String, Integer> frequencyDiff;
        public double similarity;
        
        public ComparisonResult() {
            this.commonLines = new ArrayList<>();
            this.uniqueToFile1 = new ArrayList<>();
            this.uniqueToFile2 = new ArrayList<>();
            this.frequencyDiff = new LinkedHashMap<>();
        }
    }
    
    public static ComparisonResult compareFiles(@NotNull String log1, @NotNull String log2) {
        ComparisonResult result = new ComparisonResult();
        
        Set<String> lines1 = new HashSet<>(Arrays.asList(log1.split("\n")));
        Set<String> lines2 = new HashSet<>(Arrays.asList(log2.split("\n")));
        
        // Common lines
        for (String line : lines1) {
            if (lines2.contains(line)) {
                result.commonLines.add(line);
            }
        }
        
        // Unique lines
        for (String line : lines1) {
            if (!lines2.contains(line)) {
                result.uniqueToFile1.add(line);
            }
        }
        
        for (String line : lines2) {
            if (!lines1.contains(line)) {
                result.uniqueToFile2.add(line);
            }
        }
        
        // Similarity
        int maxSize = Math.max(lines1.size(), lines2.size());
        result.similarity = (result.commonLines.size() * 100.0) / maxSize;
        
        return result;
    }
    
    public static Map<String, List<String>> groupByPattern(@NotNull List<String> logs, @NotNull String pattern) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        
        for (int i = 0; i < logs.size(); i++) {
            java.util.regex.Matcher m = p.matcher(logs.get(i));
            if (m.find()) {
                String key = m.group();
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(logs.get(i));
            }
        }
        
        return groups;
    }
    
    public static Map<String, Integer> mergeStatistics(@NotNull List<String> logs) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        
        for (String log : logs) {
            int errors = countOccurrences(log, "ERROR");
            int warnings = countOccurrences(log, "WARN");
            int info = countOccurrences(log, "INFO");
            
            stats.put("Total Errors", stats.getOrDefault("Total Errors", 0) + errors);
            stats.put("Total Warnings", stats.getOrDefault("Total Warnings", 0) + warnings);
            stats.put("Total Info", stats.getOrDefault("Total Info", 0) + info);
        }
        
        return stats;
    }
    
    public static List<String> findCommonErrors(@NotNull List<String> logs) {
        Map<String, Integer> errorCounts = new HashMap<>();
        
        for (String log : logs) {
            Set<String> logErrors = extractErrors(log);
            for (String error : logErrors) {
                errorCounts.put(error, errorCounts.getOrDefault(error, 0) + 1);
            }
        }
        
        // Return errors found in all logs
        List<String> common = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : errorCounts.entrySet()) {
            if (entry.getValue() == logs.size()) {
                common.add(entry.getKey());
            }
        }
        
        return common;
    }
    
    private static Set<String> extractErrors(@NotNull String log) {
        Set<String> errors = new HashSet<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(NullPointerException|OutOfMemoryError|SQLException|IOException|TimeoutException)"
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(log);
        while (matcher.find()) {
            errors.add(matcher.group());
        }
        
        return errors;
    }
    
    private static int countOccurrences(@NotNull String text, @NotNull String word) {
        return (int) text.split(word, -1).length - 1;
    }
}

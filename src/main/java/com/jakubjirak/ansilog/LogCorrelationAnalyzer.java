package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Pattern;

public class LogCorrelationAnalyzer {
    
    public static class CorrelationPair {
        public String pattern1;
        public String pattern2;
        public int cooccurrences;
        public double correlation;
        public List<Integer> lineNumbers1;
        public List<Integer> lineNumbers2;
        
        public CorrelationPair(String p1, String p2) {
            this.pattern1 = p1;
            this.pattern2 = p2;
            this.lineNumbers1 = new ArrayList<>();
            this.lineNumbers2 = new ArrayList<>();
        }
    }
    
    public static class CausalSequence {
        public String antecedent;
        public String consequence;
        public int occurrences;
        public long avgDelayMs;
        public List<String> examples;
        
        public CausalSequence(String antecedent, String consequence) {
            this.antecedent = antecedent;
            this.consequence = consequence;
            this.examples = new ArrayList<>();
        }
    }
    
    public static class CorrelationResult {
        public List<CorrelationPair> correlations;
        public List<CausalSequence> causalSequences;
        public Map<String, List<String>> relatedErrors;
        public Map<String, Integer> errorChains;
        
        public CorrelationResult() {
            this.correlations = new ArrayList<>();
            this.causalSequences = new ArrayList<>();
            this.relatedErrors = new LinkedHashMap<>();
            this.errorChains = new LinkedHashMap<>();
        }
    }
    
    public static CorrelationResult analyzeCorrelations(@NotNull String logContent) {
        CorrelationResult result = new CorrelationResult();
        String[] lines = logContent.split("\n");
        
        List<String> commonPatterns = extractCommonPatterns(logContent);
        
        // Find correlations between patterns
        for (int i = 0; i < commonPatterns.size(); i++) {
            for (int j = i + 1; j < commonPatterns.size(); j++) {
                CorrelationPair pair = correlatePatterns(
                        commonPatterns.get(i), 
                        commonPatterns.get(j), 
                        lines);
                if (pair.cooccurrences > 0) {
                    result.correlations.add(pair);
                }
            }
        }
        
        // Find causal sequences
        for (int i = 0; i < commonPatterns.size(); i++) {
            for (int j = 0; j < commonPatterns.size(); j++) {
                if (i != j) {
                    CausalSequence seq = findCausalSequence(
                            commonPatterns.get(i), 
                            commonPatterns.get(j), 
                            lines);
                    if (seq.occurrences > 0) {
                        result.causalSequences.add(seq);
                    }
                }
            }
        }
        
        // Find related errors
        extractRelatedErrors(lines, result);
        
        // Find error chains
        analyzeErrorChains(lines, result);
        
        return result;
    }
    
    private static List<String> extractCommonPatterns(@NotNull String content) {
        List<String> patterns = new ArrayList<>();
        patterns.add("ERROR");
        patterns.add("WARN");
        patterns.add("Exception");
        patterns.add("timeout");
        patterns.add("connection");
        patterns.add("failed");
        patterns.add("retry");
        patterns.add("fallback");
        patterns.add("unauthorized");
        patterns.add("database");
        return patterns;
    }
    
    private static CorrelationPair correlatePatterns(@NotNull String p1, @NotNull String p2, @NotNull String[] lines) {
        CorrelationPair pair = new CorrelationPair(p1, p2);
        
        List<Integer> lines1 = new ArrayList<>();
        List<Integer> lines2 = new ArrayList<>();
        
        Pattern pattern1 = Pattern.compile(p1, Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile(p2, Pattern.CASE_INSENSITIVE);
        
        for (int i = 0; i < lines.length; i++) {
            if (pattern1.matcher(lines[i]).find()) lines1.add(i);
            if (pattern2.matcher(lines[i]).find()) lines2.add(i);
        }
        
        pair.lineNumbers1 = lines1;
        pair.lineNumbers2 = lines2;
        
        // Calculate correlation
        if (lines1.isEmpty() || lines2.isEmpty()) {
            pair.correlation = 0.0;
        } else {
            int overlaps = 0;
            for (int line1 : lines1) {
                for (int line2 : lines2) {
                    if (Math.abs(line1 - line2) <= 5) {
                        overlaps++;
                    }
                }
            }
            pair.cooccurrences = overlaps;
            pair.correlation = (double) overlaps / Math.max(lines1.size(), lines2.size());
        }
        
        return pair;
    }
    
    private static CausalSequence findCausalSequence(@NotNull String antecedent, @NotNull String consequence, @NotNull String[] lines) {
        CausalSequence seq = new CausalSequence(antecedent, consequence);
        
        Pattern antPattern = Pattern.compile(antecedent, Pattern.CASE_INSENSITIVE);
        Pattern conseqPattern = Pattern.compile(consequence, Pattern.CASE_INSENSITIVE);
        
        for (int i = 0; i < lines.length - 1; i++) {
            if (antPattern.matcher(lines[i]).find()) {
                for (int j = i + 1; j < Math.min(i + 10, lines.length); j++) {
                    if (conseqPattern.matcher(lines[j]).find()) {
                        seq.occurrences++;
                        seq.examples.add(lines[i] + " -> " + lines[j]);
                        break;
                    }
                }
            }
        }
        
        return seq;
    }
    
    private static void extractRelatedErrors(@NotNull String[] lines, @NotNull CorrelationResult result) {
        Map<String, Set<String>> errorMap = new LinkedHashMap<>();
        
        for (String line : lines) {
            if (line.contains("ERROR") || line.contains("Exception")) {
                String errorType = extractErrorType(line);
                String errorContext = extractContext(line);
                
                errorMap.computeIfAbsent(errorType, k -> new HashSet<>())
                        .add(errorContext);
            }
        }
        
        errorMap.forEach((type, contexts) -> 
            result.relatedErrors.put(type, new ArrayList<>(contexts))
        );
    }
    
    private static void analyzeErrorChains(@NotNull String[] lines, @NotNull CorrelationResult result) {
        StringBuilder chain = new StringBuilder();
        
        for (String line : lines) {
            if (line.contains("ERROR") || line.contains("WARN")) {
                if (!chain.isEmpty()) {
                    chain.append(" -> ");
                }
                chain.append(extractErrorType(line));
            }
        }
        
        if (chain.length() > 0) {
            result.errorChains.put("Chain", 1);
        }
    }
    
    private static String extractErrorType(String line) {
        if (line.contains("NullPointerException")) return "NPE";
        if (line.contains("OutOfMemoryError")) return "OOM";
        if (line.contains("SQLException")) return "SQL";
        if (line.contains("IOException")) return "IO";
        if (line.contains("timeout")) return "Timeout";
        if (line.contains("Connection refused")) return "ConnRefused";
        return "Unknown";
    }
    
    private static String extractContext(String line) {
        int idx = line.indexOf("-");
        return idx > 0 ? line.substring(idx + 1).trim() : line;
    }
    
    public static String generateCorrelationReport(@NotNull CorrelationResult result) {
        StringBuilder report = new StringBuilder();
        
        report.append("╔════════════════════════════════════════════════════════════╗\n");
        report.append("║             LOG CORRELATION ANALYSIS REPORT                 ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        report.append("Correlations:\n");
        result.correlations.stream()
                .sorted((a, b) -> Double.compare(b.correlation, a.correlation))
                .forEach(corr -> {
                    report.append(String.format("  %s <-> %s: %.2f%% (cooccurrences: %d)\n",
                            corr.pattern1, corr.pattern2, corr.correlation * 100, corr.cooccurrences));
                });
        
        report.append("\nCausal Sequences:\n");
        result.causalSequences.stream()
                .sorted((a, b) -> Integer.compare(b.occurrences, a.occurrences))
                .limit(10)
                .forEach(seq -> {
                    report.append(String.format("  %s -> %s (%d occurrences)\n",
                            seq.antecedent, seq.consequence, seq.occurrences));
                });
        
        report.append("\nRelated Errors:\n");
        result.relatedErrors.forEach((type, contexts) -> {
            report.append(String.format("  %s: %s\n", type, String.join(", ", contexts.stream().limit(3).toList())));
        });
        
        return report.toString();
    }
}

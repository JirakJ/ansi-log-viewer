package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class PredictiveAlertEngine {
    
    public static class AlertRule {
        public String name;
        public String pattern;
        public int threshold;
        public long timeWindowMs;
        public String severity;
        public String action;
        
        public AlertRule(String name, String pattern, int threshold) {
            this.name = name;
            this.pattern = pattern;
            this.threshold = threshold;
            this.timeWindowMs = 60000;
            this.severity = "MEDIUM";
            this.action = "NOTIFY";
        }
    }
    
    public static class PredictedAlert {
        public String alertName;
        public String severity;
        public double probability;
        public String reason;
        public long predictedTimeMs;
        public List<String> evidence;
        
        public PredictedAlert(String alertName, double probability) {
            this.alertName = alertName;
            this.probability = probability;
            this.evidence = new ArrayList<>();
        }
    }
    
    public static class AlertMetrics {
        public int totalAlerts;
        public int criticalAlerts;
        public int falsePositives;
        public double averageProbability;
        public Map<String, Integer> alertCounts;
        
        public AlertMetrics() {
            this.alertCounts = new HashMap<>();
        }
    }
    
    private List<AlertRule> rules;
    private AlertMetrics metrics;
    
    public PredictiveAlertEngine() {
        this.rules = new ArrayList<>();
        this.metrics = new AlertMetrics();
        initializeDefaultRules();
    }
    
    private void initializeDefaultRules() {
        rules.add(createRule("High Error Rate", 
            "ERROR", 10, 60000, "HIGH"));
        rules.add(createRule("Memory Issues", 
            "OutOfMemoryError|StackOverflowError", 1, 300000, "CRITICAL"));
        rules.add(createRule("Connection Failures", 
            "Connection refused|Connection timeout|Connection reset", 5, 120000, "HIGH"));
        rules.add(createRule("Authorization Failures", 
            "Unauthorized|Forbidden|PermissionDenied", 3, 60000, "MEDIUM"));
        rules.add(createRule("Database Issues", 
            "SQLException|Database connection failed|Query timeout", 2, 300000, "HIGH"));
        rules.add(createRule("Timeout Pattern", 
            "timeout|TimeoutException|Timeout occurred", 5, 120000, "MEDIUM"));
        rules.add(createRule("Resource Exhaustion", 
            "No space left|Disk full|File descriptor limit", 1, 600000, "CRITICAL"));
    }
    
    private AlertRule createRule(String name, String pattern, int threshold, long timeWindow, String severity) {
        AlertRule rule = new AlertRule(name, pattern, threshold);
        rule.timeWindowMs = timeWindow;
        rule.severity = severity;
        return rule;
    }
    
    public List<PredictedAlert> predictAlerts(@NotNull String logContent) {
        List<PredictedAlert> alerts = new ArrayList<>();
        String[] lines = logContent.split("\n");
        
        for (AlertRule rule : rules) {
            int matchCount = countMatches(logContent, rule.pattern);
            
            if (matchCount > 0) {
                double probability = Math.min(1.0, (double) matchCount / rule.threshold);
                PredictedAlert alert = new PredictedAlert(rule.name, probability);
                alert.severity = rule.severity;
                alert.predictedTimeMs = calculatePredictedTime(lines, rule.pattern);
                alert.reason = generateReason(rule, matchCount);
                alert.evidence = extractEvidence(lines, rule.pattern, 3);
                
                alerts.add(alert);
                metrics.alertCounts.merge(rule.name, 1, Integer::sum);
                
                if (probability >= 0.8) {
                    metrics.totalAlerts++;
                    if (rule.severity.equals("CRITICAL")) {
                        metrics.criticalAlerts++;
                    }
                }
            }
        }
        
        calculateMetrics(alerts);
        return alerts;
    }
    
    private int countMatches(@NotNull String content, @NotNull String pattern) {
        try {
            return (int) java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(content)
                    .results()
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long calculatePredictedTime(String[] lines, String pattern) {
        int lastMatchLine = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i].matches("(?i).*" + pattern + ".*")) {
                lastMatchLine = i;
                break;
            }
        }
        return lastMatchLine >= 0 ? (lines.length - lastMatchLine) * 100 : 0;
    }
    
    private String generateReason(@NotNull AlertRule rule, int matchCount) {
        return String.format("%s detected %d times (threshold: %d)", 
                rule.name, matchCount, rule.threshold);
    }
    
    private List<String> extractEvidence(String[] lines, String pattern, int maxCount) {
        List<String> evidence = new ArrayList<>();
        int count = 0;
        
        for (int i = lines.length - 1; i >= 0 && count < maxCount; i--) {
            if (lines[i].matches("(?i).*" + pattern + ".*")) {
                evidence.add(0, truncate(lines[i], 80));
                count++;
            }
        }
        
        return evidence;
    }
    
    private void calculateMetrics(List<PredictedAlert> alerts) {
        if (alerts.isEmpty()) return;
        
        double sum = alerts.stream()
                .mapToDouble(a -> a.probability)
                .sum();
        metrics.averageProbability = sum / alerts.size();
    }
    
    public void addRule(@NotNull AlertRule rule) {
        rules.add(rule);
    }
    
    public AlertMetrics getMetrics() {
        return metrics;
    }
    
    public String generateAlertReport(@NotNull List<PredictedAlert> alerts) {
        StringBuilder report = new StringBuilder();
        
        report.append("╔════════════════════════════════════════════════════════════╗\n");
        report.append("║              PREDICTIVE ALERT ANALYSIS                      ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Total Alerts: %d\n", metrics.totalAlerts));
        report.append(String.format("Critical Alerts: %d\n", metrics.criticalAlerts));
        report.append(String.format("Average Probability: %.2f%%\n\n", metrics.averageProbability * 100));
        
        alerts.stream()
                .sorted((a, b) -> Double.compare(b.probability, a.probability))
                .forEach(alert -> {
                    report.append(String.format("[%s] %s (%.0f%% confidence)\n",
                            alert.severity, alert.alertName, alert.probability * 100));
                    report.append(String.format("  Reason: %s\n", alert.reason));
                    if (!alert.evidence.isEmpty()) {
                        report.append("  Evidence:\n");
                        alert.evidence.forEach(e -> report.append(String.format("    • %s\n", e)));
                    }
                });
        
        return report.toString();
    }
    
    private String truncate(String text, int length) {
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }
}

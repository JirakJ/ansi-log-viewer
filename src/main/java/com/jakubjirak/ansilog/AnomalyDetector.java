package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class AnomalyDetector {
    
    public static class Anomaly {
        public int lineNumber;
        public String line;
        public String anomalyType;
        public double score;
        public String reason;
        public List<String> relatedLines;
        
        public Anomaly(int lineNumber, String line) {
            this.lineNumber = lineNumber;
            this.line = line;
            this.relatedLines = new ArrayList<>();
        }
    }
    
    public static class AnomalyReport {
        public List<Anomaly> anomalies;
        public int totalLines;
        public double anomalyRate;
        public Map<String, Integer> anomalyTypes;
        public List<String> insights;
        
        public AnomalyReport() {
            this.anomalies = new ArrayList<>();
            this.anomalyTypes = new LinkedHashMap<>();
            this.insights = new ArrayList<>();
        }
    }
    
    public static AnomalyReport detectAnomalies(@NotNull String logContent) {
        AnomalyReport report = new AnomalyReport();
        String[] lines = logContent.split("\n");
        report.totalLines = lines.length;
        
        // Calculate baseline statistics
        Map<String, Double> baseline = calculateBaseline(lines);
        
        // Detect various anomalies
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Type 1: Sudden increase in errors
            if (i > 0 && hasAbruptChange(lines, i, "ERROR")) {
                Anomaly anomaly = new Anomaly(i, line);
                anomaly.anomalyType = "Abrupt Error Spike";
                anomaly.score = 0.85;
                anomaly.reason = "Unexpected increase in error frequency";
                anomaly.relatedLines = extractRelatedLines(lines, i, 2);
                report.anomalies.add(anomaly);
            }
            
            // Type 2: Unusual log length
            if (line.length() > baseline.get("avgLineLength") * 3) {
                Anomaly anomaly = new Anomaly(i, line);
                anomaly.anomalyType = "Unusually Long Line";
                anomaly.score = 0.6;
                anomaly.reason = "Line length exceeds baseline by 300%";
                report.anomalies.add(anomaly);
            }
            
            // Type 3: Rare patterns
            if (isRarePattern(line, lines)) {
                Anomaly anomaly = new Anomaly(i, line);
                anomaly.anomalyType = "Rare Pattern";
                anomaly.score = 0.72;
                anomaly.reason = "Unique pattern not observed elsewhere";
                report.anomalies.add(anomaly);
            }
            
            // Type 4: Out-of-order severity
            if (i > 0 && isOutOfOrderSeverity(lines[i - 1], line)) {
                Anomaly anomaly = new Anomaly(i, line);
                anomaly.anomalyType = "Severity Reversal";
                anomaly.score = 0.65;
                anomaly.reason = "Lower severity follows higher severity abnormally";
                report.anomalies.add(anomaly);
            }
        }
        
        // Calculate metrics
        report.anomalyRate = (double) report.anomalies.size() / report.totalLines;
        
        // Count anomaly types
        for (Anomaly anomaly : report.anomalies) {
            report.anomalyTypes.merge(anomaly.anomalyType, 1, Integer::sum);
        }
        
        // Generate insights
        generateInsights(report);
        
        return report;
    }
    
    private static Map<String, Double> calculateBaseline(@NotNull String[] lines) {
        Map<String, Double> baseline = new HashMap<>();
        
        double totalLength = 0;
        int errorCount = 0;
        
        for (String line : lines) {
            totalLength += line.length();
            if (line.contains("ERROR")) errorCount++;
        }
        
        baseline.put("avgLineLength", totalLength / lines.length);
        baseline.put("errorRate", (double) errorCount / lines.length);
        baseline.put("lineCount", (double) lines.length);
        
        return baseline;
    }
    
    private static boolean hasAbruptChange(@NotNull String[] lines, int currentIndex, @NotNull String pattern) {
        int windowSize = Math.min(5, currentIndex);
        int countBefore = 0;
        int countAfter = 0;
        
        for (int i = Math.max(0, currentIndex - windowSize); i < currentIndex; i++) {
            if (lines[i].contains(pattern)) countBefore++;
        }
        
        for (int i = currentIndex; i < Math.min(currentIndex + windowSize, lines.length); i++) {
            if (lines[i].contains(pattern)) countAfter++;
        }
        
        return countAfter > countBefore * 2;
    }
    
    private static boolean isRarePattern(@NotNull String line, @NotNull String[] allLines) {
        int similarity = 0;
        for (String other : allLines) {
            if (stringSimilarity(line, other) > 0.8) {
                similarity++;
            }
        }
        return similarity <= 2;
    }
    
    private static boolean isOutOfOrderSeverity(@NotNull String line1, @NotNull String line2) {
        int sev1 = getSeverityLevel(line1);
        int sev2 = getSeverityLevel(line2);
        return sev2 < sev1;
    }
    
    private static int getSeverityLevel(@NotNull String line) {
        if (line.contains("FATAL")) return 5;
        if (line.contains("ERROR")) return 4;
        if (line.contains("WARN")) return 3;
        if (line.contains("INFO")) return 2;
        if (line.contains("DEBUG")) return 1;
        return 0;
    }
    
    private static double stringSimilarity(@NotNull String s1, @NotNull String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        int matches = 0;
        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) == s2.charAt(i)) matches++;
        }
        
        return (double) matches / maxLength;
    }
    
    private static List<String> extractRelatedLines(@NotNull String[] lines, int index, int count) {
        List<String> related = new ArrayList<>();
        for (int i = Math.max(0, index - count); i <= Math.min(lines.length - 1, index + count); i++) {
            if (i != index && lines[i].contains("ERROR")) {
                related.add(lines[i]);
            }
        }
        return related;
    }
    
    private static void generateInsights(@NotNull AnomalyReport report) {
        if (report.anomalyRate > 0.1) {
            report.insights.add("⚠️ High anomaly rate detected - " + String.format("%.1f%%", report.anomalyRate * 100));
        }
        if (report.anomalyTypes.containsKey("Abrupt Error Spike")) {
            report.insights.add("🔴 Error spike detected - investigate recent changes");
        }
        if (report.anomalyTypes.containsKey("Severity Reversal")) {
            report.insights.add("⚠️ Severity pattern inconsistencies found");
        }
        if (report.anomalies.isEmpty()) {
            report.insights.add("✅ No significant anomalies detected");
        }
    }
    
    public static String generateAnomalyReport(@NotNull AnomalyReport report) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║                   ANOMALY DETECTION REPORT                  ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        sb.append(String.format("Total Lines: %d\n", report.totalLines));
        sb.append(String.format("Anomalies Found: %d (%.2f%%)\n\n", 
                report.anomalies.size(), report.anomalyRate * 100));
        
        sb.append("Anomaly Types:\n");
        report.anomalyTypes.forEach((type, count) -> {
            sb.append(String.format("  %s: %d\n", type, count));
        });
        
        sb.append("\nInsights:\n");
        report.insights.forEach(insight -> sb.append("  " + insight + "\n"));
        
        sb.append("\nTop Anomalies:\n");
        report.anomalies.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(5)
                .forEach(anomaly -> {
                    sb.append(String.format("[Line %d] %s (%.0f%% confidence)\n",
                            anomaly.lineNumber, anomaly.anomalyType, anomaly.score * 100));
                    sb.append(String.format("  Reason: %s\n", anomaly.reason));
                });
        
        return sb.toString();
    }
}

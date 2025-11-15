package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class QualityGateCheck {
    
    public static class QualityGate {
        public String name;
        public String condition;
        public boolean passed;
        public String errorMessage;
        public int severity;
        
        public QualityGate(String name, int severity) {
            this.name = name;
            this.severity = severity;
            this.passed = true;
        }
    }
    
    public static class QualityReport {
        public List<QualityGate> gates;
        public boolean allPassed;
        public int passedCount;
        public int failedCount;
        public Map<String, Integer> severityCount;
        public String overallGrade;
        
        public QualityReport() {
            this.gates = new ArrayList<>();
            this.severityCount = new HashMap<>();
        }
    }
    
    public static QualityReport runQualityGates(@NotNull String logContent) {
        QualityReport report = new QualityReport();
        
        // Gate 1: Error Rate
        QualityGate errorGate = new QualityGate("Error Rate", 1);
        double errorRate = countOccurrences(logContent, "ERROR|FATAL") / (double) logContent.split("\n").length;
        errorGate.passed = errorRate < 0.2;
        if (!errorGate.passed) errorGate.errorMessage = "Error rate exceeds 20%";
        report.gates.add(errorGate);
        
        // Gate 2: Memory Safety
        QualityGate memoryGate = new QualityGate("Memory Safety", 1);
        memoryGate.passed = !logContent.contains("OutOfMemoryError") && !logContent.contains("StackOverflowError");
        if (!memoryGate.passed) memoryGate.errorMessage = "Memory errors detected";
        report.gates.add(memoryGate);
        
        // Gate 3: Connectivity
        QualityGate connectivityGate = new QualityGate("Connectivity", 2);
        int connectionErrors = countOccurrences(logContent, "Connection refused|Connection timeout");
        connectivityGate.passed = connectionErrors < 5;
        if (!connectivityGate.passed) connectivityGate.errorMessage = "Multiple connection failures detected";
        report.gates.add(connectivityGate);
        
        // Gate 4: Security
        QualityGate securityGate = new QualityGate("Security", 1);
        securityGate.passed = !logContent.contains("Unauthorized") && !logContent.contains("PermissionDenied");
        if (!securityGate.passed) securityGate.errorMessage = "Security/authorization issues found";
        report.gates.add(securityGate);
        
        // Gate 5: Data Integrity
        QualityGate integrityGate = new QualityGate("Data Integrity", 1);
        integrityGate.passed = !logContent.contains("data corruption") && !logContent.contains("inconsistency");
        if (!integrityGate.passed) integrityGate.errorMessage = "Data integrity issues detected";
        report.gates.add(integrityGate);
        
        // Gate 6: Performance
        QualityGate performanceGate = new QualityGate("Performance", 2);
        int timeouts = countOccurrences(logContent, "timeout|slow query|performance");
        performanceGate.passed = timeouts < 10;
        if (!performanceGate.passed) performanceGate.errorMessage = "Performance degradation detected";
        report.gates.add(performanceGate);
        
        // Gate 7: Consistency
        QualityGate consistencyGate = new QualityGate("Log Consistency", 3);
        String[] lines = logContent.split("\n");
        int malformedLines = 0;
        for (String line : lines) {
            if (line.length() > 5000) malformedLines++;
        }
        consistencyGate.passed = malformedLines < lines.length * 0.01;
        if (!consistencyGate.passed) consistencyGate.errorMessage = "Malformed log entries detected";
        report.gates.add(consistencyGate);
        
        // Calculate report statistics
        for (QualityGate gate : report.gates) {
            if (gate.passed) {
                report.passedCount++;
            } else {
                report.failedCount++;
            }
            report.severityCount.merge("Severity " + gate.severity, 1, Integer::sum);
        }
        
        report.allPassed = report.failedCount == 0;
        report.overallGrade = calculateGrade(report);
        
        return report;
    }
    
    private static int countOccurrences(@NotNull String content, @NotNull String pattern) {
        try {
            return (int) java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(content)
                    .results()
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static String calculateGrade(@NotNull QualityReport report) {
        double passRate = (double) report.passedCount / (report.passedCount + report.failedCount);
        
        if (passRate >= 0.95) return "A+ (Excellent)";
        if (passRate >= 0.85) return "A (Very Good)";
        if (passRate >= 0.75) return "B (Good)";
        if (passRate >= 0.65) return "C (Fair)";
        if (passRate >= 0.50) return "D (Poor)";
        return "F (Critical)";
    }
    
    public static String generateQualityReport(@NotNull QualityReport report) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║                  QUALITY GATE REPORT                        ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        sb.append(String.format("Overall Grade: %s\n", report.overallGrade));
        sb.append(String.format("Passed: %d / %d gates\n\n", report.passedCount, report.passedCount + report.failedCount));
        
        if (report.allPassed) {
            sb.append("✅ All quality gates passed!\n\n");
        } else {
            sb.append("❌ Quality gate failures detected:\n");
            for (QualityGate gate : report.gates) {
                if (!gate.passed) {
                    sb.append(String.format("  • [Severity %d] %s: %s\n", gate.severity, gate.name, gate.errorMessage));
                }
            }
            sb.append("\n");
        }
        
        sb.append("Gate Details:\n");
        for (QualityGate gate : report.gates) {
            String status = gate.passed ? "✅" : "❌";
            sb.append(String.format("%s %s\n", status, gate.name));
        }
        
        return sb.toString();
    }
}

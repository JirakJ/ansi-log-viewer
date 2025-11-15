package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class MasterLogAnalyzer {
    
    public static class ComprehensiveAnalysis {
        public AIContextAnalyzer.AIContext aiContext;
        public LogTimelineVisualizer.Timeline timeline;
        public AnomalyDetector.AnomalyReport anomalyReport;
        public PredictiveAlertEngine.AlertMetrics alertMetrics;
        public LogClusteringEngine.ClusteringResult clustering;
        public LogRecommendationEngine.RecommendationPack recommendations;
        public LogCorrelationAnalyzer.CorrelationResult correlations;
        public PerformanceStats performanceStats;
        public ExecutiveSummary summary;
        
        public ComprehensiveAnalysis() {
            this.performanceStats = new PerformanceStats();
            this.summary = new ExecutiveSummary();
        }
    }
    
    public static class PerformanceStats {
        public long analysisTimeMs;
        public long memoryUsedMb;
        public int componentsRun;
    }
    
    public static class ExecutiveSummary {
        public String overallStatus;
        public String criticalIssues;
        public List<String> topRecommendations;
        public String nextSteps;
        
        public ExecutiveSummary() {
            this.topRecommendations = new ArrayList<>();
        }
    }
    
    public static ComprehensiveAnalysis analyzeComplete(@NotNull String logContent) {
        long startTime = System.currentTimeMillis();
        ComprehensiveAnalysis analysis = new ComprehensiveAnalysis();
        
        try (LogPerformanceProfiler.ProfileTimer timer = LogPerformanceProfiler.measure("Complete Analysis")) {
            
            // 1. AI Context Analysis
            analysis.aiContext = AIContextAnalyzer.analyzeForAI(logContent);
            
            // 2. Timeline Analysis
            analysis.timeline = LogTimelineVisualizer.analyzeTimeline(logContent);
            
            // 3. Anomaly Detection
            analysis.anomalyReport = AnomalyDetector.detectAnomalies(logContent);
            
            // 4. Predictive Alerts
            PredictiveAlertEngine alertEngine = new PredictiveAlertEngine();
            List<PredictiveAlertEngine.PredictedAlert> alerts = alertEngine.predictAlerts(logContent);
            analysis.alertMetrics = alertEngine.getMetrics();
            
            // 5. Log Clustering
            int clusterCount = Math.max(3, (int) Math.sqrt(logContent.split("\n").length / 100));
            analysis.clustering = LogClusteringEngine.clusterLogs(logContent, clusterCount);
            
            // 6. Recommendations
            analysis.recommendations = LogRecommendationEngine.analyzeAndRecommend(logContent);
            
            // 7. Correlation Analysis
            analysis.correlations = LogCorrelationAnalyzer.analyzeCorrelations(logContent);
            
            // Calculate performance stats
            analysis.performanceStats.analysisTimeMs = System.currentTimeMillis() - startTime;
            analysis.performanceStats.memoryUsedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            analysis.performanceStats.componentsRun = 7;
            
            // Generate executive summary
            generateExecutiveSummary(analysis);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return analysis;
    }
    
    private static void generateExecutiveSummary(@NotNull ComprehensiveAnalysis analysis) {
        ExecutiveSummary summary = analysis.summary;
        
        // Determine overall status
        if (analysis.anomalyReport.anomalyRate > 0.15 || analysis.alertMetrics.criticalAlerts > 0) {
            summary.overallStatus = "🔴 CRITICAL - Immediate attention required";
        } else if (analysis.anomalyReport.anomalyRate > 0.05 || analysis.alertMetrics.totalAlerts > 3) {
            summary.overallStatus = "🟠 WARNING - Review recommended";
        } else if (analysis.recommendations.totalIssues > 0) {
            summary.overallStatus = "🟡 CAUTION - Monitor closely";
        } else {
            summary.overallStatus = "🟢 HEALTHY - Operating normally";
        }
        
        // Critical issues
        List<String> criticalList = new ArrayList<>();
        if (analysis.alertMetrics.criticalAlerts > 0) {
            criticalList.add(analysis.alertMetrics.criticalAlerts + " critical alerts");
        }
        if (analysis.anomalyReport.anomalyRate > 0.15) {
            criticalList.add("High anomaly rate (" + String.format("%.1f%%", analysis.anomalyReport.anomalyRate * 100) + ")");
        }
        if (!analysis.aiContext.suspiciousPatterns.isEmpty()) {
            criticalList.addAll(analysis.aiContext.suspiciousPatterns.stream().limit(2).toList());
        }
        summary.criticalIssues = criticalList.isEmpty() ? "None detected" : String.join(", ", criticalList);
        
        // Top recommendations
        analysis.recommendations.recommendations.stream()
                .sorted(Comparator.comparingInt(r -> r.priority))
                .limit(3)
                .forEach(rec -> summary.topRecommendations.add(rec.title));
        
        // Next steps
        if (summary.topRecommendations.isEmpty()) {
            summary.nextSteps = "Monitor application performance and maintain current practices";
        } else {
            summary.nextSteps = "1. Investigate critical issues\n2. Apply recommended optimizations\n3. Re-run analysis after fixes";
        }
    }
    
    public static String generateComprehensiveReport(@NotNull ComprehensiveAnalysis analysis) {
        StringBuilder report = new StringBuilder();
        
        report.append("╔════════════════════════════════════════════════════════════╗\n");
        report.append("║         COMPREHENSIVE LOG ANALYSIS REPORT v2.0              ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        // Executive Summary
        report.append("═ EXECUTIVE SUMMARY ═════════════════════════════════════════\n");
        report.append(String.format("Status: %s\n", analysis.summary.overallStatus));
        report.append(String.format("Critical Issues: %s\n", analysis.summary.criticalIssues));
        report.append(String.format("Health Score: %s\n\n", analysis.recommendations.overallHealthScore));
        
        // Key Findings
        report.append("═ KEY FINDINGS ══════════════════════════════════════════════\n");
        report.append(String.format("Application Domain: %s\n", analysis.aiContext.applicationDomain));
        report.append(String.format("Detected Technologies: %s\n", String.join(", ", analysis.aiContext.detectedTechnologies)));
        report.append(String.format("Timeline: %s\n", formatDuration(analysis.timeline.totalDuration)));
        report.append(String.format("Anomaly Rate: %.2f%%\n", analysis.anomalyReport.anomalyRate * 100));
        report.append(String.format("Error Rate: %.2f%%\n\n", analysis.aiContext.errorRate * 100));
        
        // Metrics
        report.append("═ METRICS ═══════════════════════════════════════════════════\n");
        report.append(String.format("Total Anomalies: %d\n", analysis.anomalyReport.anomalies.size()));
        report.append(String.format("Predicted Alerts: %d (Critical: %d)\n", 
            analysis.alertMetrics.totalAlerts, analysis.alertMetrics.criticalAlerts));
        report.append(String.format("Log Clusters: %d\n", analysis.clustering.clusters.size()));
        report.append(String.format("Clustering Quality: %.3f\n", analysis.clustering.silhouetteScore));
        report.append(String.format("Recommended Actions: %d\n\n", analysis.recommendations.totalIssues));
        
        // Top Recommendations
        report.append("═ TOP RECOMMENDATIONS ═══════════════════════════════════════\n");
        for (int i = 0; i < analysis.summary.topRecommendations.size(); i++) {
            report.append(String.format("%d. %s\n", i + 1, analysis.summary.topRecommendations.get(i)));
        }
        report.append("\n");
        
        // Performance
        report.append("═ ANALYSIS PERFORMANCE ══════════════════════════════════════\n");
        report.append(String.format("Analysis Time: %d ms\n", analysis.performanceStats.analysisTimeMs));
        report.append(String.format("Memory Used: %d MB\n", analysis.performanceStats.memoryUsedMb));
        report.append(String.format("Components Run: %d\n\n", analysis.performanceStats.componentsRun));
        
        // Next Steps
        report.append("═ NEXT STEPS ════════════════════════════════════════════════\n");
        report.append(analysis.summary.nextSteps).append("\n\n");
        
        report.append("═ DETAILED ANALYSIS ═════════════════════════════════════════\n");
        report.append("Use specialized tools for deeper investigation of specific areas.\n");
        
        return report.toString();
    }
    
    private static String formatDuration(java.time.Duration duration) {
        if (duration == null) return "N/A";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class LogRecommendationEngine {
    
    public static class Recommendation {
        public String title;
        public String description;
        public String category;
        public int priority;
        public String action;
        public List<String> references;
        
        public Recommendation(String title, String category, int priority) {
            this.title = title;
            this.category = category;
            this.priority = priority;
            this.references = new ArrayList<>();
        }
    }
    
    public static class RecommendationPack {
        public List<Recommendation> recommendations;
        public Map<String, Integer> categoryCount;
        public int totalIssues;
        public String overallHealthScore;
        
        public RecommendationPack() {
            this.recommendations = new ArrayList<>();
            this.categoryCount = new LinkedHashMap<>();
        }
    }
    
    public static RecommendationPack analyzeAndRecommend(@NotNull String logContent) {
        RecommendationPack pack = new RecommendationPack();
        
        // Analyze log content
        AnalysisContext context = analyzeContext(logContent);
        
        // Generate recommendations
        if (context.errorRate > 0.2) {
            pack.recommendations.add(createRecommendation(
                "Reduce Error Rate",
                "High error rate detected (" + String.format("%.1f%%", context.errorRate * 100) + "). " +
                "Review error logs and implement error handling improvements.",
                "Performance",
                1,
                "Review and fix error-prone code paths"
            ));
        }
        
        if (context.hasMemoryIssues) {
            pack.recommendations.add(createRecommendation(
                "Fix Memory Issues",
                "OutOfMemoryError or StackOverflowError detected. " +
                "Implement memory profiling and optimize heap usage.",
                "Memory",
                1,
                "Run heap dumps and identify memory leaks"
            ));
        }
        
        if (context.hasConnectivityIssues) {
            pack.recommendations.add(createRecommendation(
                "Improve Connectivity",
                "Connection failures detected. Implement retry logic and circuit breakers.",
                "Reliability",
                2,
                "Add resilience patterns (retry, circuit breaker, timeout)"
            ));
        }
        
        if (context.hasCacheMissPatterns) {
            pack.recommendations.add(createRecommendation(
                "Optimize Caching",
                "High cache miss rate detected. Improve cache strategy.",
                "Performance",
                2,
                "Increase cache TTL or implement distributed caching"
            ));
        }
        
        if (context.hasSlowQueries) {
            pack.recommendations.add(createRecommendation(
                "Optimize Database",
                "Slow queries detected. Add indexes or optimize query logic.",
                "Database",
                2,
                "Run query execution plans and add appropriate indexes"
            ));
        }
        
        if (context.hasSecurityIssues) {
            pack.recommendations.add(createRecommendation(
                "Address Security Concerns",
                "Authentication or authorization failures detected.",
                "Security",
                1,
                "Review access control and implement proper authentication"
            ));
        }
        
        if (context.logVerbosity > 100000) {
            pack.recommendations.add(createRecommendation(
                "Reduce Log Verbosity",
                "Excessive logging detected. This impacts performance.",
                "Performance",
                3,
                "Reduce debug logging and implement log levels"
            ));
        }
        
        if (context.errorRate < 0.01 && context.recommendationCount < 3) {
            pack.recommendations.add(createRecommendation(
                "Monitor Performance",
                "Application appears to be running smoothly. " +
                "Continue monitoring and maintain current practices.",
                "Health",
                5,
                "Set up dashboards for key metrics"
            ));
        }
        
        // Calculate statistics
        for (Recommendation rec : pack.recommendations) {
            pack.categoryCount.merge(rec.category, 1, Integer::sum);
        }
        pack.totalIssues = pack.recommendations.size();
        pack.overallHealthScore = calculateHealthScore(context);
        
        return pack;
    }
    
    private static Recommendation createRecommendation(@NotNull String title, @NotNull String description, 
                                                       @NotNull String category, int priority, @NotNull String action) {
        Recommendation rec = new Recommendation(title, category, priority);
        rec.description = description;
        rec.action = action;
        return rec;
    }
    
    private static class AnalysisContext {
        double errorRate;
        boolean hasMemoryIssues;
        boolean hasConnectivityIssues;
        boolean hasCacheMissPatterns;
        boolean hasSlowQueries;
        boolean hasSecurityIssues;
        long logVerbosity;
        int recommendationCount;
    }
    
    private static AnalysisContext analyzeContext(@NotNull String logContent) {
        AnalysisContext context = new AnalysisContext();
        String[] lines = logContent.split("\n");
        
        int errorCount = 0;
        for (String line : lines) {
            if (line.contains("ERROR") || line.contains("FATAL")) errorCount++;
            if (line.contains("OutOfMemory") || line.contains("StackOverflow")) context.hasMemoryIssues = true;
            if (line.contains("Connection refused") || line.contains("Connection timeout")) context.hasConnectivityIssues = true;
            if (line.contains("Cache miss") || line.contains("cache miss")) context.hasCacheMissPatterns = true;
            if (line.contains("slow query") || line.contains("query timeout")) context.hasSlowQueries = true;
            if (line.contains("Unauthorized") || line.contains("Forbidden") || line.contains("PermissionDenied")) context.hasSecurityIssues = true;
        }
        
        context.errorRate = (double) errorCount / Math.max(lines.length, 1);
        context.logVerbosity = logContent.length();
        context.recommendationCount = 0;
        if (context.hasMemoryIssues) context.recommendationCount++;
        if (context.hasConnectivityIssues) context.recommendationCount++;
        if (context.hasCacheMissPatterns) context.recommendationCount++;
        if (context.hasSlowQueries) context.recommendationCount++;
        if (context.hasSecurityIssues) context.recommendationCount++;
        
        return context;
    }
    
    private static String calculateHealthScore(@NotNull AnalysisContext context) {
        double score = 100;
        score -= context.errorRate * 50;
        if (context.hasMemoryIssues) score -= 20;
        if (context.hasConnectivityIssues) score -= 15;
        if (context.hasCacheMissPatterns) score -= 10;
        if (context.hasSlowQueries) score -= 10;
        if (context.hasSecurityIssues) score -= 25;
        
        score = Math.max(0, Math.min(100, score));
        
        if (score >= 80) return "🟢 Excellent";
        if (score >= 60) return "🟡 Good";
        if (score >= 40) return "🟠 Fair";
        return "🔴 Poor";
    }
    
    public static String generateRecommendationReport(@NotNull RecommendationPack pack) {
        StringBuilder report = new StringBuilder();
        
        report.append("╔════════════════════════════════════════════════════════════╗\n");
        report.append("║          INTELLIGENT RECOMMENDATION REPORT                  ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Overall Health: %s\n", pack.overallHealthScore));
        report.append(String.format("Total Issues Found: %d\n\n", pack.totalIssues));
        
        report.append("Issues by Category:\n");
        pack.categoryCount.forEach((category, count) -> {
            report.append(String.format("  %s: %d\n", category, count));
        });
        
        report.append("\nRecommendations (by priority):\n");
        pack.recommendations.stream()
                .sorted(Comparator.comparingInt(r -> r.priority))
                .forEach(rec -> {
                    String priority = "[P" + rec.priority + "]";
                    report.append(String.format("%s %s\n", priority, rec.title));
                    report.append(String.format("  Category: %s\n", rec.category));
                    report.append(String.format("  Description: %s\n", rec.description));
                    report.append(String.format("  Action: %s\n\n", rec.action));
                });
        
        return report.toString();
    }
}

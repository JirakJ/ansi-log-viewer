package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AIContextAnalyzer {
    
    public static class AIContext {
        public String applicationDomain;
        public List<String> detectedTechnologies;
        public String problemSummary;
        public List<String> suggestedQueries;
        public Map<String, Integer> keywordFrequency;
        public List<String> suspiciousPatterns;
        public double errorRate;
        public String recommendedAction;
        
        public AIContext() {
            this.detectedTechnologies = new ArrayList<>();
            this.suggestedQueries = new ArrayList<>();
            this.keywordFrequency = new LinkedHashMap<>();
            this.suspiciousPatterns = new ArrayList<>();
        }
    }
    
    private static final Map<String, String> TECH_SIGNATURES = new LinkedHashMap<>();
    
    static {
        TECH_SIGNATURES.put("SQLException", "Database");
        TECH_SIGNATURES.put("NullPointerException", "Java");
        TECH_SIGNATURES.put("RuntimeException", "Java");
        TECH_SIGNATURES.put("ConnectionError", "Network");
        TECH_SIGNATURES.put("TimeoutException", "Network");
        TECH_SIGNATURES.put("OutOfMemoryError", "Memory");
        TECH_SIGNATURES.put("StackOverflowError", "Memory");
        TECH_SIGNATURES.put("FileNotFoundException", "FileSystem");
        TECH_SIGNATURES.put("PermissionError", "Security");
        TECH_SIGNATURES.put("AuthenticationError", "Security");
        TECH_SIGNATURES.put("nginx", "Web Server");
        TECH_SIGNATURES.put("docker", "Containerization");
        TECH_SIGNATURES.put("kubernetes", "Orchestration");
        TECH_SIGNATURES.put("elasticsearch", "Search Engine");
        TECH_SIGNATURES.put("redis", "Cache");
        TECH_SIGNATURES.put("postgresql", "Database");
        TECH_SIGNATURES.put("mongodb", "Database");
        TECH_SIGNATURES.put("kafka", "Message Queue");
        TECH_SIGNATURES.put("spring", "Framework");
        TECH_SIGNATURES.put("hibernate", "ORM");
    }
    
    public static AIContext analyzeForAI(@NotNull String logContent) {
        AIContext context = new AIContext();
        
        String[] lines = logContent.split("\n");
        int errorCount = 0;
        
        for (String line : lines) {
            if (line.contains("ERROR") || line.contains("FATAL")) errorCount++;
        }
        
        context.errorRate = (double) errorCount / Math.max(lines.length, 1);
        
        // Detect technologies
        detectTechnologies(logContent, context);
        
        // Analyze domain
        context.applicationDomain = inferApplicationDomain(logContent);
        
        // Extract keywords
        extractKeywords(logContent, context);
        
        // Find suspicious patterns
        findSuspiciousPatterns(logContent, context);
        
        // Generate problem summary
        context.problemSummary = generateProblemSummary(context);
        
        // Suggest queries for AI
        context.suggestedQueries = generateSuggestedQueries(context);
        
        // Recommend action
        context.recommendedAction = generateRecommendation(context);
        
        return context;
    }
    
    private static void detectTechnologies(@NotNull String content, @NotNull AIContext context) {
        Set<String> foundTechs = new HashSet<>();
        
        TECH_SIGNATURES.forEach((keyword, tech) -> {
            if (content.toLowerCase().contains(keyword.toLowerCase())) {
                foundTechs.add(tech);
            }
        });
        
        context.detectedTechnologies.addAll(foundTechs);
    }
    
    private static String inferApplicationDomain(@NotNull String content) {
        if (content.contains("transaction") || content.contains("account")) return "Finance";
        if (content.contains("user") && content.contains("auth")) return "Authentication";
        if (content.contains("api") || content.contains("request")) return "API Service";
        if (content.contains("database") || content.contains("query")) return "Data Layer";
        if (content.contains("cache") || content.contains("redis")) return "Caching Layer";
        if (content.contains("message") || content.contains("queue")) return "Message Queue";
        if (content.contains("service") && content.contains("started")) return "Microservice";
        return "General Application";
    }
    
    private static void extractKeywords(@NotNull String content, @NotNull AIContext context) {
        String[] keywords = {
            "error", "exception", "failed", "timeout", "connection", "rejected",
            "unauthorized", "forbidden", "not found", "invalid", "critical",
            "warning", "retry", "fallback", "degraded", "failure"
        };
        
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            long count = pattern.matcher(content).results().count();
            if (count > 0) {
                context.keywordFrequency.put(keyword, (int) count);
            }
        }
    }
    
    private static void findSuspiciousPatterns(@NotNull String content, @NotNull AIContext context) {
        if (content.matches(".*OutOfMemoryError.*")) {
            context.suspiciousPatterns.add("Memory leak detected");
        }
        if (content.matches(".*Connection refused.*")) {
            context.suspiciousPatterns.add("Service unreachable");
        }
        if (content.matches(".*Timeout.*")) {
            context.suspiciousPatterns.add("Performance degradation");
        }
        if (content.matches(".*PermissionError.*")) {
            context.suspiciousPatterns.add("Security/Authorization issue");
        }
        if (content.matches(".*retry.*")) {
            context.suspiciousPatterns.add("Transient failures - retries active");
        }
    }
    
    private static String generateProblemSummary(@NotNull AIContext context) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("Domain: %s. ", context.applicationDomain));
        summary.append(String.format("Error rate: %.1f%%. ", context.errorRate * 100));
        summary.append(String.format("Technologies: %s. ", 
                String.join(", ", context.detectedTechnologies)));
        
        if (!context.suspiciousPatterns.isEmpty()) {
            summary.append(String.format("Issues: %s.", 
                    String.join(", ", context.suspiciousPatterns)));
        }
        
        return summary.toString();
    }
    
    private static List<String> generateSuggestedQueries(@NotNull AIContext context) {
        List<String> queries = new ArrayList<>();
        
        if (context.errorRate > 0.1) {
            queries.add("Why are there so many errors in this log?");
        }
        if (context.suspiciousPatterns.contains("Memory leak detected")) {
            queries.add("How do I debug a memory leak in " + context.applicationDomain + "?");
        }
        if (context.suspiciousPatterns.contains("Service unreachable")) {
            queries.add("How to troubleshoot connection refused errors?");
        }
        
        queries.add("Analyze this " + context.applicationDomain + " error pattern");
        queries.add("What are the root causes for these failures?");
        queries.add("Suggest optimization strategies for this " + context.applicationDomain);
        
        return queries;
    }
    
    private static String generateRecommendation(@NotNull AIContext context) {
        if (context.errorRate > 0.3) {
            return "CRITICAL: High error rate detected. Immediate investigation required.";
        }
        if (context.errorRate > 0.1) {
            return "WARNING: Elevated error rate. Review error patterns and metrics.";
        }
        if (!context.suspiciousPatterns.isEmpty()) {
            return "INFO: Suspicious patterns detected. Recommend applying AI analysis.";
        }
        return "OK: Log analysis suggests normal operation. Monitor for changes.";
    }
}

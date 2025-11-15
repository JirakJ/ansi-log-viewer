package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogPerformanceProfiler {
    
    public static class PerformanceMetric {
        public String name;
        public long minTimeMs;
        public long maxTimeMs;
        public long totalTimeMs;
        public int callCount;
        public double avgTimeMs;
        public long memoryUsed;
        
        public PerformanceMetric(String name) {
            this.name = name;
            this.minTimeMs = Long.MAX_VALUE;
            this.maxTimeMs = 0;
            this.totalTimeMs = 0;
            this.callCount = 0;
            this.memoryUsed = 0;
        }
        
        public void update(long timeMs, long memory) {
            this.minTimeMs = Math.min(this.minTimeMs, timeMs);
            this.maxTimeMs = Math.max(this.maxTimeMs, timeMs);
            this.totalTimeMs += timeMs;
            this.callCount++;
            this.memoryUsed += memory;
            this.avgTimeMs = (double) this.totalTimeMs / this.callCount;
        }
    }
    
    public static class ProfileResult {
        public Map<String, PerformanceMetric> metrics;
        public long totalTimeMs;
        public long totalMemoryMb;
        public double avgCpuLoad;
        
        public ProfileResult() {
            this.metrics = new LinkedHashMap<>();
        }
    }
    
    private static final Map<String, PerformanceMetric> metrics = new ConcurrentHashMap<>();
    private static final long startTime = System.currentTimeMillis();
    private static final Runtime runtime = Runtime.getRuntime();
    
    public static class ProfileTimer implements AutoCloseable {
        private String name;
        private long startTime;
        private long startMemory;
        
        public ProfileTimer(@NotNull String name) {
            this.name = name;
            this.startTime = System.currentTimeMillis();
            this.startMemory = runtime.totalMemory() - runtime.freeMemory();
        }
        
        @Override
        public void close() {
            long endTime = System.currentTimeMillis();
            long endMemory = runtime.totalMemory() - runtime.freeMemory();
            long duration = endTime - startTime;
            long memoryDiff = endMemory - startMemory;
            
            metrics.computeIfAbsent(name, PerformanceMetric::new)
                    .update(duration, memoryDiff);
        }
    }
    
    public static ProfileTimer measure(@NotNull String name) {
        return new ProfileTimer(name);
    }
    
    public static void recordMetric(@NotNull String name, long timeMs) {
        metrics.computeIfAbsent(name, PerformanceMetric::new)
                .update(timeMs, 0);
    }
    
    public static void recordMetric(@NotNull String name, long timeMs, long memory) {
        metrics.computeIfAbsent(name, PerformanceMetric::new)
                .update(timeMs, memory);
    }
    
    public static ProfileResult getResults() {
        ProfileResult result = new ProfileResult();
        result.metrics.putAll(metrics);
        result.totalTimeMs = System.currentTimeMillis() - startTime;
        result.totalMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        try {
            result.avgCpuLoad = com.sun.management.OperatingSystemMXBean.class
                    .cast(java.lang.management.ManagementFactory.getOperatingSystemMXBean())
                    .getProcessCpuLoad();
        } catch (Exception e) {
            result.avgCpuLoad = 0.0;
        }
        return result;
    }
    
    public static String generateReport() {
        ProfileResult result = getResults();
        StringBuilder report = new StringBuilder();
        
        report.append("╔════════════════════════════════════════════════════════════╗\n");
        report.append("║              PERFORMANCE PROFILING REPORT                   ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Total Runtime: %d ms\n", result.totalTimeMs));
        report.append(String.format("Total Memory: %d MB\n", result.totalMemoryMb));
        report.append(String.format("CPU Load: %.2f%%\n\n", result.avgCpuLoad * 100));
        
        report.append("┌─ Metrics ─────────────────────────────────────────────────┐\n");
        result.metrics.forEach((name, metric) -> {
            report.append(String.format("| %s\n", name));
            report.append(String.format("|   Calls: %d, Avg: %.2f ms, Min: %d ms, Max: %d ms\n",
                    metric.callCount, metric.avgTimeMs, metric.minTimeMs, metric.maxTimeMs));
        });
        report.append("└──────────────────────────────────────────────────────────┘\n");
        
        return report.toString();
    }
    
    public static void reset() {
        metrics.clear();
    }
    
    public static Map<String, PerformanceMetric> getMetrics() {
        return new HashMap<>(metrics);
    }
}

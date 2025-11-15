package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class RealtimeLogMonitor {
    
    public static class MonitorConfig {
        public Path filePath;
        public long pollIntervalMs;
        public int maxBufferSize;
        public boolean followTail;
        public String encoding;
        public List<String> highlightPatterns;
        public List<String> alertPatterns;
        
        public MonitorConfig(Path filePath) {
            this.filePath = filePath;
            this.pollIntervalMs = 500;
            this.maxBufferSize = 10000;
            this.followTail = true;
            this.encoding = "UTF-8";
            this.highlightPatterns = new ArrayList<>();
            this.alertPatterns = new ArrayList<>();
        }
    }
    
    public static class LogEntry {
        public long timestamp;
        public String content;
        public boolean isAlert;
        public String severity;
        
        public LogEntry(String content, boolean isAlert, String severity) {
            this.timestamp = System.currentTimeMillis();
            this.content = content;
            this.isAlert = isAlert;
            this.severity = severity;
        }
    }
    
    public static class MonitorStats {
        public long startTime;
        public long endTime;
        public int totalLines;
        public int alertCount;
        public Map<String, Integer> severityCount;
        public long bytesRead;
        
        public MonitorStats() {
            this.severityCount = new HashMap<>();
        }
    }
    
    private MonitorConfig config;
    private ScheduledExecutorService executor;
    private Consumer<LogEntry> callback;
    private long lastPosition;
    private RandomAccessFile raf;
    private Queue<LogEntry> buffer;
    private MonitorStats stats;
    private volatile boolean running;
    
    public RealtimeLogMonitor(@NotNull MonitorConfig config) {
        this.config = config;
        this.executor = Executors.newScheduledThreadPool(2);
        this.buffer = new ConcurrentLinkedQueue<>();
        this.stats = new MonitorStats();
        this.running = false;
    }
    
    public void start(@NotNull Consumer<LogEntry> callback) throws IOException {
        this.callback = callback;
        this.running = true;
        this.stats.startTime = System.currentTimeMillis();
        this.raf = new RandomAccessFile(config.filePath.toFile(), "r");
        
        if (config.followTail) {
            this.lastPosition = raf.length();
        } else {
            this.lastPosition = 0;
        }
        
        executor.scheduleAtFixedRate(this::checkForNewLines, 0, config.pollIntervalMs, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::processBuffer, 100, config.pollIntervalMs / 2, TimeUnit.MILLISECONDS);
    }
    
    public void stop() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (raf != null) {
                raf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stats.endTime = System.currentTimeMillis();
    }
    
    private void checkForNewLines() {
        try {
            long currentSize = raf.length();
            if (currentSize > lastPosition) {
                raf.seek(lastPosition);
                String line;
                while ((line = raf.readLine()) != null) {
                    String decodedLine = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    boolean isAlert = checkAlertPatterns(decodedLine);
                    String severity = detectSeverity(decodedLine);
                    
                    LogEntry entry = new LogEntry(decodedLine, isAlert, severity);
                    buffer.offer(entry);
                    stats.totalLines++;
                    stats.bytesRead += line.length();
                    
                    if (isAlert) {
                        stats.alertCount++;
                    }
                    stats.severityCount.merge(severity, 1, Integer::sum);
                }
                lastPosition = raf.getFilePointer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processBuffer() {
        int processed = 0;
        while (!buffer.isEmpty() && processed < 100) {
            LogEntry entry = buffer.poll();
            if (entry != null && callback != null) {
                callback.accept(entry);
                processed++;
            }
        }
    }
    
    private boolean checkAlertPatterns(@NotNull String line) {
        for (String pattern : config.alertPatterns) {
            if (line.matches(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    private String detectSeverity(@NotNull String line) {
        if (line.contains("ERROR") || line.contains("FATAL")) return "ERROR";
        if (line.contains("WARN")) return "WARN";
        if (line.contains("INFO")) return "INFO";
        if (line.contains("DEBUG")) return "DEBUG";
        if (line.contains("TRACE")) return "TRACE";
        return "UNKNOWN";
    }
    
    public MonitorStats getStats() {
        return stats;
    }
    
    public Queue<LogEntry> getBuffer() {
        return buffer;
    }
    
    public boolean isRunning() {
        return running;
    }
}

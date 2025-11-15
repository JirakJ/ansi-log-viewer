package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogTemplateGenerator {
    
    public enum LogLevel {
        TRACE("\u001B[36mTRACE\u001B[0m"),
        DEBUG("\u001B[34mDEBUG\u001B[0m"),
        INFO("\u001B[32mINFO\u001B[0m"),
        WARN("\u001B[33mWARN\u001B[0m"),
        ERROR("\u001B[31mERROR\u001B[0m"),
        FATAL("\u001B[1;31mFATAL\u001B[0m");
        
        private final String ansiCode;
        
        LogLevel(String ansiCode) {
            this.ansiCode = ansiCode;
        }
        
        public String getAnsiCode() {
            return ansiCode;
        }
    }
    
    public static class Template {
        public String name;
        public String pattern;
        public List<String> exampleLines;
        
        public Template(String name, String pattern) {
            this.name = name;
            this.pattern = pattern;
            this.exampleLines = new ArrayList<>();
        }
    }
    
    private static final List<Template> PREDEFINED_TEMPLATES = new ArrayList<>();
    
    static {
        Template java = new Template("Java Application", 
            "[%timestamp%] %level% [%thread%] %logger% - %message%");
        java.exampleLines.add("[2024-11-13 10:57:39] INFO [main] com.example.App - Application started");
        PREDEFINED_TEMPLATES.add(java);
        
        Template spring = new Template("Spring Boot",
            "%timestamp% %level% %logger% : %message%");
        spring.exampleLines.add("2024-11-13 10:57:39.123 INFO  org.springframework.boot.Application : Started Application");
        PREDEFINED_TEMPLATES.add(spring);
        
        Template nginx = new Template("Nginx",
            "%timestamp% [%level%] %pid%#%tid%: %message% \"%address%\"");
        nginx.exampleLines.add("2024/11/13 10:57:39 [error] 1234#5678: Connection refused 127.0.0.1");
        PREDEFINED_TEMPLATES.add(nginx);
        
        Template docker = new Template("Docker",
            "%timestamp% %level% %image%: %message%");
        docker.exampleLines.add("2024-11-13T10:57:39.123Z INFO myimage: Container started successfully");
        PREDEFINED_TEMPLATES.add(docker);
        
        Template database = new Template("Database",
            "%timestamp% [%level%] [%query_id%] - %message% (duration: %duration%ms)");
        database.exampleLines.add("2024-11-13 10:57:39 [DEBUG] [q1234] - SELECT * FROM users (duration: 45ms)");
        PREDEFINED_TEMPLATES.add(database);
    }
    
    public static List<Template> getPredefinedTemplates() {
        return new ArrayList<>(PREDEFINED_TEMPLATES);
    }
    
    public static String generateLogLine(@NotNull String template, @NotNull LogLevel level, @NotNull String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return template
                .replace("%timestamp%", now.format(formatter))
                .replace("%level%", level.getAnsiCode())
                .replace("%message%", message)
                .replace("%thread%", Thread.currentThread().getName())
                .replace("%logger%", "com.example.Logger")
                .replace("%pid%", String.valueOf(ProcessHandle.current().pid()))
                .replace("%tid%", String.valueOf(Thread.currentThread().hashCode()))
                .replace("%image%", "app-container")
                .replace("%address%", "192.168.1.1")
                .replace("%query_id%", String.valueOf(UUID.randomUUID()))
                .replace("%duration%", String.valueOf(new Random().nextInt(1000)));
    }
    
    public static String generateTestLog(@NotNull String template, int lines) {
        StringBuilder log = new StringBuilder();
        String[] messages = {
            "Application started successfully",
            "Processing request from client",
            "Database connection established",
            "Cache miss - fetching data",
            "Warning: High memory usage detected",
            "Error: Connection timeout",
            "File not found: config.properties",
            "Successfully completed batch processing",
            "User authentication failed",
            "Scheduled task executed"
        };
        
        LogLevel[] levels = LogLevel.values();
        Random random = new Random();
        
        for (int i = 0; i < lines; i++) {
            LogLevel level = levels[random.nextInt(levels.length)];
            String message = messages[random.nextInt(messages.length)];
            log.append(generateLogLine(template, level, message)).append("\n");
        }
        
        return log.toString();
    }
    
    public static Template createCustomTemplate(@NotNull String name, @NotNull String pattern) {
        return new Template(name, pattern);
    }
    
    public static String analyzeLogFormat(@NotNull String sampleLog) {
        List<String> lines = Arrays.asList(sampleLog.split("\n"));
        if (lines.isEmpty()) return "Unknown format";
        
        String firstLine = lines.get(0);
        
        if (firstLine.matches(".*\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}.*")) {
            if (firstLine.contains("[") && firstLine.contains("]")) {
                return "Java/Generic Bracketed Format";
            }
            return "ISO 8601 Timestamp Format";
        }
        if (firstLine.contains("\\[error\\]") || firstLine.contains("\\[warn\\]")) {
            return "Nginx/Web Server Format";
        }
        if (firstLine.contains("T") && firstLine.contains("Z")) {
            return "Docker/UTC ISO 8601 Format";
        }
        
        return "Custom/Unknown Format";
    }
}

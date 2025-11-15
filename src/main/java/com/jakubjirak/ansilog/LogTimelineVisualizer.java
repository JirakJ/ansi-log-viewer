package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogTimelineVisualizer {
    
    public static class TimelineEvent {
        public LocalDateTime timestamp;
        public String level;
        public String message;
        public int lineNumber;
        
        public TimelineEvent(LocalDateTime timestamp, String level, String message, int lineNumber) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
            this.lineNumber = lineNumber;
        }
    }
    
    public static class Timeline {
        public List<TimelineEvent> events;
        public Duration totalDuration;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public Map<String, Integer> levelDistribution;
        
        public Timeline() {
            this.events = new ArrayList<>();
            this.levelDistribution = new LinkedHashMap<>();
        }
    }
    
    public static Timeline analyzeTimeline(@NotNull String content) {
        Timeline timeline = new Timeline();
        String[] lines = content.split("\n");
        
        Pattern timestampPattern = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})[\\s T](\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)"
        );
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = timestampPattern.matcher(line);
            
            if (matcher.find()) {
                try {
                    LocalDateTime timestamp = parseTimestamp(matcher.group(1), matcher.group(2));
                    String level = extractLevel(line);
                    String message = extractMessage(line);
                    
                    TimelineEvent event = new TimelineEvent(timestamp, level, message, i + 1);
                    timeline.events.add(event);
                    timeline.levelDistribution.merge(level, 1, Integer::sum);
                    
                    if (timeline.startTime == null || timestamp.isBefore(timeline.startTime)) {
                        timeline.startTime = timestamp;
                    }
                    if (timeline.endTime == null || timestamp.isAfter(timeline.endTime)) {
                        timeline.endTime = timestamp;
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }
        }
        
        if (timeline.startTime != null && timeline.endTime != null) {
            timeline.totalDuration = Duration.between(timeline.startTime, timeline.endTime);
        }
        
        return timeline;
    }
    
    public static String visualizeTimeline(@NotNull Timeline timeline) {
        StringBuilder visual = new StringBuilder();
        
        visual.append("╔════════════════════════════════════════════════════════════╗\n");
        visual.append("║                    LOG TIMELINE ANALYSIS                    ║\n");
        visual.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        if (timeline.startTime != null && timeline.endTime != null) {
            visual.append(String.format("Start: %s\n", timeline.startTime));
            visual.append(String.format("End:   %s\n", timeline.endTime));
            visual.append(String.format("Duration: %s\n\n", formatDuration(timeline.totalDuration)));
        }
        
        visual.append("Level Distribution:\n");
        timeline.levelDistribution.forEach((level, count) -> {
            int barLength = Math.min(50, count);
            String bar = "█".repeat(barLength);
            visual.append(String.format("  %-8s: %s %d\n", level, bar, count));
        });
        
        visual.append("\nTimeline:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        for (TimelineEvent event : timeline.events) {
            String levelIcon = getLevelIcon(event.level);
            visual.append(String.format("%s %s %s [Line %d] %s\n",
                    levelIcon,
                    event.timestamp.format(formatter),
                    event.level,
                    event.lineNumber,
                    truncate(event.message, 40)));
        }
        
        return visual.toString();
    }
    
    public static Map<LocalDateTime, Integer> getEventFrequency(@NotNull Timeline timeline, int bucketMinutes) {
        Map<LocalDateTime, Integer> frequency = new LinkedHashMap<>();
        
        for (TimelineEvent event : timeline.events) {
            LocalDateTime bucket = event.timestamp
                    .withSecond(0)
                    .withNano(0)
                    .minusMinutes(event.timestamp.getMinute() % bucketMinutes);
            
            frequency.merge(bucket, 1, Integer::sum);
        }
        
        return frequency;
    }
    
    public static List<Duration> getInterEventDurations(@NotNull Timeline timeline) {
        List<Duration> durations = new ArrayList<>();
        
        for (int i = 1; i < timeline.events.size(); i++) {
            Duration duration = Duration.between(
                    timeline.events.get(i - 1).timestamp,
                    timeline.events.get(i).timestamp
            );
            durations.add(duration);
        }
        
        return durations;
    }
    
    private static LocalDateTime parseTimestamp(String date, String time) {
        String dateTime = date + "T" + time;
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
    }
    
    private static String extractLevel(String line) {
        if (line.contains("ERROR") || line.contains("FATAL")) return "ERROR";
        if (line.contains("WARN")) return "WARN";
        if (line.contains("INFO")) return "INFO";
        if (line.contains("DEBUG")) return "DEBUG";
        if (line.contains("TRACE")) return "TRACE";
        return "OTHER";
    }
    
    private static String extractMessage(String line) {
        int dashIndex = line.indexOf("-");
        if (dashIndex != -1) {
            return line.substring(dashIndex + 1).trim();
        }
        return line.substring(Math.min(30, line.length()));
    }
    
    private static String getLevelIcon(String level) {
        return switch (level) {
            case "ERROR" -> "❌";
            case "WARN" -> "⚠️";
            case "INFO" -> "ℹ️";
            case "DEBUG" -> "🔍";
            case "TRACE" -> "📍";
            default -> "◆";
        };
    }
    
    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private static String truncate(String text, int length) {
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }
}

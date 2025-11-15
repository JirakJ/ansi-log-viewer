package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class BatchLogProcessor {
    
    public static class BatchJob {
        public String name;
        public Path inputDir;
        public Path outputDir;
        public String pattern;
        public boolean recursive;
        public List<String> operations;
        public int threads;
        
        public BatchJob(String name) {
            this.name = name;
            this.operations = new ArrayList<>();
            this.threads = Runtime.getRuntime().availableProcessors();
        }
    }
    
    public static class BatchResult {
        public int totalFiles;
        public int processedFiles;
        public int failedFiles;
        public long totalSize;
        public long processingTimeMs;
        public List<String> errors;
        
        public BatchResult() {
            this.errors = new ArrayList<>();
        }
    }
    
    public static BatchResult processBatch(@NotNull BatchJob job) throws IOException {
        BatchResult result = new BatchResult();
        long startTime = System.currentTimeMillis();
        
        List<Path> files = findLogFiles(job.inputDir, job.pattern, job.recursive);
        result.totalFiles = files.size();
        
        ExecutorService executor = Executors.newFixedThreadPool(job.threads);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        try {
            for (Path file : files) {
                futures.add(executor.submit(() -> {
                    try {
                        processFile(file, job, result);
                        return true;
                    } catch (Exception e) {
                        result.errors.add(file + ": " + e.getMessage());
                        result.failedFiles++;
                        return false;
                    }
                }));
            }
            
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    result.processedFiles++;
                }
            }
        } catch (Exception e) {
            result.errors.add("Batch processing error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
        
        result.processingTimeMs = System.currentTimeMillis() - startTime;
        return result;
    }
    
    private static void processFile(@NotNull Path file, @NotNull BatchJob job, @NotNull BatchResult result) throws IOException {
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        result.totalSize += file.toFile().length();
        
        for (String operation : job.operations) {
            content = applyOperation(content, operation);
        }
        
        Path outputPath = job.outputDir.resolve(file.getFileName());
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, content.getBytes(StandardCharsets.UTF_8));
    }
    
    private static String applyOperation(@NotNull String content, @NotNull String operation) {
        if (operation.equals("strip_ansi")) {
            return content.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "");
        } else if (operation.equals("normalize")) {
            return normalizeLogLines(content);
        } else if (operation.equals("deduplicate")) {
            return deduplicateLines(content);
        } else if (operation.equals("sort")) {
            return sortLogLines(content);
        }
        return content;
    }
    
    private static String normalizeLogLines(@NotNull String content) {
        StringBuilder sb = new StringBuilder();
        for (String line : content.split("\n")) {
            String normalized = line
                    .replaceAll("\\s+", " ")
                    .replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "")
                    .trim();
            if (!normalized.isEmpty()) {
                sb.append(normalized).append("\n");
            }
        }
        return sb.toString();
    }
    
    private static String deduplicateLines(@NotNull String content) {
        Set<String> seen = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();
        for (String line : content.split("\n")) {
            if (seen.add(line)) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
    
    private static String sortLogLines(@NotNull String content) {
        String[] lines = content.split("\n");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }
    
    private static List<Path> findLogFiles(@NotNull Path dir, @NotNull String pattern, boolean recursive) throws IOException {
        List<Path> files = new ArrayList<>();
        
        if (recursive) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "**/*" + pattern)) {
                stream.forEach(files::add);
            } catch (NotDirectoryException e) {
                // Handle single file case
                if (Files.isRegularFile(dir)) {
                    files.add(dir);
                }
            }
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*" + pattern)) {
                stream.forEach(files::add);
            }
        }
        
        return files;
    }
}

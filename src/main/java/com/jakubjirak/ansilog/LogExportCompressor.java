package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogExportCompressor {
    
    public static class ExportOptions {
        public boolean stripAnsi;
        public boolean compress;
        public String format;
        public boolean includeMetadata;
        
        public ExportOptions() {
            this.stripAnsi = true;
            this.compress = false;
            this.format = "txt";
            this.includeMetadata = true;
        }
    }
    
    public static boolean exportLog(@NotNull String content, @NotNull Path outputPath, @NotNull ExportOptions options) throws IOException {
        Files.createDirectories(outputPath.getParent());
        
        String processedContent = content;
        if (options.stripAnsi) {
            processedContent = stripAnsiCodes(content);
        }
        
        if (options.includeMetadata) {
            processedContent = prependMetadata(processedContent);
        }
        
        if (options.compress) {
            return compressToZip(processedContent, outputPath.toString() + ".zip", outputPath.getFileName().toString());
        } else {
            Files.write(outputPath, processedContent.getBytes(StandardCharsets.UTF_8));
            return true;
        }
    }
    
    public static boolean exportAsHtml(@NotNull String content, @NotNull Path outputPath) throws IOException {
        String htmlContent = buildHtmlContent(content);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, htmlContent.getBytes(StandardCharsets.UTF_8));
        return true;
    }
    
    public static boolean compressMultipleFiles(@NotNull List<Path> files, @NotNull Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Path file : files) {
                if (Files.isRegularFile(file)) {
                    ZipEntry entry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(entry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        }
        return true;
    }
    
    private static boolean compressToZip(@NotNull String content, @NotNull String zipPath, @NotNull String fileName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(zipPath)))) {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return true;
    }
    
    private static String stripAnsiCodes(@NotNull String content) {
        return content.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "");
    }
    
    private static String prependMetadata(@NotNull String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LOG METADATA ===\n");
        sb.append("Generated: ").append(new java.util.Date()).append("\n");
        sb.append("Lines: ").append(content.split("\n").length).append("\n");
        sb.append("Size: ").append(formatBytes(content.length())).append("\n");
        sb.append("================================\n\n");
        sb.append(content);
        return sb.toString();
    }
    
    private static String buildHtmlContent(@NotNull String content) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("  <meta charset='UTF-8'>\n");
        html.append("  <title>Log Export</title>\n");
        html.append("  <style>\n");
        html.append("    body { font-family: monospace; white-space: pre-wrap; background: #1e1e1e; color: #d4d4d4; }\n");
        html.append("    .error { color: #f48771; }\n");
        html.append("    .warn { color: #dcdcaa; }\n");
        html.append("    .info { color: #569cd6; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        for (String line : content.split("\n")) {
            String className = "";
            if (line.contains("ERROR")) className = "error";
            else if (line.contains("WARN")) className = "warn";
            else if (line.contains("INFO")) className = "info";
            
            if (!className.isEmpty()) {
                html.append(String.format("<div class='%s'>%s</div>\n", className, escapeHtml(line)));
            } else {
                html.append(escapeHtml(line)).append("\n");
            }
        }
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String escapeHtml(@NotNull String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
    
    private static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}

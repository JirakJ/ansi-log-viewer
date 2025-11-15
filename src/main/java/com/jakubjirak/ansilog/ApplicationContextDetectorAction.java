package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Pattern;

public class ApplicationContextDetectorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        StringBuilder context = new StringBuilder();
        
        context.append("DETECTED_APPLICATION_CONTEXT:\n\n");
        
        // Detect framework
        String framework = detectFramework(text);
        context.append("Framework: ").append(framework).append("\n");
        
        // Detect language
        String language = detectLanguage(text);
        context.append("Language: ").append(language).append("\n");
        
        // Detect components
        List<String> components = detectComponents(text);
        context.append("\nDetected Components:\n");
        components.forEach(c -> context.append("  • ").append(c).append("\n"));
        
        // Detect database references
        List<String> databases = detectDatabases(text);
        if (!databases.isEmpty()) {
            context.append("\nDatabase Systems:\n");
            databases.forEach(db -> context.append("  • ").append(db).append("\n"));
        }
        
        // Detect external services
        List<String> services = detectExternalServices(text);
        if (!services.isEmpty()) {
            context.append("\nExternal Services:\n");
            services.forEach(s -> context.append("  • ").append(s).append("\n"));
        }
        
        // Detect security-related logs
        int securityEvents = (int) text.lines()
                .filter(l -> l.matches(".*(?i)(auth|security|token|permission|forbidden|unauthorized).*"))
                .count();
        if (securityEvents > 0) {
            context.append("\nSecurity Events Detected: ").append(securityEvents).append("\n");
        }
        
        String result = context.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard for Copilot!",
                "Application Context"
        );
    }

    private String detectFramework(String text) {
        if (text.contains("Spring") || text.contains("org.springframework")) return "Spring Boot";
        if (text.contains("Hibernate") || text.contains("org.hibernate")) return "Hibernate";
        if (text.contains("Quarkus")) return "Quarkus";
        if (text.contains("Vert.x")) return "Vert.x";
        if (text.contains("Micronaut")) return "Micronaut";
        if (text.contains("Apache")) return "Apache";
        if (text.contains("Tomcat")) return "Tomcat";
        return "Unknown/Custom";
    }

    private String detectLanguage(String text) {
        if (text.contains("java.") || text.contains("Exception in thread")) return "Java";
        if (text.contains("python") || text.contains("Traceback")) return "Python";
        if (text.contains("node") || text.contains("Error:")) return "Node.js";
        if (text.contains(".NET") || text.contains("System.")) return ".NET";
        return "Unknown";
    }

    private List<String> detectComponents(String text) {
        List<String> components = new ArrayList<>();
        String[] keywords = {
            "Cache", "Queue", "Database", "API", "Gateway", 
            "Service", "Controller", "Repository", "Middleware",
            "Worker", "Scheduler", "Logger", "Metrics"
        };
        
        for (String keyword : keywords) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                components.add(keyword);
            }
        }
        
        return components.isEmpty() ? List.of("Generic Application") : components;
    }

    private List<String> detectDatabases(String text) {
        List<String> databases = new ArrayList<>();
        if (text.contains("mysql") || text.contains("MySQL")) databases.add("MySQL");
        if (text.contains("postgres") || text.contains("Postgres")) databases.add("PostgreSQL");
        if (text.contains("mongodb") || text.contains("MongoDB")) databases.add("MongoDB");
        if (text.contains("redis") || text.contains("Redis")) databases.add("Redis");
        if (text.contains("oracle") || text.contains("Oracle")) databases.add("Oracle");
        if (text.contains("sqlite") || text.contains("SQLite")) databases.add("SQLite");
        return databases;
    }

    private List<String> detectExternalServices(String text) {
        List<String> services = new ArrayList<>();
        if (text.contains("aws") || text.contains("AWS")) services.add("AWS");
        if (text.contains("docker") || text.contains("Docker")) services.add("Docker");
        if (text.contains("kubernetes") || text.contains("k8s")) services.add("Kubernetes");
        if (text.contains("kafka") || text.contains("Kafka")) services.add("Kafka");
        if (text.contains("elasticsearch") || text.contains("Elastic")) services.add("Elasticsearch");
        if (text.contains("prometheus") || text.contains("Prometheus")) services.add("Prometheus");
        return services;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

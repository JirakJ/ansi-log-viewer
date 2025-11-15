package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CacheManagementAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StringBuilder status = new StringBuilder();
        status.append("CACHE_MANAGEMENT:\n\n");
        
        // ANSI Code Cache
        AnsiCodeCache ansiCache = AnsiCodeCache.getInstance();
        status.append("ANSI Code Cache:\n");
        status.append("  Size: ").append(ansiCache.size()).append(" entries\n");
        status.append("  Status: ").append(ansiCache.size() > 0 ? "Active" : "Empty").append("\n\n");
        
        // Copilot Query Cache
        CopilotQueryCache copilotCache = CopilotQueryCache.getInstance();
        status.append("Copilot Query Cache:\n");
        status.append("  Size: ").append(copilotCache.getCacheSize()).append(" entries\n");
        status.append("  Status: ").append(copilotCache.getCacheSize() > 0 ? "Active" : "Empty").append("\n\n");
        
        // Memory stats
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        status.append("MEMORY_STATISTICS:\n");
        status.append(String.format("  Total: %s\n", formatBytes(totalMemory)));
        status.append(String.format("  Used: %s\n", formatBytes(usedMemory)));
        status.append(String.format("  Free: %s\n", formatBytes(freeMemory)));
        status.append(String.format("  Usage: %.1f%%\n\n", (usedMemory * 100.0 / totalMemory)));
        
        // Actions
        String[] actions = {"Clear All Caches", "Optimize Memory", "View Details"};
        int choice = showChoiceDialog(e, status.toString(), "Cache Management", actions, 0);
        
        switch(choice) {
            case 0 -> {
                ansiCache.clear();
                copilotCache.clear();
                Messages.showInfoMessage(e.getProject(), "✓ All caches cleared!", "Cache Cleared");
            }
            case 1 -> {
                copilotCache.clearExpired();
                System.gc();
                Messages.showInfoMessage(e.getProject(), "✓ Memory optimized!", "Memory Optimized");
            }
            case 2 -> {
                StringBuilder details = new StringBuilder();
                details.append("DETAILED_CACHE_INFO:\n\n");
                details.append("ANSI Code Cache (LRU):\n");
                details.append("  Max entries: 100\n");
                details.append("  Current: ").append(ansiCache.size()).append("\n");
                details.append("  TTL: 60 seconds\n\n");
                
                details.append("Copilot Query Cache (LRU):\n");
                details.append("  Max entries: 50\n");
                details.append("  Current: ").append(copilotCache.getCacheSize()).append("\n");
                details.append("  TTL: 1 hour\n\n");
                
                details.append("Auto-optimization:\n");
                details.append("  Expired entries: Auto-removed\n");
                details.append("  LRU eviction: Enabled\n");
                
                Messages.showInfoMessage(e.getProject(), details.toString(), "Cache Details");
            }
        }
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(true);
    }

    private int showChoiceDialog(AnActionEvent e, String message, String title, String[] options, int defaultIndex) {
        JBList<String> list = new JBList<>(options);
        list.setSelectedIndex(defaultIndex);
        
        DialogWrapper dialog = new DialogWrapper(e.getProject(), false) {
            {
                init();
                setTitle(title);
            }
            
            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new JLabel(message));
                panel.add(Box.createVerticalStrut(10));
                panel.add(new JScrollPane(list));
                return panel;
            }
        };
        
        if (dialog.showAndGet()) {
            return list.getSelectedIndex();
        }
        return -1;
    }
}

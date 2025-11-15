package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryMonitorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        
        String fileSize = formatBytes(editor.getDocument().getText().length());
        String heapUsedMB = formatBytes(heapUsed);
        String heapMaxMB = formatBytes(heapMax);
        String nonHeapMB = formatBytes(nonHeapUsed);
        
        String stats = String.format(
                "Memory & Performance Stats:\n\n" +
                "File size: %s\n" +
                "Heap memory used: %s / %s\n" +
                "Non-heap memory: %s\n" +
                "Heap usage: %.1f%%\n" +
                "\nCache size: %d entries\n" +
                "Processors: %d",
                fileSize,
                heapUsedMB,
                heapMaxMB,
                nonHeapMB,
                (heapUsed * 100.0 / heapMax),
                AnsiCodeCache.getInstance().size(),
                Runtime.getRuntime().availableProcessors()
        );
        
        Messages.showInfoMessage(e.getProject(), stats, "Memory Monitor");
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
}

package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class LogFingerprintAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        LogFingerprintAnalyzer.LogFingerprint fingerprint = LogFingerprintAnalyzer.generateFingerprint(text);
        
        StringBuilder output = new StringBuilder();
        output.append("LOG_FINGERPRINT_ANALYSIS:\n\n");
        
        output.append("IDENTITY:\n");
        output.append("  Hash: ").append(fingerprint.hash).append("\n");
        output.append("  Size: ").append(formatBytes(fingerprint.fileSize)).append("\n");
        output.append("  Lines: ").append(fingerprint.lineCount).append("\n");
        output.append("  Modified: ").append(formatTime(fingerprint.lastModified)).append("\n\n");
        
        output.append("FRAMEWORK:\n");
        output.append("  Detected: ").append(fingerprint.framework).append("\n\n");
        
        output.append("ERROR_ANALYSIS:\n");
        output.append("  Unique Error Types: ").append(fingerprint.uniqueErrors).append("\n");
        if (!fingerprint.errorFrequency.isEmpty()) {
            output.append("  Distribution:\n");
            fingerprint.errorFrequency.forEach((error, count) ->
                output.append(String.format("    • %s: %d\n", error, count))
            );
        }
        output.append("\n");
        
        if (!fingerprint.suspiciousPatterns.isEmpty()) {
            output.append("⚠️ SUSPICIOUS_PATTERNS:\n");
            fingerprint.suspiciousPatterns.forEach(pattern ->
                output.append("  • ").append(pattern).append("\n")
            );
        }
        
        String result = output.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard!",
                "Log Fingerprint"
        );
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String formatTime(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class IntelligentRecommendationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        StringBuilder recommendations = new StringBuilder();
        
        recommendations.append("INTELLIGENT_RECOMMENDATIONS:\n\n");
        
        // Analyze and provide recommendations
        analyzeAndRecommend(text, recommendations);
        
        String result = recommendations.toString();
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                result + "\n\nCopied to clipboard!",
                "Smart Recommendations"
        );
    }

    private void analyzeAndRecommend(String text, StringBuilder output) {
        // Check error density
        int errorCount = countOccurrences(text, "ERROR");
        int warnCount = countOccurrences(text, "WARN");
        int totalLines = text.split("\n").length;
        
        output.append("1. ERROR_DENSITY:\n");
        double errorRatio = (errorCount * 100.0) / totalLines;
        if (errorRatio > 30) {
            output.append("   ⚠️ CRITICAL: Very high error rate (").append(String.format("%.1f", errorRatio))
                    .append("%)\n");
            output.append("   → Action: Investigate system stability immediately\n");
        } else if (errorRatio > 10) {
            output.append("   ⚠️ HIGH: Elevated error rate (").append(String.format("%.1f", errorRatio))
                    .append("%)\n");
            output.append("   → Action: Review error patterns and root causes\n");
        } else if (errorRatio > 1) {
            output.append("   ℹ️ Moderate error rate (").append(String.format("%.1f", errorRatio))
                    .append("%)\n");
            output.append("   → Action: Monitor and log for patterns\n");
        } else {
            output.append("   ✓ Healthy: Low error rate (").append(String.format("%.1f", errorRatio))
                    .append("%)\n");
        }
        output.append("\n");
        
        // Check error-to-warning ratio
        output.append("2. ERROR_TO_WARNING_RATIO:\n");
        if (warnCount > 0) {
            double ratio = (double) errorCount / warnCount;
            output.append("   Ratio: 1:").append(String.format("%.1f", ratio)).append("\n");
            if (ratio > 3) {
                output.append("   ⚠️ Warnings are being ignored - many errors escalating\n");
            }
        }
        output.append("\n");
        
        // Check for resource issues
        output.append("3. RESOURCE_ISSUES:\n");
        int memoryErrors = countOccurrences(text, "OutOfMemory") + countOccurrences(text, "PermGen");
        int timeoutErrors = countOccurrences(text, "Timeout") + countOccurrences(text, "timeout");
        int connectionErrors = countOccurrences(text, "Connection") + countOccurrences(text, "refused");
        
        if (memoryErrors > 0) {
            output.append("   🔴 Memory issues detected (").append(memoryErrors).append(" occurrences)\n");
            output.append("   → Increase heap size or optimize memory usage\n");
        }
        if (timeoutErrors > 0) {
            output.append("   🟡 Timeout issues detected (").append(timeoutErrors).append(" occurrences)\n");
            output.append("   → Increase timeout thresholds or improve performance\n");
        }
        if (connectionErrors > 0) {
            output.append("   🟡 Connection issues detected (").append(connectionErrors).append(" occurrences)\n");
            output.append("   → Check network/database connectivity\n");
        }
        if (memoryErrors == 0 && timeoutErrors == 0 && connectionErrors == 0) {
            output.append("   ✓ No obvious resource issues detected\n");
        }
        output.append("\n");
        
        // Check for common issues
        output.append("4. COMMON_ISSUES:\n");
        int nullPointers = countOccurrences(text, "NullPointerException");
        int classNotFound = countOccurrences(text, "ClassNotFoundException");
        int sqlErrors = countOccurrences(text, "SQLException");
        
        List<String> issues = new ArrayList<>();
        if (nullPointers > 0) issues.add(String.format("NullPointerException (%d)", nullPointers));
        if (classNotFound > 0) issues.add(String.format("ClassNotFoundException (%d)", classNotFound));
        if (sqlErrors > 0) issues.add(String.format("SQLException (%d)", sqlErrors));
        
        if (issues.isEmpty()) {
            output.append("   ✓ No common exceptions found\n");
        } else {
            issues.forEach(issue -> output.append("   • ").append(issue).append("\n"));
        }
        output.append("\n");
        
        // Performance metrics
        output.append("5. NEXT_STEPS:\n");
        output.append("   1. Use 'Copilot Prompt Helper' for detailed analysis\n");
        output.append("   2. Run 'Smart Error Grouping' to categorize errors\n");
        output.append("   3. Check 'Timestamp Analysis' for performance patterns\n");
        if (errorCount > 0) {
            output.append("   4. Use 'Stack Trace Analyzer' for debugging\n");
        }
    }

    private int countOccurrences(String text, String word) {
        return (int) text.split(word, -1).length - 1;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

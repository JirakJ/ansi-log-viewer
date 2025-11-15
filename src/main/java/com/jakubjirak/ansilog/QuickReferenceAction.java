package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class QuickReferenceAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String reference = "ANSI Log Viewer - Quick Reference\n\n" +
                "KEYBOARD SHORTCUTS:\n" +
                "  Ctrl+Shift+A - Find Next ANSI Code\n\n" +
                "TOOLS MENU FEATURES:\n" +
                "  Hide/Show ANSI Codes - Toggle visibility\n" +
                "  Strip ANSI Codes - Permanently remove codes\n" +
                "  Copy Without ANSI Codes - Context menu option\n" +
                "  ANSI Code Statistics - Count and distribution\n" +
                "  Find Next ANSI Code - Jump to next code\n" +
                "  Export to HTML - Save with colors\n" +
                "  Show Error Lines - Filter red colored lines\n\n" +
                "ANALYSIS TOOLS:\n" +
                "  Filter by Log Level - ERROR/WARN/INFO/DEBUG/TRACE\n" +
                "  Log Level Statistics - Show distribution\n" +
                "  Normalize ANSI Codes - Convert 256 to standard\n" +
                "  Replace ANSI Codes - Bulk replacements\n" +
                "  Search ANSI Patterns - Find specific types\n" +
                "  Find Duplicate Lines - Detect redundancy\n" +
                "  Memory Monitor - Performance stats\n\n" +
                "SETTINGS:\n" +
                "  Tools → Settings → ANSI Log Viewer\n" +
                "  Configure file extensions\n" +
                "  Toggle ANSI code visibility\n" +
                "  Configure theme colors\n";
        
        Messages.showInfoMessage(e.getProject(), reference, "Quick Reference");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(true);
    }
}

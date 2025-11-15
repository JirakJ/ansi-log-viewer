package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CopilotPromptHelperAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String[] prompts = {
            "🔍 Analyze errors",
            "📊 Summarize log",
            "⚠️ Detect anomalies",
            "🐛 Debug suggestions",
            "🎯 Root cause analysis",
            "📈 Performance issues",
            "🔐 Security concerns"
        };
        
        int choice = showChoiceDialog(e, "Select analysis type for Copilot:", "Copilot Helper", prompts, 0);
        
        if (choice < 0) return;
        
        String text = editor.getDocument().getText();
        String context = CopilotContextBuilder.buildLogContext(text, 100);
        
        String prompt = switch(choice) {
            case 0 -> buildErrorAnalysisPrompt(context);
            case 1 -> buildSummaryPrompt(context);
            case 2 -> buildAnomalyPrompt(context);
            case 3 -> buildDebugPrompt(context);
            case 4 -> buildRootCausePrompt(context);
            case 5 -> buildPerformancePrompt(context);
            case 6 -> buildSecurityPrompt(context);
            default -> context;
        };
        
        // Copy to clipboard
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(prompt), null);
        
        Messages.showInfoMessage(
                e.getProject(),
                "✅ Analysis prompt copied to clipboard!\n\n" +
                "Open GitHub Copilot Chat and paste to analyze.",
                "Copilot Prompt"
        );
    }

    private String buildErrorAnalysisPrompt(String context) {
        return "Analyze the following application log and identify all errors:\n\n" +
                context + "\n\n" +
                "Please provide:\n" +
                "1. Summary of errors found\n" +
                "2. Error categories\n" +
                "3. Recommended fixes";
    }

    private String buildSummaryPrompt(String context) {
        return "Create a concise summary of this log file:\n\n" +
                context + "\n\n" +
                "Include:\n" +
                "1. Key events\n" +
                "2. Problem areas\n" +
                "3. Status assessment";
    }

    private String buildAnomalyPrompt(String context) {
        return "Identify unusual patterns in this log:\n\n" +
                context + "\n\n" +
                "Highlight:\n" +
                "1. Unexpected behaviors\n" +
                "2. Performance anomalies\n" +
                "3. Potential issues";
    }

    private String buildDebugPrompt(String context) {
        return "Help debug the issues in this log:\n\n" +
                context + "\n\n" +
                "Provide:\n" +
                "1. Possible causes\n" +
                "2. Debugging steps\n" +
                "3. Testing suggestions";
    }

    private String buildRootCausePrompt(String context) {
        return "Perform root cause analysis on this log:\n\n" +
                context + "\n\n" +
                "Determine:\n" +
                "1. Primary cause\n" +
                "2. Contributing factors\n" +
                "3. Prevention strategies";
    }

    private String buildPerformancePrompt(String context) {
        return "Identify performance issues in this log:\n\n" +
                context + "\n\n" +
                "Analyze:\n" +
                "1. Slow operations\n" +
                "2. Resource bottlenecks\n" +
                "3. Optimization suggestions";
    }

    private String buildSecurityPrompt(String context) {
        return "Check for security concerns in this log:\n\n" +
                context + "\n\n" +
                "Look for:\n" +
                "1. Failed authentications\n" +
                "2. Suspicious activities\n" +
                "3. Vulnerability indicators";
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
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

package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class AskCopilotAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String context = CopilotContextBuilder.buildLogContext(text, 100);
        
        String question = Messages.showInputDialog(
                e.getProject(),
                "Ask Copilot (context will include log summary):",
                "Ask Copilot",
                null
        );
        
        if (question == null || question.isEmpty()) return;
        
        String prompt = context + "\n\nUSER_QUESTION:\n" + question;
        
        // Prepare context for Copilot
        // The user's IDE should have Copilot Chat available via IDE's native UI
        Messages.showInfoMessage(
                e.getProject(),
                "Context prepared for Copilot Chat:\n\n" +
                "You can now open GitHub Copilot Chat and paste your question.\n" +
                "Context has been copied to clipboard.\n\n" +
                "Your question: " + question,
                "Copilot Ready"
        );
        
        // Copy prompt to clipboard for user to paste into Copilot Chat
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(prompt), null);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }
}

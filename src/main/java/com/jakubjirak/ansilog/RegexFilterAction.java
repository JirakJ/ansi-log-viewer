package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexFilterAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String pattern = Messages.showInputDialog(
                e.getProject(),
                "Enter regex pattern:\n(e.g., .*ERROR.* or ^\\[.*ERROR)",
                "Regex Filter",
                null
        );
        
        if (pattern == null || pattern.trim().isEmpty()) return;
        
        try {
            Pattern p = Pattern.compile(pattern);
            String text = editor.getDocument().getText();
            String[] lines = text.split("\n");
            
            List<String> matched = new ArrayList<>();
            for (String line : lines) {
                if (p.matcher(line).find()) {
                    matched.add(line);
                }
            }
            
            String result = String.format("Found %d matching line(s)\n\nPreview:\n%s",
                    matched.size(),
                    matched.stream()
                            .limit(10)
                            .reduce("", (a, b) -> a + b + "\n"));
            
            Messages.showInfoMessage(e.getProject(), result, "Regex Filter Results");
        } catch (PatternSyntaxException ex) {
            Messages.showErrorDialog(
                    e.getProject(),
                    "Invalid regex pattern:\n" + ex.getMessage(),
                    "Regex Error"
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

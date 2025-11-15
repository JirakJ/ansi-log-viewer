package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class FindDuplicateLinesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String[] lines = text.split("\n");
        
        Map<String, Integer> lineCount = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        
        for (String line : lines) {
            String cleanLine = line.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "").trim();
            if (!cleanLine.isEmpty()) {
                int count = lineCount.getOrDefault(cleanLine, 0);
                lineCount.put(cleanLine, count + 1);
                if (count > 0 && !duplicates.contains(cleanLine)) {
                    duplicates.add(cleanLine);
                }
            }
        }
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d duplicate line(s)\n\n", duplicates.size()));
        
        duplicates.stream()
                .limit(10)
                .forEach(line -> result.append(String.format("[%d] %s\n", lineCount.get(line), 
                        line.length() > 60 ? line.substring(0, 60) + "..." : line)));
        
        if (duplicates.size() > 10) {
            result.append(String.format("\n... and %d more", duplicates.size() - 10));
        }
        
        Messages.showInfoMessage(e.getProject(), result.toString(), "Duplicate Lines");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

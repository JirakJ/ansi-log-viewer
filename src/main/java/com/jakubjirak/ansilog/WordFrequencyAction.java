package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class WordFrequencyAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        String cleanText = text.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
        
        Map<String, Integer> wordFreq = new HashMap<>();
        String[] words = cleanText.split("[\\s\\p{P}]+");
        
        for (String word : words) {
            if (word.length() > 3) {
                String w = word.toLowerCase();
                wordFreq.put(w, wordFreq.getOrDefault(w, 0) + 1);
            }
        }
        
        String result = wordFreq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(20)
                .map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
        
        String message = String.format("Top 20 Most Frequent Words:\n\n%s\n\nTotal unique words (>3 chars): %d",
                result, wordFreq.size());
        
        Messages.showInfoMessage(e.getProject(), message, "Word Frequency Analysis");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

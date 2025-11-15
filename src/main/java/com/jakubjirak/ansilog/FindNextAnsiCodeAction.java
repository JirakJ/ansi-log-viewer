package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import java.util.regex.Matcher;

public class FindNextAnsiCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        int cursorOffset = editor.getCaretModel().getOffset();
        
        Matcher matcher = AnsiPatternUtil.ANSI_PATTERN.matcher(text);
        
        boolean found = false;
        while (matcher.find()) {
            if (matcher.start() > cursorOffset) {
                editor.getCaretModel().moveToOffset(matcher.start());
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                editor.getSelectionModel().setSelection(matcher.start(), matcher.end());
                found = true;
                break;
            }
        }
        
        if (!found) {
            Messages.showInfoMessage(e.getProject(), "No more ANSI codes found.", "ANSI Log Viewer");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String text = editor != null ? editor.getDocument().getText() : "";
        e.getPresentation().setEnabled(!text.isEmpty() && (text.contains("\u001B") || text.contains("\\u001B")));
    }
}

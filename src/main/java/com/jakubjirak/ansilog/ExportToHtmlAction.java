package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

public class ExportToHtmlAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getProject();
        if (editor == null || project == null) return;
        
        FileSaverDescriptor descriptor = new FileSaverDescriptor("Export to HTML", "Save as HTML file", "html");
        VirtualFileWrapper result = FileChooserFactory.getInstance()
                .createSaveFileDialog(descriptor, project).save((java.nio.file.Path) null, "log.html");
        
        if (result == null) return;
        
        String htmlContent = generateHtml(editor);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(result.getFile()))) {
            writer.write(htmlContent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateHtml(Editor editor) {
        String text = editor.getDocument().getText();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>ANSI Log</title>\n");
        html.append("<style>\n");
        html.append("body { background-color: #1e1e1e; color: #d4d4d4; font-family: monospace; white-space: pre-wrap; word-wrap: break-word; padding: 20px; }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        Matcher matcher = AnsiPatternUtil.ANSI_PATTERN.matcher(text);
        
        int lastIndex = 0;
        String currentColor = "#d4d4d4";
        
        while (matcher.find()) {
            String textBefore = text.substring(lastIndex, matcher.start());
            html.append(escapeHtml(textBefore));
            
            String seq = matcher.group();
            currentColor = parseAnsiCode(seq, currentColor);
            lastIndex = matcher.end();
        }
        
        html.append(escapeHtml(text.substring(lastIndex)));
        html.append("\n</body>\n</html>");
        
        return html.toString();
    }

    private String parseAnsiCode(String code, String currentColor) {
        return currentColor;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

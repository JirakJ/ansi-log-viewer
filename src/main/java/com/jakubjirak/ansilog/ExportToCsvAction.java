package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExportToCsvAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        FileSaverDescriptor descriptor = new FileSaverDescriptor("Export to CSV", "Save as CSV file", "csv");
        VirtualFileWrapper result = FileChooserFactory.getInstance()
                .createSaveFileDialog(descriptor, e.getProject()).save((java.nio.file.Path) null, "log.csv");
        
        if (result == null) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(result.getFile()))) {
            String text = editor.getDocument().getText();
            String[] lines = text.split("\n");
            
            writer.write("Line Number,Content,ANSI Code Count,Log Level\n");
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String cleanLine = line.replaceAll("(?:\\u001B|\\\\u001B)\\[[0-9;]*m", "");
                
                long ansiCount = AnsiPatternUtil.ANSI_PATTERN.matcher(line).results().count();
                
                String logLevel = "INFO";
                if (cleanLine.toUpperCase().contains("ERROR")) logLevel = "ERROR";
                else if (cleanLine.toUpperCase().contains("WARN")) logLevel = "WARN";
                else if (cleanLine.toUpperCase().contains("DEBUG")) logLevel = "DEBUG";
                
                String csvLine = String.format("\"%d\",\"%s\",\"%d\",\"%s\"\n",
                        i + 1,
                        escapeCsv(cleanLine),
                        ansiCount,
                        logLevel);
                
                writer.write(csvLine);
            }
            
            System.out.println("CSV exported successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null);
    }
}

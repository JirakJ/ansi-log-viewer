package com.jakubjirak.ansilog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AsyncAnsiProcessor {
    private static final int LARGE_FILE_THRESHOLD = 1_000_000;

    public static void processAnsiCodesAsync(@NotNull Editor editor, @Nullable Project project) {
        String text = editor.getDocument().getText();
        
        if (text.length() < LARGE_FILE_THRESHOLD) {
            processSync(editor);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(
                project,
                "Processing ANSI codes...",
                true
        ) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText("Analyzing ANSI codes...");
                
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!editor.isDisposed()) {
                        processSync(editor);
                        indicator.setFraction(1.0);
                    }
                });
            }
        });
    }

    private static void processSync(Editor editor) {
        if (editor instanceof com.intellij.openapi.editor.ex.EditorEx) {
            new AnsiLogFileOpenListener().applyAnsiHighlighting(editor);
        }
    }
}

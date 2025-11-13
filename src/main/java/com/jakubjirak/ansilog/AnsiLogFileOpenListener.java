package com.jakubjirak.ansilog;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.editor.ex.EditorEx;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.util.Alarm;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiLogFileOpenListener implements FileEditorManagerListener {
    private final Map<Document, Alarm> alarms = new WeakHashMap<>();
    private static final int DEBOUNCE_MS = 200;
    private static final Pattern ESC_PATTERN = Pattern.compile("\u001B\\[[0-9;]*m");

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        String name = file.getName();
        List<String> exts = AnsiLogSettingsState.getInstance().getExtensions();
        boolean match = exts.stream().anyMatch(ext -> name.endsWith("." + ext));
        if (!match) return;
        Arrays.stream(source.getEditors(file)).filter(e -> e instanceof TextEditor).findFirst().ifPresent(e -> {
            Editor editor = ((TextEditor) e).getEditor();
            applyAnsiHighlighting(editor);
            attachLiveUpdate(editor);
        });
    }

    private void applyAnsiHighlighting(Editor editor) {
        String text = editor.getDocument().getText();
        MarkupModel markup = editor.getMarkupModel();
        markup.removeAllHighlighters();

        if (editor instanceof EditorEx) {
            FoldingModel foldingModel = editor.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> {
                Matcher m = ESC_PATTERN.matcher(text);
                while (m.find()) {
                    foldingModel.addFoldRegion(m.start(), m.end(), "");
                }
            });
        }

        Matcher matcher = ESC_PATTERN.matcher(text);
        int lastIndex = 0;
        TextAttributes current = new TextAttributes();
        while (matcher.find()) {
            int escStart = matcher.start();
            if (escStart > lastIndex) {
                addHighlighter(markup, lastIndex, escStart, current);
            }
            String seq = matcher.group();
            applySequenceToAttributes(seq, current);
            lastIndex = matcher.end();
        }
        if (lastIndex < text.length()) {
            addHighlighter(markup, lastIndex, text.length(), current);
        }
    }

    private void addHighlighter(MarkupModel markup, int start, int end, TextAttributes attrs) {
        if (start >= end) return;
        TextAttributes copy = attrs.clone();
        markup.addRangeHighlighter(start, end, HighlighterLayer.ADDITIONAL_SYNTAX, copy, HighlighterTargetArea.EXACT_RANGE);
    }

    private void applySequenceToAttributes(String esc, TextAttributes attrs) {
        int lb = esc.indexOf('[');
        int m = esc.indexOf('m');
        if (lb < 0 || m < 0) return;
        String codesStr = esc.substring(lb + 1, m);
        if (codesStr.isEmpty()) codesStr = "0";
        String[] codes = codesStr.split(";");
        for (int i = 0; i < codes.length; i++) {
            int c;
            try { c = Integer.parseInt(codes[i]); } catch (NumberFormatException e) { continue; }
            if (c == 0) { reset(attrs); }
            else if (c == 1) { attrs.setFontType(Font.BOLD); }
            else if (c == 3) { attrs.setFontType(Font.ITALIC); }
            else if (c == 4) { attrs.setEffectType(EffectType.LINE_UNDERSCORE); }
            else if (30 <= c && c <= 37) { attrs.setForegroundColor(ansiColor(c - 30, false)); }
            else if (90 <= c && c <= 97) { attrs.setForegroundColor(ansiColor(c - 90, true)); }
            else if (40 <= c && c <= 47) { attrs.setBackgroundColor(ansiColor(c - 40, false)); }
            else if (100 <= c && c <= 107) { attrs.setBackgroundColor(ansiColor(c - 100, true)); }
            // 256-color & truecolor sequences
            else if (c == 38 || c == 48) { // foreground/background extended
                boolean fg = (c == 38);
                if (i + 1 < codes.length) {
                    if ("5".equals(codes[i + 1]) && i + 2 < codes.length) { // 256 color
                        try {
                            int idx = Integer.parseInt(codes[i + 2]);
                            Color col = ansi256(idx);
                            if (fg) attrs.setForegroundColor(col); else attrs.setBackgroundColor(col);
                        } catch (NumberFormatException ignored) {}
                        i += 2;
                    } else if ("2".equals(codes[i + 1]) && i + 4 < codes.length) { // truecolor r;g;b
                        try {
                            int r = Integer.parseInt(codes[i + 2]);
                            int g = Integer.parseInt(codes[i + 3]);
                            int b = Integer.parseInt(codes[i + 4]);
                            Color col = new Color(r, g, b);
                            if (fg) attrs.setForegroundColor(col); else attrs.setBackgroundColor(col);
                        } catch (NumberFormatException ignored) {}
                        i += 4;
                    }
                }
            }
        }
    }

    private Color ansi256(int idx) {
        if (idx < 0) idx = 0; if (idx > 255) idx = 255;
        if (idx < 16) { // basic + bright
            boolean bright = idx > 7;
            int base = idx % 8;
            return ansiColor(base, bright);
        }
        if (idx >= 16 && idx <= 231) { // 6x6x6 cube
            int cube = idx - 16;
            int r = (cube / 36) % 6;
            int g = (cube / 6) % 6;
            int b = cube % 6;
            int conv = 55; // base offset except 0
            int cr = r == 0 ? 0 : conv + (r - 1) * 40;
            int cg = g == 0 ? 0 : conv + (g - 1) * 40;
            int cb = b == 0 ? 0 : conv + (b - 1) * 40;
            return new Color(cr, cg, cb);
        }
        // grayscale 232-255
        int gray = 8 + (idx - 232) * 10;
        return new Color(gray, gray, gray);
    }

    private void reset(TextAttributes attrs) {
        attrs.setForegroundColor(null);
        attrs.setBackgroundColor(null);
        attrs.setFontType(Font.PLAIN);
        attrs.setEffectType(null);
        attrs.setEffectColor(null);
    }

    private Color ansiColor(int idx, boolean bright) {
        Color[] base = new Color[]{
                new Color(0,0,0),
                new Color(128,0,0),
                new Color(0,128,0),
                new Color(128,128,0),
                new Color(0,0,128),
                new Color(128,0,128),
                new Color(0,128,128),
                new Color(192,192,192)
        };
        Color c = base[Math.max(0, Math.min(idx, base.length-1))];
        if (!bright) return c;
        return new Color(Math.min(255, c.getRed() + 80), Math.min(255, c.getGreen() + 80), Math.min(255, c.getBlue() + 80));
    }

    private void attachLiveUpdate(Editor editor) {
        Document doc = editor.getDocument();
        if (alarms.containsKey(doc)) return;
        Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
        alarms.put(doc, alarm);
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (doc.getTextLength() > 5_000_000) return; // skip very large files
                alarm.cancelAllRequests();
                alarm.addRequest(() -> {
                    if (editor.isDisposed()) return;
                    applyAnsiHighlighting(editor);
                }, DEBOUNCE_MS);
            }
        });
    }
}

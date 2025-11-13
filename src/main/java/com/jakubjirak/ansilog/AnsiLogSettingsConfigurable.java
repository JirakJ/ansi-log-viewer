package com.jakubjirak.ansilog;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnsiLogSettingsConfigurable implements Configurable {
    private JTextField extensionsField;

    @Override public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() { return "ANSI Log Viewer"; }

    @Override public @Nullable JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        extensionsField = new JTextField(String.join(",", AnsiLogSettingsState.getInstance().getExtensions()));
        panel.add(new JLabel("File extensions (comma separated, without leading dot):"), BorderLayout.NORTH);
        panel.add(extensionsField, BorderLayout.CENTER);
        return panel;
    }

    @Override public boolean isModified() {
        List<String> current = AnsiLogSettingsState.getInstance().getExtensions();
        List<String> entered = parse();
        return !current.equals(entered);
    }

    private List<String> parse() {
        return Arrays.stream(extensionsField.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override public void apply() throws ConfigurationException { AnsiLogSettingsState.getInstance().setExtensions(parse()); }

    @Override public void reset() { extensionsField.setText(String.join(",", AnsiLogSettingsState.getInstance().getExtensions())); }

    @Override public void disposeUIResources() { extensionsField = null; }
}

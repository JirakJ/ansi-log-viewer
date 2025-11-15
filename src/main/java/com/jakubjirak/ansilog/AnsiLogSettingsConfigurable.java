package com.jakubjirak.ansilog;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnsiLogSettingsConfigurable implements Configurable {
    private JTextField extensionsField;
    private JCheckBox hideAnsiCodesCheckbox;
    private JCheckBox showOnCursorCheckbox;
    private JCheckBox darkThemeCheckbox;

    @Override public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() { return "ANSI Log Viewer"; }

    @Override public @Nullable JComponent createComponent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // File Extensions Panel
        JPanel extPanel = new JPanel();
        extPanel.setLayout(new BoxLayout(extPanel, BoxLayout.Y_AXIS));
        extPanel.setBorder(new TitledBorder("File Extensions"));
        
        JLabel extLabel = new JLabel("Extensions to process (comma separated, without dot):");
        extLabel.setFont(extLabel.getFont().deriveFont(Font.PLAIN));
        extPanel.add(extLabel);
        extPanel.add(Box.createVerticalStrut(5));
        
        extensionsField = new JTextField(String.join(",", AnsiLogSettingsState.getInstance().getExtensions()), 30);
        extensionsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, extensionsField.getPreferredSize().height));
        extPanel.add(extensionsField);
        extPanel.add(Box.createVerticalStrut(5));
        
        JLabel extHint = new JLabel("Example: log, txt, out");
        extHint.setFont(extHint.getFont().deriveFont(Font.ITALIC, 11f));
        extHint.setForeground(Color.GRAY);
        extPanel.add(extHint);
        
        mainPanel.add(extPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // ANSI Display Settings Panel
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBorder(new TitledBorder("Display Settings"));
        
        hideAnsiCodesCheckbox = new JCheckBox("Hide ANSI escape codes (show only colors)", 
            AnsiLogSettingsState.getInstance().isHideAnsiCodes());
        hideAnsiCodesCheckbox.setToolTipText("When enabled, ANSI escape sequences are folded and invisible - only colors are visible");
        displayPanel.add(hideAnsiCodesCheckbox);
        displayPanel.add(Box.createVerticalStrut(8));
        
        showOnCursorCheckbox = new JCheckBox("Show ANSI codes on cursor hover", 
            AnsiLogSettingsState.getInstance().isShowAnsiCodesOnCursor());
        showOnCursorCheckbox.setToolTipText("When enabled, hovering cursor on current line reveals the hidden ANSI codes");
        displayPanel.add(showOnCursorCheckbox);
        displayPanel.add(Box.createVerticalStrut(8));
        
        darkThemeCheckbox = new JCheckBox("Use dark theme colors", 
            AnsiLogSettingsState.getInstance().isDarkTheme());
        darkThemeCheckbox.setToolTipText("Optimize colors for dark IDE background");
        displayPanel.add(darkThemeCheckbox);
        
        mainPanel.add(displayPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new TitledBorder("Quick Tips"));
        
        JLabel tip1 = new JLabel("• Hide ANSI codes to see only colors");
        JLabel tip2 = new JLabel("• Right-click to copy text without ANSI codes");
        JLabel tip3 = new JLabel("• Use regex patterns for advanced filtering");
        
        infoPanel.add(tip1);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(tip2);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(tip3);
        
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalGlue());
        
        return mainPanel;
    }

    @Override public boolean isModified() {
        List<String> current = AnsiLogSettingsState.getInstance().getExtensions();
        List<String> entered = parse();
        boolean extChanged = !current.equals(entered);
        boolean hideChanged = AnsiLogSettingsState.getInstance().isHideAnsiCodes() != hideAnsiCodesCheckbox.isSelected();
        boolean cursorChanged = AnsiLogSettingsState.getInstance().isShowAnsiCodesOnCursor() != showOnCursorCheckbox.isSelected();
        boolean themeChanged = AnsiLogSettingsState.getInstance().isDarkTheme() != darkThemeCheckbox.isSelected();
        return extChanged || hideChanged || cursorChanged || themeChanged;
    }

    private List<String> parse() {
        return Arrays.stream(extensionsField.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override public void apply() throws ConfigurationException { 
        AnsiLogSettingsState.getInstance().setExtensions(parse());
        AnsiLogSettingsState.getInstance().setHideAnsiCodes(hideAnsiCodesCheckbox.isSelected());
        AnsiLogSettingsState.getInstance().setShowAnsiCodesOnCursor(showOnCursorCheckbox.isSelected());
        AnsiLogSettingsState.getInstance().setDarkTheme(darkThemeCheckbox.isSelected());
    }

    @Override public void reset() { 
        extensionsField.setText(String.join(",", AnsiLogSettingsState.getInstance().getExtensions()));
        hideAnsiCodesCheckbox.setSelected(AnsiLogSettingsState.getInstance().isHideAnsiCodes());
        showOnCursorCheckbox.setSelected(AnsiLogSettingsState.getInstance().isShowAnsiCodesOnCursor());
        darkThemeCheckbox.setSelected(AnsiLogSettingsState.getInstance().isDarkTheme());
    }

    @Override public void disposeUIResources() { 
        extensionsField = null;
        hideAnsiCodesCheckbox = null;
        showOnCursorCheckbox = null;
        darkThemeCheckbox = null;
    }
}

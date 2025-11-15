package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new SettingsDialog().show();
    }

    private static class SettingsDialog extends DialogWrapper {
        private JTextField extensionsField;
        private JCheckBox hideAnsiCodesCheckbox;
        private JCheckBox showOnCursorCheckbox;
        private JCheckBox darkThemeCheckbox;

        protected SettingsDialog() {
            super(true);
            setTitle("ANSI Log Viewer Settings");
            setSize(500, 400);
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
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
            
            JLabel tip1 = new JLabel("• Press Ctrl+Shift+A to find next ANSI code");
            JLabel tip2 = new JLabel("• Toggle visibility from Tools > Hide ANSI Codes");
            JLabel tip3 = new JLabel("• Use Tools > Advanced Search for powerful log filtering");
            
            infoPanel.add(tip1);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(tip2);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(tip3);
            
            mainPanel.add(infoPanel);
            mainPanel.add(Box.createVerticalGlue());
            
            return mainPanel;
        }

        @Override
        protected void doOKAction() {
            AnsiLogSettingsState state = AnsiLogSettingsState.getInstance();
            state.setExtensions(parse());
            state.setHideAnsiCodes(hideAnsiCodesCheckbox.isSelected());
            state.setShowAnsiCodesOnCursor(showOnCursorCheckbox.isSelected());
            state.setDarkTheme(darkThemeCheckbox.isSelected());
            super.doOKAction();
        }

        private List<String> parse() {
            return Arrays.stream(extensionsField.getText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
}

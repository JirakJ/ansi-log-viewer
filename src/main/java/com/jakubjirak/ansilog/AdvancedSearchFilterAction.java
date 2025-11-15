package com.jakubjirak.ansilog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AdvancedSearchFilterAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        
        String text = editor.getDocument().getText();
        Project project = e.getProject();
        
        AdvancedSearchDialog dialog = new AdvancedSearchDialog(project, text);
        if (dialog.showAndGet()) {
            String results = dialog.getResults();
            Messages.showInfoMessage(project, results, "Advanced Search Results");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(editor != null && !editor.getDocument().getText().isEmpty());
    }

    private static class AdvancedSearchDialog extends DialogWrapper {
        private final String logText;
        private JTextField searchField;
        private JCheckBox regexCheckbox;
        private JCheckBox caseCheckbox;
        private JCheckBox wholeWordCheckbox;
        private JTextField includeField;
        private JTextField excludeField;
        private JSpinner contextSpinner;
        private JTextArea resultArea;
        private String searchResults = "";

        protected AdvancedSearchDialog(@Nullable Project project, String logText) {
            super(project);
            this.logText = logText;
            setTitle("Advanced Log Search");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Search criteria
            JPanel searchPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
            
            searchField = new JTextField(30);
            searchPanel.add(createLabeledField("Search Pattern:", searchField));
            
            JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            regexCheckbox = new JCheckBox("Regular Expression");
            caseCheckbox = new JCheckBox("Case Sensitive", true);
            wholeWordCheckbox = new JCheckBox("Whole Word");
            checkboxPanel.add(regexCheckbox);
            checkboxPanel.add(caseCheckbox);
            checkboxPanel.add(wholeWordCheckbox);
            searchPanel.add(checkboxPanel);
            
            includeField = new JTextField(30);
            searchPanel.add(createLabeledField("Include Pattern (regex):", includeField));
            
            excludeField = new JTextField(30);
            searchPanel.add(createLabeledField("Exclude Pattern (regex):", excludeField));
            
            contextSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
            searchPanel.add(createLabeledField("Context Lines:", contextSpinner));

            // Results
            JPanel resultsPanel = new JPanel(new BorderLayout());
            resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
            resultArea = new JTextArea(15, 50);
            resultArea.setEditable(false);
            resultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            resultsPanel.add(new JBScrollPane(resultArea), BorderLayout.CENTER);

            mainPanel.add(searchPanel, BorderLayout.NORTH);
            mainPanel.add(resultsPanel, BorderLayout.CENTER);

            return mainPanel;
        }

        @Override
        protected void doOKAction() {
            performSearch();
            super.doOKAction();
        }

        private void performSearch() {
            String pattern = searchField.getText();
            if (pattern.isEmpty()) {
                resultArea.setText("Please enter a search pattern");
                return;
            }

            AdvancedLogSearchEngine.SearchQuery query = new AdvancedLogSearchEngine.SearchQuery(pattern);
            query.regex = regexCheckbox.isSelected();
            query.caseSensitive = caseCheckbox.isSelected();
            query.wholeWord = wholeWordCheckbox.isSelected();
            query.contextLines = (Integer) contextSpinner.getValue();

            if (!includeField.getText().isEmpty()) {
                query.includePatterns.add(includeField.getText());
            }
            if (!excludeField.getText().isEmpty()) {
                query.excludePatterns.add(excludeField.getText());
            }

            List<AdvancedLogSearchEngine.SearchResult> results = AdvancedLogSearchEngine.search(logText, query);

            StringBuilder output = new StringBuilder();
            output.append("Found ").append(results.size()).append(" match(es)\n");
            output.append("=".repeat(60)).append("\n\n");

            for (AdvancedLogSearchEngine.SearchResult result : results) {
                output.append("Line ").append(result.lineNumber).append(" (offset ").append(result.offset).append("):\n");
                output.append(result.line).append("\n");
                if (!result.context.isEmpty()) {
                    output.append("Context:\n");
                    for (String ctx : result.context) {
                        output.append("  ").append(ctx).append("\n");
                    }
                }
                output.append("-".repeat(60)).append("\n");
            }

            resultArea.setText(output.toString());
            searchResults = output.toString();
        }

        public String getResults() {
            return searchResults;
        }

        private JComponent createLabeledField(String label, JComponent field) {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.add(new JLabel(label), BorderLayout.WEST);
            panel.add(field, BorderLayout.CENTER);
            return panel;
        }
    }
}

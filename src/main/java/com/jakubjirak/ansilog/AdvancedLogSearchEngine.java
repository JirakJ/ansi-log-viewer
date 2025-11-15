package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.regex.*;

public class AdvancedLogSearchEngine {
    
    public static class SearchQuery {
        public String text;
        public boolean regex;
        public boolean caseSensitive;
        public boolean wholeWord;
        public List<String> includePatterns;
        public List<String> excludePatterns;
        public int contextLines;
        
        public SearchQuery(String text) {
            this.text = text;
            this.regex = false;
            this.caseSensitive = true;
            this.wholeWord = false;
            this.includePatterns = new ArrayList<>();
            this.excludePatterns = new ArrayList<>();
            this.contextLines = 2;
        }
    }
    
    public static class SearchResult {
        public int lineNumber;
        public int offset;
        public String line;
        public String before;
        public String match;
        public String after;
        public List<String> context;
        
        public SearchResult(int lineNumber, int offset, String line, String match) {
            this.lineNumber = lineNumber;
            this.offset = offset;
            this.line = line;
            this.match = match;
            this.context = new ArrayList<>();
        }
    }
    
    public static List<SearchResult> search(@NotNull String text, @NotNull SearchQuery query) {
        List<SearchResult> results = new ArrayList<>();
        String[] lines = text.split("\n");
        
        Pattern mainPattern = compilePattern(query.text, query.regex, query.caseSensitive);
        if (mainPattern == null) return results;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Apply exclude patterns
            if (shouldExclude(line, query.excludePatterns)) {
                continue;
            }
            
            // Apply include patterns
            if (!query.includePatterns.isEmpty() && !shouldInclude(line, query.includePatterns)) {
                continue;
            }
            
            Matcher matcher = mainPattern.matcher(removeAnsiCodes(line));
            while (matcher.find()) {
                SearchResult result = new SearchResult(
                    i + 1,
                    matcher.start(),
                    line,
                    matcher.group()
                );
                
                // Add context lines
                for (int j = Math.max(0, i - query.contextLines); j < Math.min(lines.length, i + query.contextLines + 1); j++) {
                    result.context.add(lines[j]);
                }
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    public static List<Integer> findLineNumbers(@NotNull String text, @NotNull String pattern) {
        List<Integer> lineNumbers = new ArrayList<>();
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (p.matcher(removeAnsiCodes(lines[i])).find()) {
                lineNumbers.add(i + 1);
            }
        }
        
        return lineNumbers;
    }
    
    public static Map<String, Integer> groupByPattern(@NotNull String text, @NotNull String pattern) {
        Map<String, Integer> groups = new LinkedHashMap<>();
        Pattern p = Pattern.compile(pattern);
        
        String[] lines = text.split("\n");
        for (String line : lines) {
            Matcher m = p.matcher(removeAnsiCodes(line));
            if (m.find()) {
                String key = m.group(1).isEmpty() ? m.group() : m.group(1);
                groups.put(key, groups.getOrDefault(key, 0) + 1);
            }
        }
        
        return groups;
    }
    
    public static List<String> extractMatches(@NotNull String text, @NotNull String pattern) {
        List<String> matches = new ArrayList<>();
        Pattern p = Pattern.compile(pattern);
        
        String[] lines = text.split("\n");
        Set<String> seen = new HashSet<>();
        
        for (String line : lines) {
            Matcher m = p.matcher(removeAnsiCodes(line));
            while (m.find()) {
                String match = m.group();
                if (seen.add(match)) {
                    matches.add(match);
                }
            }
        }
        
        return matches;
    }
    
    private static Pattern compilePattern(String text, boolean regex, boolean caseSensitive) {
        try {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            if (regex) {
                return Pattern.compile(text, flags);
            } else {
                return Pattern.compile(Pattern.quote(text), flags);
            }
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
    
    private static boolean shouldExclude(String line, List<String> patterns) {
        String clean = removeAnsiCodes(line);
        for (String pattern : patterns) {
            if (clean.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean shouldInclude(String line, List<String> patterns) {
        String clean = removeAnsiCodes(line);
        for (String pattern : patterns) {
            if (clean.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    private static String removeAnsiCodes(String text) {
        return text.replaceAll("(?:\u001B|\\u001B)\\[[0-9;]*m", "");
    }
}

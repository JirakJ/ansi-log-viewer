package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LineCache {
    private final Map<Integer, LineCacheEntry> cache;
    private static final int MAX_ENTRIES = 1000;

    public LineCache() {
        this.cache = new LinkedHashMap<Integer, LineCacheEntry>(MAX_ENTRIES, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_ENTRIES;
            }
        };
    }

    public void put(int lineNumber, String lineContent, LineInfo info) {
        cache.put(lineNumber, new LineCacheEntry(lineContent, info));
    }

    public LineInfo get(int lineNumber, String lineContent) {
        LineCacheEntry entry = cache.get(lineNumber);
        if (entry != null && entry.content.equals(lineContent)) {
            return entry.info;
        }
        return null;
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public static class LineInfo {
        public final int ansiCodeCount;
        public final boolean hasError;
        public final boolean hasWarn;
        public final boolean hasInfo;

        public LineInfo(int ansiCodeCount, boolean hasError, boolean hasWarn, boolean hasInfo) {
            this.ansiCodeCount = ansiCodeCount;
            this.hasError = hasError;
            this.hasWarn = hasWarn;
            this.hasInfo = hasInfo;
        }
    }

    private static class LineCacheEntry {
        final String content;
        final LineInfo info;

        LineCacheEntry(String content, LineInfo info) {
            this.content = content;
            this.info = info;
        }
    }
}

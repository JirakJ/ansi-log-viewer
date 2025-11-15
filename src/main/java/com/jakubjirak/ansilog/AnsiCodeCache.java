package com.jakubjirak.ansilog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnsiCodeCache {
    private static final AnsiCodeCache INSTANCE = new AnsiCodeCache();
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 60000;
    private static final int MAX_CACHE_SIZE = 100;

    public static AnsiCodeCache getInstance() {
        return INSTANCE;
    }

    public CacheEntry get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry;
        }
        if (entry != null) {
            cache.remove(key);
        }
        return null;
    }

    public void put(String key, Object value) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            cache.clear();
        }
        cache.put(key, new CacheEntry(value, System.currentTimeMillis()));
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public static class CacheEntry {
        private final Object value;
        private final long timestamp;

        public CacheEntry(Object value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL;
        }
    }
}

package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class CopilotQueryCache {
    private static final CopilotQueryCache INSTANCE = new CopilotQueryCache();
    private final Map<String, CachedQuery> queryCache = Collections.synchronizedMap(new LinkedHashMap<String, CachedQuery>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 50;
        }
    });

    public static CopilotQueryCache getInstance() {
        return INSTANCE;
    }

    public void cacheQuery(@NotNull String logHash, @NotNull String query, @NotNull String response) {
        queryCache.put(logHash + ":" + query, new CachedQuery(response, System.currentTimeMillis()));
    }

    @Nullable
    public String getCachedResponse(@NotNull String logHash, @NotNull String query) {
        CachedQuery cached = queryCache.get(logHash + ":" + query);
        if (cached != null && !cached.isExpired()) {
            return cached.response;
        }
        return null;
    }

    public void clearExpired() {
        queryCache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    public int getCacheSize() {
        return queryCache.size();
    }

    public void clear() {
        queryCache.clear();
    }

    public static class CachedQuery {
        public final String response;
        public final long timestamp;
        private static final long TTL = 3600000; // 1 hour

        public CachedQuery(String response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TTL;
        }
    }
}

package com.sismics.reader.core.util.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiCache {
    private Map<String, CacheEntry> cache = new HashMap<>();
    private Duration ttl;

    public ApiCache(Duration ttl) {
        this.ttl = ttl;
    }

    public Optional<JSONObject> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getData();
        }
        return Optional.empty();
    }

    public void put(String key, Optional<JSONObject> value) {
        Instant expiryTime = Instant.now().plus(ttl);
        cache.put(key, new CacheEntry(value, expiryTime));
    }
}
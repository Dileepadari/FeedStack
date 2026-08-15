package com.sismics.reader.core.util.cache;

import java.time.Instant;
import java.util.Optional;
import org.json.JSONObject;

public class CacheEntry {
    private Optional<JSONObject> data;
    private Instant expiryTime;

    public CacheEntry(Optional<JSONObject> data, Instant expiryTime) {
        this.data = data;
        this.expiryTime = expiryTime;
    }

    public Optional<JSONObject> getData() {
        return data;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }
}
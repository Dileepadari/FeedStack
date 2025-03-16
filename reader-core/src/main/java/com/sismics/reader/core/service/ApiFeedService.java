package com.sismics.reader.core.service;

import com.sismics.reader.core.util.cache.ApiCache;
import com.sismics.reader.core.util.cache.CacheEntry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.time.Duration;

public class ApiFeedService {
    private static final Logger logger = LoggerFactory.getLogger(ApiFeedService.class);
    private static ApiFeedService instance;
    private ApiCache cache = new ApiCache(Duration.ofMinutes(15));

    private ApiFeedService() {
    }

    public static ApiFeedService getInstance() {
        if (instance == null) {
            instance = new ApiFeedService();
        }
        return instance;
    }
    
    public Optional<JSONObject> fetchContent(String apiCall) {
        
        Optional<JSONObject> cachedContent = cache.get(apiCall);
        if (cachedContent.isPresent()) {
            logger.info("Returning cached content for: {}", apiCall);
            return cachedContent;
        }

        try {
            logger.info("Fetching content from: {}", apiCall);
            URL url = new URL(apiCall);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    cache.put(apiCall, Optional.of(new JSONObject(response.toString())));
                    return Optional.of(new JSONObject(response.toString()));
                }
            } else {
                logger.error("Failed to fetch content. HTTP Response Code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Exception while fetching content: ", e);
        }
        cache.put(apiCall, Optional.empty());
        return Optional.empty();
    }
}

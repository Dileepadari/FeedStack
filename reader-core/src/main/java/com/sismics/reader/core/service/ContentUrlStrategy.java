package com.sismics.reader.core.service;

import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.service.UrlStrategy;
import com.sismics.reader.core.service.ContentInterface;
import com.sismics.reader.core.service.ApiFeedService;

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

public class ContentUrlStrategy implements UrlStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ContentUrlStrategy.class);
    private static final String API_URL = "https://newsapi.org/v2/everything";
    private static final String API_KEY = "d8e9a2bcc6394a208e234c1241d43c62";

    ApiFeedService apiFeedService = ApiFeedService.getInstance();
    ContentInterface feedAdapter = new FeedAdapter(apiFeedService);

    @Override
    public RssReader getRssFeedReader(String url) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            logger.error("API Key is missing! Set the NEWS_API_KEY environment variable.");
            throw new IllegalStateException("API Key is required for News API.");
        }

        try {
            final RssReader reader = new RssReader();
            InputStream is = getRssFeed(url);
            reader.readRssFeed(is);
            reader.getFeed().setRssUrl(url);
            return reader;  
        }
        catch (Exception e) {
            logger.error("Error while parsing feed: " + url, e);
            throw new RuntimeException("Error while parsing feed: " + url, e);
        }
    }

    

    private String buildApiCall(String url) {
        try {
            String encodedQuery = url.replace("http://www.", "").replace("https://www.", "");
            encodedQuery = URLEncoder.encode(encodedQuery, StandardCharsets.UTF_8.name());
            return API_URL + "?q=" + encodedQuery + "&apiKey=" + API_KEY;
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding URL: ", e);
            throw new RuntimeException("Error encoding URL", e);
        }
    }


    private InputStream getRssFeed(String url) {
        logger.info("Generating RSS feed for: {}", url);
        String apiCall = buildApiCall(url);
        InputStream is = feedAdapter.fetchContent(apiCall);
        return is;
    }
}
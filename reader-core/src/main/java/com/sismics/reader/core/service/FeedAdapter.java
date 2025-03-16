package com.sismics.reader.core.service;


import com.sismics.reader.core.service.ApiFeedService;
import com.sismics.reader.core.service.ContentInterface;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FeedAdapter implements ContentInterface {

    private static final Logger logger = LoggerFactory.getLogger(FeedAdapter.class);
    private ApiFeedService apiFeedService;

    private static final String API_URL = "https://newsapi.org/v2/everything";
    private String title = "Generated Feed";
    private String link = "https://newsapi.org/v2/everything";
    private String description = "Latest articles";

    public FeedAdapter(ApiFeedService apiFeedService) {
        this.apiFeedService = apiFeedService;
    }

    @Override
    public InputStream fetchContent(String api_call) {
        Optional<JSONObject> content = apiFeedService.fetchContent(api_call);
        extractMetadataFromURL(api_call);
        return content.map(this::contentToRSS).orElse(null);
    }

    /**
     * Converts the JSON content to an RSS feed.
     */
    private InputStream contentToRSS(JSONObject content) {

        JSONArray articles = content.optJSONArray("articles");
        if (articles == null) return new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));

        StringBuilder rssFeed = new StringBuilder();
        rssFeed.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        rssFeed.append("<rss version=\"2.0\"");
        rssFeed.append("\n xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"");
        rssFeed.append("\n xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\"");
        rssFeed.append("\n xmlns:dc=\"http://purl.org/dc/elements/1.1/\"");
        rssFeed.append("\n xmlns:atom=\"http://www.w3.org/2005/Atom\"");
        rssFeed.append("\n xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\"");
        rssFeed.append("\n xmlns:slash=\"http://purl.org/rss/1.0/modules/slash/\">\n");
        rssFeed.append("<channel>\n");
        rssFeed.append("<title>").append(escapeXml(title)).append("</title>\n");
        rssFeed.append("<link>").append(escapeXml(link)).append("</link>\n");
        rssFeed.append("<description>").append(escapeXml(description)).append("</description>\n");
        rssFeed.append("<language>en-us</language>\n");

        for (int i=0; i<articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            rssFeed.append("<item>\n");
            rssFeed.append("<title>").append(escapeXml(article.optString("title", "No title"))).append("</title>\n");
            rssFeed.append("<link>").append(escapeXml(article.optString("url", "No URL"))).append("</link>\n");
            // rssFeed.append("<guid isPermaLink=\"false\"><![CDATA[").append(article.optString("url", "No URL")).append("]]></guid>\n");
            // rssFeed.append("<comments><![CDATA[").append(article.optString("url", "No Comments URL")).append("]]></comments>\n");
            // rssFeed.append("<slash:comments><![CDATA[0]]></slash:comments>\n");  // Assuming no comments count is available
            rssFeed.append("<dc:creator><![CDATA[").append(escapeXml(article.optString("author", "Unknown"))).append("]]></dc:creator>\n");
            rssFeed.append("<pubDate>").append(escapeXml(article.optString("publishedAt", "No date"))).append("</pubDate>\n");
            // rssFeed.append("<dc:date><![CDATA[").append(article.optString("publishedAt", "No date")).append("]]></dc:date>\n");
            rssFeed.append("<description><![CDATA[").append(escapeXml(article.optString("description", "No description"))).append("]]></description>\n");
            if (article.has("urlToImage")) {
                rssFeed.append("<content:encoded><![CDATA[").append("<img src=").append(escapeXml(article.optString("urlToImage", "Image not available"))).append(" width=\"700\" height=\"400\" alt=\"CloudOps\" />").append(escapeXml(article.optString("content", "Full content unavailable."))).append("]]></content:encoded>\n");
            }
            else {
                rssFeed.append("<content:encoded><![CDATA[").append(escapeXml(article.optString("content", "Full content unavailable."))).append("]]></content:encoded>\n");
            }

            rssFeed.append("</item>\n");
        }

        rssFeed.append("</channel>\n</rss>");
        return new ByteArrayInputStream(rssFeed.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Escapes special XML characters to prevent malformed XML errors.
     */
    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    /**
     * Extracts metadata (title, link, description) from the API URL string.
     */
    private void extractMetadataFromURL(String apiUrl) {
        logger.info("Extracting metadata from API URL: {}", apiUrl);
        
        try {
            this.link = apiUrl; // Use full API URL as the link
            Map<String, String> queryParams = parseQueryParams(apiUrl);

            // Extract meaningful metadata
            String query = queryParams.getOrDefault("q", "Generated Feed");
            this.title = extractDomainName(query);
            this.link = "https://" + this.title;
            // remove .com or .org from title
            if (this.title.contains(".com") || this.title.contains(".org")) {
                this.title = this.title.substring(0, this.title.indexOf("."));
            }
            this.description = "Articles related to " + this.title + " from " + queryParams.getOrDefault("from", "latest date");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses query parameters from the API URL.
     */
    private Map<String, String> parseQueryParams(String url) {
        Map<String, String> queryParams = new HashMap<>();
        if (!url.contains("?")) return queryParams;

        String[] urlParts = url.split("\\?");
        if (urlParts.length < 2) return queryParams;

        String[] params = urlParts[1].split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                try {
                    queryParams.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return queryParams;
    }

    /**
     * Extracts the domain name from the query parameter.
     */
    private String extractDomainName(String query) {
        if (query.contains("/")) {
            String[] parts = query.split("/");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return query;
    }
}
```java
package com.sismics.reader.core.dao.file.json;

import com.google.common.collect.ImmutableList;
import com.sismics.reader.core.event.StarredArticleImportedEvent;
import com.sismics.reader.core.event.StarredArticleImportedListener;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.JsonValidationUtil;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

class StarredReader {
    private static final Logger log = LoggerFactory.getLogger(StarredReader.class);
    public static final int MILLIS_TO_SECONDS_CONVERSION_FACTOR = 1000;
    private static final String ORIGIN_JSON_FIELD = "origin";
    private static final String ITEMS_JSON_FIELD = "items";
    private static final String FEED_RSS_URL_JSON_FIELD = "streamId";
    private static final String FEED_URL_JSON_FIELD = "htmlUrl";
    private static final String FEED_TITLE_JSON_FIELD = "title";
    private static final String ARTICLE_TITLE_JSON_FIELD = "title";
    private static final String ARTICLE_PUBLISHED_TIMESTAMP_JSON_FIELD = "published";
    private static final String ALTERNATE_JSON_FIELD = "alternate";
    private static final String HREF_JSON_FIELD = "href";
    private static final String SUMMARY_JSON_FIELD = "summary";
    private static final String CONTENT_JSON_FIELD = "content";
    private final List<String> mandatoryFields = ImmutableList.of(ORIGIN_JSON_FIELD, ITEMS_JSON_FIELD, FEED_TITLE_JSON_FIELD, ARTICLE_PUBLISHED_TIMESTAMP_JSON_FIELD);
    private StarredArticleImportedListener starredArticleImportedListener;

    public void read(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        JsonNode rootNode = mapper.readTree(is);
        JsonValidationUtil.validateJsonRequiredFields(rootNode, mandatoryFields);
        ArrayNode itemsNode =  (ArrayNode)rootNode.get(ITEMS_JSON_FIELD);
        for (JsonNode itemNode : itemsNode) {
            try {
                StarredNode feedNode = new StarredNode(itemNode, mapper);
                logInfoIfEmpty(feedNode.getFeedTitle(), FEED_TITLE_JSON_FIELD);
                logInfoIfEmpty(feedNode.getFeedUrl(), FEED_URL_JSON_FIELD);
                logInfoIfEmpty(feedNode.getTitle(), ARTICLE_TITLE_JSON_FIELD);
                logInfoIfEmpty(feedNode.getUrl().orElse(null), ALTERNATE_JSON_FIELD + " -> " + HREF_JSON_FIELD);
                logInfoIfEmpty(feedNode.getDescription().orElse(null), SUMMARY_JSON_FIELD + " -> " + CONTENT_JSON_FIELD);
                starredArticleImportedListener.onStarredArticleImported(new StarredArticleImportedEvent(feedNode.toFeed(), feedNode.toArticle()));
            } catch (Exception e) {
                log.error(MessageFormat.format("Failed to parse {0} node: {1}", itemNode.toString(), e.getMessage()));
            }
        }
    }

    private void logInfoIfEmpty(String value, String jsonField) {
        if (value.isEmpty()) {
            log.info(MessageFormat.format("'{0}' not found for starred article", jsonField));
        }
    }

    public void setStarredArticleListener(StarredArticleImportedListener starredArticleListener) {
        this.starredArticleImportedListener = starredArticleListener;
    }

    private static class StarredNode {
        private final String title;
        private final long publishedTimestamp;
        private final Optional<String> url;
        private final Optional<String> description;
        private final Feed feed;

        public StarredNode(JsonNode itemNode, ObjectMapper mapper) throws IOException {
            this.title = JsonValidationUtil.validateJsonAndGetTextValue(itemNode, ARTICLE_TITLE_JSON_FIELD);
            this.publishedTimestamp = JsonValidationUtil.validateJsonAndGetLongValue(itemNode, ARTICLE_PUBLISHED_TIMESTAMP_JSON_FIELD) * MILLIS_TO_SECONDS_CONVERSION_FACTOR;
            this.url = extractUrl(itemNode);
            this.description = extractDescription(itemNode, mapper);
            this.feed = extractFeed(itemNode, mapper);
        }

        private Optional<String> extractUrl(JsonNode itemNode) {
            String result = null;
            if (itemNode.has(ALTERNATE_JSON_FIELD)) {
                ArrayNode alternates = (ArrayNode) itemNode.get(ALTERNATE_JSON_FIELD);
                if (!alternates.isEmpty()) {
                    JsonNode firstAlternate = alternates.get(0);
                    if (firstAlternate.has(HREF_JSON_FIELD)) {
                        result = firstAlternate.get(HREF_JSON_FIELD).getTextValue();
                    }
                }
            }
            return Optional.ofNullable(result);
        }

        private Optional<String> extractDescription(JsonNode itemNode, ObjectMapper mapper) throws IOException {
            if (itemNode.has(SUMMARY_JSON_FIELD)) {
                JsonNode summaryNode = itemNode.get(SUMMARY_JSON_FIELD);
                if (summaryNode.has(CONTENT_JSON_FIELD)) {
                    return Optional.ofNullable(JsonValidationUtil.validateJsonAndGetTextValue(summaryNode, CONTENT_JSON_FIELD));
                }
            }
            if (itemNode.has(CONTENT_JSON_FIELD)) {
                ObjectNode contentNode = (ObjectNode) itemNode.get(CONTENT_JSON_FIELD);
                if (contentNode.has(CONTENT_JSON_FIELD)) {
                    return Optional.ofNullable(JsonValidationUtil.validateJsonAndGetTextValue(contentNode, CONTENT_JSON_FIELD));
                }
            }
            return Optional.empty();
        }

        private Feed extractFeed(JsonNode itemNode, ObjectMapper mapper) throws IOException {
            JsonNode originNode = itemNode.get(ORIGIN_JSON_FIELD);
            Feed feed = new Feed();
            feed.setRssUrl(extractFeedUrl(originNode));
            feed.setTitle(extractFeedTitle(originNode));
            feed.setUrl(extractFeedUrl(originNode));
            return feed;
        }

        private String extractFeedTitle(JsonNode originNode) throws IOException {
            if (originNode.has(FEED_TITLE_JSON_FIELD) && originNode.get(FEED_TITLE_JSON_FIELD).getTextValue() != null) {
                return originNode.get(FEED_TITLE_JSON_FIELD).getTextValue();
            }
            return JsonValidationUtil.extractFieldFromNode(originNode, FEED_RSS_URL_JSON_FIELD);
        }

        private String extractFeedUrl(JsonNode originNode) throws IOException {
            return originNode.get(FEED_RSS_URL_JSON_FIELD).getTextValue();
        }

        public String getTitle() {
            return title;
        }

        public long getPublishedTimestamp() {
            return publishedTimestamp;
        }

        public Optional<String> getUrl() {
            return url;
        }

        public Optional<String> getDescription() {
            return description;
        }

        public Feed getFeed() {
            return feed;
        }

        public Article toArticle() {
            Article article = new Article();
            article.setTitle(title);
            article.setDescription(description.orElse(null));
            article.setPublicationDate(new Date(publishedTimestamp));
            article.setUrl(url.orElse(null));
            return article;
        }

        public Feed toFeed() {
            return feed;
        }
    }
}
```
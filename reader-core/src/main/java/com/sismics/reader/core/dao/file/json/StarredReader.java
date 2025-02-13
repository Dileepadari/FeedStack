```java
package com.sismics.reader.core.dao.file.json;

import com.google.common.collect.ImmutableList;
import com.sismics.reader.core.event.StarredArticleImportedEvent;
import com.sismics.reader.core.event.StarredArticleImportedListener;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.JsonUtil;
import org.codehaus.jackson.JsonFactory;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
class StarredReader implements StarredReaderApi {
    private static final Logger log = LoggerFactory.getLogger(StarredReader.class);
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private JsonParser createJsonParser(InputStream in) {
        try {
            return JSON_FACTORY.createJsonParser(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(InputStream is, StarredArticleImportedListener starredArticleImportedListener) {
        try (JsonParser parser = createJsonParser(is)) {
            this.starredArticleImportedListener = starredArticleImportedListener;
            parse(parser);
        } catch (IOException e) {
            log.error("Unable to parse JSON file {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void parse(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new RuntimeException("Root of JSON must be an object");
        }
        JsonUtil.validateJsonRequiredFields(parser, MANDATORY_FIELDS);
        parser.nextToken(); // Move to "items" array

        if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new RuntimeException("Items should be an array");
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            readStarredItem(parser);
        }
    }

    private void readStarredItem(final JsonParser parser) throws IOException {
        ObjectNode node = parser.readValueAsTree();
        StarredNode starredNode = new StarredNode(node);
        starredArticleImportedListener.onStarredArticleImported(new StarredArticleImportedEvent(starredNode.toFeed(), starredNode.toArticle()));
    }

    private static class StarredNode {
        private final String title;
        private final long publishedTimestamp;
        private final Optional<String> url;
        private final Optional<String> description;
        private final Feed feed;

        public StarredNode(ObjectNode node) throws IOException {
            this.title = JsonUtil.validateJsonAndGetTextValue(node, "title");
            this.publishedTimestamp = Long.parseLong(JsonUtil.validateJsonAndGetTextValue(node, "published")) * 1000;
            this.url = extractUrl(node);
            this.description = extractDescription(node);
            this.feed = extractFeed(node);
        }

        private Optional<String> extractUrl(ObjectNode node) {
            String result = null;
            if (node.has("alternate")) {
                ArrayNode alternates = (ArrayNode) node.get("alternate");
                if (!alternates.isEmpty()) {
                    ObjectNode firstAlternate = (ObjectNode) alternates.get(0);
                    if (firstAlternate.has("href")) {
                        result = firstAlternate.get("href").getTextValue();
                    }
                }
            }
            return Optional.ofNullable(result);
        }

        private Optional<String> extractDescription(ObjectNode node) throws IOException {
            String result = null;
            if (node.has("summary")) {
                ObjectNode summaryNode = (ObjectNode) node.get("summary");
                if (summaryNode.has("content")) {
                    result = JsonUtil.validateJsonAndGetTextValue(summaryNode, "content");
                }
            }
            return Optional.ofNullable(result);
        }

        private Feed extractFeed(ObjectNode node) throws IOException {
            ObjectNode originNode = (ObjectNode) node.get("origin");
            Feed feed = new Feed();
            feed.setRssUrl(extractFeedUrl(originNode));
            feed.setTitle(extractFeedTitle(originNode));
            feed.setUrl(extractFeedUrl(originNode));
            return feed;
        }

        private String extractFeedTitle(ObjectNode originNode) throws IOException {
            return JsonUtil.extractFieldFromNode(originNode, "title");
        }

        private String extractFeedUrl(ObjectNode originNode) throws IOException {
            return JsonUtil.extractFieldFromNode(originNode, "htmlUrl");
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
====FILE_DELIMITER====
```java
package com.sismics.util;

import com.codehaus.jackson.JsonNode;
import com.codehaus.jackson.JsonParseException;
import com.codehaus.jackson.JsonParser;
import com.codehaus.jackson.map.DeserializationContext;
import com.codehaus.jackson.map.JsonDeserializer;
import com.codehaus.jackson.map.JsonMappingException;
import com.codehaus.jackson.map.ObjectMapper;
import com.codehaus.jackson.map.annotate.JsonDeserialize;
import com.codehaus.jackson.map.annotate.JsonSerialize;
import com.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private JsonUtil() {
        throw new AssertionError("Not instantiable");
    }

    public static String validateJsonAndGetTextValue(JsonNode node, String fieldName) throws JsonParseException {
        if (!node.has(fieldName)) {
            throw new JsonParseException(String.format("Missing required field '%s'", fieldName), null);
        }
        JsonNode fieldNode = node.get(fieldName);
        if (!fieldNode.isTextual()) {
            throw new JsonParseException(String.format("Field '%s' must be a string", fieldName), null);
        }
        return fieldNode.getTextValue();
    }

    public static void validateJsonRequiredFields(JsonParser parser, List<String> requiredFields) throws JsonParseException, IOException {
        for (String field : requiredFields) {
            if (!parser.hasCurrentToken() && parser.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw new JsonParseException(String.format("Missing required field '%s'", field), null);
            }
            if (!parser.getCurrentName().equalsIgnoreCase(field)) {
                throw new JsonParseException(String.format("Expected field '%s' not found", field), null);
            }
            parser.nextToken();
            if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
                throw new JsonParseException(String.format("Field '%s' cannot be null", field), null);
            }
        }
    }

    public static String extractFieldFromNode(JsonNode node, String fieldName) throws IOException {
        JsonNode fieldNode = node.get(fieldName);
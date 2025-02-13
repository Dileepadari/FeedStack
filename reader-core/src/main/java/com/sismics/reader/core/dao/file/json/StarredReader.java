```java
package com.sismics.reader.core.dao.file.json;

import com.google.common.collect.ImmutableList;
import com.sismics.reader.core.event.StarredArticleImportedEvent;
import com.sismics.reader.core.event.StarredArticleImportedListener;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.JsonUtil;
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

class StarredReader {
    private static final Logger log = LoggerFactory.getLogger(StarredReader.class);
    private static final List<String> MANDATORY_FIELDS = ImmutableList.of("origin", "items", "feed.title", "published");
    private StarredArticleImportedListener starredArticleImportedListener;

    public void read(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        JsonNode rootNode = mapper.readTree(is);

        JsonUtil.validateJsonRequiredFields(rootNode, MANDATORY_FIELDS);
        ArrayNode itemsNode = (ArrayNode) rootNode.get("items");

        for (JsonNode itemNode : itemsNode) {
            try {
                StarredNode feedNode = new StarredNode(itemNode, mapper);

                starredArticleImportedListener.onStarredArticleImported(new StarredArticleImportedEvent(feedNode.toFeed(), feedNode.toArticle()));
            } catch (Exception e) {
                log.error(MessageFormat.format("Failed to parse {0} node: {1}", itemNode.toString(), e.getMessage()));
            }
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
            this.title = JsonUtil.validateJsonAndGetTextValue(itemNode, "title");
            this.publishedTimestamp = Long.parseLong(JsonUtil.validateJsonAndGetTextValue(itemNode, "published")) * 1000;
            this.url = extractUrl(itemNode);
            this.description = extractDescription(itemNode, mapper);
            this.feed = extractFeed(itemNode, mapper);
        }

        private Optional<String> extractUrl(JsonNode itemNode) {
            String result = null;
            if (itemNode.has("alternate")) {
                ArrayNode alternates = (ArrayNode) itemNode.get("alternate");
                if (!alternates.isEmpty()) {
                    JsonNode firstAlternate = alternates.get(0);
                    if (firstAlternate.has("href")) {
                        result = firstAlternate.get("href").getTextValue();
                    }
                }
            }
            return Optional.ofNullable(result);
        }

        private Optional<String> extractDescription(JsonNode itemNode, ObjectMapper mapper) throws IOException {
            String result = null;
            if (itemNode.has("summary")) {
                JsonNode summaryNode = itemNode.get("summary");
                if (summaryNode.has("content")) {
                    result = JsonUtil.validateJsonAndGetTextValue(summaryNode, "content");
                }
            }
            return Optional.ofNullable(result);
        }

        private Feed extractFeed(JsonNode itemNode, ObjectMapper mapper) throws IOException {
            JsonNode originNode = itemNode.get("origin");
            Feed feed = new Feed();
            feed.setRssUrl(extractFeedUrl(originNode));
            feed.setTitle(extractFeedTitle(originNode));
            feed.setUrl(extractFeedUrl(originNode));
            return feed;
        }

        private String extractFeedTitle(JsonNode originNode) throws IOException {
            return JsonUtil.extractFieldFromNode(originNode, "title");
        }

        private String extractFeedUrl(JsonNode originNode) throws IOException {
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

====FILE_DELIMITER====
package com.sismics.util;

import com.codehaus.jackson.JsonNode;
import com.codehaus.jackson.JsonParseException;
import com.codehaus.jackson.JsonParser;
import com.codehaus.jackson.map.DeserializationContext;
import com.codehaus.jackson.map.JsonDeserializer;
import com.codehaus.jackson.map.JsonMappingException;
import com.codehaus.jackson.map.ObjectMapper;
import com.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

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

    private static ObjectMapper newMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        return mapper;
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

    public static void validateJsonRequiredFields(JsonNode node, List<String> requiredFields) throws JsonParseException {
        for (String field : requiredFields) {
            if (!node.has(field)) {
                throw new JsonParseException(String.format("Missing required field '%s'", field), null);
            }
        }
    }

    public static String extractFieldFromNode(JsonNode node, String fieldName) throws IOException {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode != null) {
            if (!fieldNode.isTextual()) {
                throw new IOException(MessageFormat.format("Field {0} is not a string", fieldName));
            }
            return fieldNode.getTextValue();
        }
        return null;
    }

    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    public static class Iso8601Date {
        private final Date date;

        public Iso8601Date(Date date) {
            this.date = date;
        }

        public Date getDate() {
            return date;
        }

        public static class DateDeserializer extends JsonDeserializer<Iso8601Date> {
            @Override
            public Iso8601Date deserialize(JsonParser jp, Des
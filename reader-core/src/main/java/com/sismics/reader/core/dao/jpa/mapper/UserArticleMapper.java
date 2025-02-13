package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.CommentDto;
import com.sismics.reader.core.dao.jpa.dto.EnclosureDto;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.util.jpa.ResultMapper;

import java.sql.Timestamp;

/**
 * @author jtremeaux
 */
public class UserArticleMapper extends ResultMapper<UserArticleDto> {
    @Override
    public UserArticleDto map(Object[] o) {
        int i = 0;
        UserArticleDto dto = new UserArticleDto();
        dto.setId(stringValue(o[i++]));
        Timestamp readTimestamp = (Timestamp) o[i++];
        if (readTimestamp != null) {
            dto.setReadTimestamp(readTimestamp.getTime());
        }
        Timestamp starTimestamp = (Timestamp) o[i++];
        if (starTimestamp != null) {
            dto.setStarTimestamp(starTimestamp.getTime());
        }
        dto.setFeedTitle(stringValue(o[i++]));
        dto.setFeedSubscriptionId(stringValue(o[i++]));
        dto.setFeedSubscriptionTitle(stringValue(o[i++]));
        ArticleDto article = new ArticleDto();
        article.setId(stringValue(o[i++]));
        article.setUrl(stringValue(o[i++]));
        article.setGuid(stringValue(o[i++]));
        article.setTitle(stringValue(o[i++]));
        article.setCreator(stringValue(o[i++]));
        article.setDescription(stringValue(o[i++]));
        CommentDto comment = new CommentDto();
        comment.setUrl(stringValue(o[i++]));
        comment.setCount(intValue(o[i++]));
        article.setComment(comment);
        EnclosureDto enclosure = new EnclosureDto();
        enclosure.setUrl(stringValue(o[i++]));
        enclosure.setCount(intValue(o[i++]));
        enclosure.setType(stringValue(o[i++]));
        article.setEnclosure(enclosure);
        article.setPublicationDate(readTimestamp);
        article.setCreateDate(readTimestamp);
        dto.setArticle(article);
        dto.setArticlePublicationTimestamp(((Timestamp) o[i]).getTime());

        return dto;
    }
}

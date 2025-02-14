package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.dto.CategoryDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class FeedSubscriptionMapper extends ResultMapper<FeedSubscriptionDto> {
    @Override
    public FeedSubscriptionDto map(Object[] o) {
        int i = 0;
        FeedSubscriptionDto dto = new FeedSubscriptionDto();
        dto.setId(stringValue(o[i++]));
        String feedSubscriptionTitle = stringValue(o[i++]);
        dto.setUnreadUserArticleCount(intValue(o[i++]));
        dto.setCreateDate(dateValue(o[i++]));
        dto.setUserId(stringValue(o[i++]));
        FeedDto feed = new FeedDto();
        feed.setId(stringValue(o[i++]));
        String feedTitle = stringValue(o[i++]);
        dto.setFeedSubscriptionTitle(feedSubscriptionTitle != null ? feedSubscriptionTitle : feedTitle);
        feed.setTitle(feedTitle);
        feed.setRssUrl(stringValue(o[i++]));
        feed.setUrl(stringValue(o[i++]));
        feed.setDescription(stringValue(o[i++]));
        dto.setFeed(feed);
        CategoryDto category = new CategoryDto();
        category.setId(stringValue(o[i++]));
        category.setParentId(stringValue(o[i++]));
        category.setName(stringValue(o[i++]));
        Boolean folded = booleanValue(o[i++]);
        category.setFolded(folded);
        dto.setCategory(category);
        dto.setSynchronizationFailCount(((Number) o[i]).intValue());

        return dto;
    }
}

package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.EnclosureDto;
import com.sismics.reader.core.dao.jpa.dto.CommentDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class ArticleMapper extends ResultMapper<ArticleDto> {
    @Override
    public ArticleDto map(Object[] o) {
        int i = 0;
        ArticleDto dto = new ArticleDto();
        dto.setId(stringValue(o[i++]));
        dto.setUrl(stringValue(o[i++]));
        dto.setGuid(stringValue(o[i++]));
        dto.setTitle(stringValue(o[i++]));
        dto.setCreator(stringValue(o[i++]));
        dto.setDescription(stringValue(o[i++]));
        CommentDto commentDto = new CommentDto();
        commentDto.setUrl(stringValue(o[i++]));
        commentDto.setCount(intValue(o[i++]));
        dto.setComment(commentDto);
        EnclosureDto enclosureDto = new EnclosureDto();
        enclosureDto.setUrl(stringValue(o[i++]));
        enclosureDto.setCount(intValue(o[i++]));
        enclosureDto.setType(stringValue(o[i++]));
        dto.setEnclosure(enclosureDto);
        dto.setPublicationDate(dateValue(o[i++]));
        dto.setCreateDate(dateValue(o[i++]));
        dto.setFeedId(stringValue(o[i]));

        return dto;
    }
}

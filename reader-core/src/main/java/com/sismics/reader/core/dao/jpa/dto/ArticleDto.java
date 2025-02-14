package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * Article DTO.
 *
 * @author jtremeaux
 */
public class ArticleDto {
    /**
     * Article ID.
     */
    private String id;

    /**
     * Article URL.
     */
    private String url;

    /**
     * Article GUID.
     */
    private String guid;

    /**
     * Article title.
     */
    private String title;

    /**
     * Article creator.
     */
    private String creator;

    /**
     * Article description.
     */
    private String description;

    /**
     * Comment Dto.
     */
    private CommentDto comment;

    /**
     * Enclosure Dto.
     */
    private EnclosureDto enclosure;

    /**
     * Publication date.
     */
    private Date publicationDate;

    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Feed ID.
     */
    private String feedId;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of url.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter of url.
     *
     * @param url url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter of guid.
     *
     * @return guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Setter of guid.
     *
     * @param guid guid
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Getter of title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of title.
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter of creator.
     *
     * @return creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Setter of creator.
     *
     * @param creator creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Getter of description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter of description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter of comment.
     *
     * @return comment
     */
    public CommentDto getComment() {
        return comment;
    }

    /**
     * Setter of comment.
     *
     * @param comment comment
     */
    public void setComment(CommentDto comment) {
        this.comment = comment;
    }

    /**
     * Getter of publicationDate.
     *
     * @return publicationDate
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    /**
     * Setter of publicationDate.
     *
     * @param publicationDate publicationDate
     */
    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of enclosure.
     *
     * @return enclosure
     */
    public EnclosureDto getEnclosure() {
        return enclosure;
    }

    /**
     * Setter of enclosure.
     *
     * @param enclosure enclosure
     */
    public void setEnclosure(EnclosureDto enclosure) {
        this.enclosure = enclosure;
    }

    /**
     * Getter of feedId.
     *
     * @return feedId
     */
    public String getFeedId() {
        return feedId;
    }

    /**
     * Setter of feedId.
     *
     * @param feedId feedId
     */
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }
}

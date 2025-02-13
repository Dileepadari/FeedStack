package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.Date;

/**
 * Article entity.
 *
 * @author jtremeaux
 */
@Entity
@Table(name = "T_ARTICLE")
public class Article {
    /**
     * Article ID.
     */
    @Id
    @Column(name = "ART_ID_C", length = 36)
    private String id;

    /**
     * Feed ID.
     */
    @Column(name = "ART_IDFEED_C", nullable = false, length = 36)
    private String feedId;

    /**
     * Article URL.
     */
    @Column(name = "ART_URL_C", length = 2000)
    private String url;

    /**
     * Relative URI (Atom).
     */
    @Column(name = "ART_BASEURI_C", length = 2000)
    private String baseUri;

    /**
     * Article GUID.
     */
    @Column(name = "ART_GUID_C", nullable = false, length = 2000)
    private String guid;

    /**
     * Article title.
     */
    @Column(name = "ART_TITLE_C", length = 4000)
    private String title;

    /**
     * Article creator.
     */
    @Column(name = "ART_CREATOR_C", length = 200)
    private String creator;

    /**
     * Article description.
     */
    @Lob
    @Column(name = "ART_DESCRIPTION_C")
    private String description;

    /**
     * Comment URL.
     */
    @Column(name = "ART_COMMENTURL_C", length = 2000)
    private String commentUrl;

    /**
     * Comment count.
     */
    @Column(name = "ART_COMMENTCOUNT_N")
    private Integer commentCount;

    /**
     * Enclosure URL.
     */
    @Column(name = "ART_ENCLOSUREURL_C", length = 2000)
    private String enclosureUrl;

    /**
     * Enclosure length in bytes.
     */
    @Column(name = "ART_ENCLOSURELENGTH_N")
    private Integer enclosureLength;

    /**
     * Enclosure MIME type.
     */
    @Column(name = "ART_ENCLOSURETYPE_C", length = 2000)
    private String enclosureType;

    /**
     * Publication date.
     */
    @Column(name = "ART_PUBLICATIONDATE_D", nullable = false)
    private Date publicationDate;

    /**
     * Creation date.
     */
    @Column(name = "ART_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "ART_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommentUrl() {
        return commentUrl;
    }

    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    public Integer getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Integer enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    public String getEnclosureType() {
        return enclosureType;
    }

    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("url", url)
                .toString();
    }

    private void cloneArticleId(Article other) {
        setId(other.getId());
    }
}

package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Authentication token entity.
 *
 * @author jtremeaux
 */
@Entity
@Table(name = "T_AUTHENTICATION_TOKEN")
public class AuthenticationToken {
    /**
     * Token.
     */
    @Id
    @Column(name = "AUT_ID_C", length = 36)
    private String id;

    /**
     * User ID.
     */
    @Column(name = "AUT_IDUSER_C", nullable = false, length = 36)
    private String userId;

    /**
     * Remember the user next time (long lasted session).
     */
    @Column(name = "AUT_LONGLASTED_B", nullable = false)
    private boolean longLasted;

    /**
     * Token creation date.
     */
    @Column(name = "AUT_CREATIONDATE_D", nullable = false)
    private Date creationDate;

    /**
     * Last connection date using this token.
     */
    @Column(name = "AUT_LASTCONNECTIONDATE_D")
    private Date lastConnectionDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLongLasted() {
        return longLasted;
    }

    public void setLongLasted(boolean longLasted) {
        this.longLasted = longLasted;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(Date lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", "**hidden**")
                .add("userId", userId)
                .add("longLasted", longLasted)
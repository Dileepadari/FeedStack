package com.sismics.reader.core.dao.jpa.dto;

/**
 * Comment DTO.
 */
public class CommentDto {
    /**
     * Comment URL.
     */
    private String url;

    /**
     * Comment count.
     */
    private Integer count;

    /**
     * Getter of commentUrl.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter of commentUrl.
     *
     * @param url commentUrl
     */

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter of commentCount.
     *
     * @return commentCount
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Setter of commentCount.
     *
     * @param count commentCount
     */
    public void setCount(Integer count) {
        this.count = count;
    }

}

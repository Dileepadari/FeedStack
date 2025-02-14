package com.sismics.reader.core.dao.jpa.dto;

/**
 * Enclosure DTO.
 */
public class EnclosureDto {
    /**
     * Enclosure URL.
     */
    private String url;

    /**
     * Enclosure size in bytes.
     */
    private Integer count;

    /**
     * Enclosure MIME type.
     */
    private String type;

    /**
     * Getter of url.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * setter of url.
     * 
     * @return
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter of size.
     *
     * @return size
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Setter of size.
     *
     * @param size
     * @return
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Getter of type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter of type.
     *
     * @param type
     * @return
     */
    public void setType(String type) {
        this.type = type;
    }
}
package com.sismics.reader.core.dao.jpa.dto;

public class CategoryDto {
    /**
     * Category ID.
     */
    private String id;

    /**
     * Category parent Id.
     */
    private String parentId;

    /**
     * Category name.
     */
    private String name;

    /**
     * True if this category is folded in the subscriptions tree.
     */
    private boolean isFolded;

    /**
     * Getter of id.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of parentId.
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Setter of parentId.
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Getter of name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter of isFolded.
     */
    public boolean isFolded() {
        return isFolded;
    }

    /**
     * Setter of isFolded.
     */
    public void setFolded(boolean isFolded) {
        this.isFolded = isFolded;
    }
}

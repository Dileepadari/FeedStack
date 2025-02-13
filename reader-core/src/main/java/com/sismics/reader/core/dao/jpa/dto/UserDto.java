UserDto.java
====FILE_DELIMITER====
package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * User DTO.
 *
 * @author jtremeaux
 */
public class UserDto {
    /**
     * User ID.
     */
    private String id;

    /**
     * Locale ID.
     */
    private String localeId;

    /**
     * Username.
     */
    private String username;

    /**
     * Email address.
     */
    private String email;

    /**
     * Creation date of this user.
     */
    private Date createTimestamp;

    // getters and setters

}
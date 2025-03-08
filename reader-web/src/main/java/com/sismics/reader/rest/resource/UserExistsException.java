package com.sismics.reader.rest.resource;

public class UserExistsException extends Exception {
    public UserExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.sismics.reader.rest.resource.validation;

public class RegistrationRequest {
    private String username;
    private String password;
    private String email;
    private String localeId;

    // Constructor
    public RegistrationRequest(String username, String password, String email, String localeId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.localeId = localeId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getLocaleId() {
        return localeId;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

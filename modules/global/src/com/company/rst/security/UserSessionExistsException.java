package com.company.rst.security;

import com.haulmont.cuba.security.global.LoginException;

public class UserSessionExistsException extends LoginException{
    protected String login;

    public UserSessionExistsException(String login) {
        super("User session exists");
        this.login = login;
    }
}

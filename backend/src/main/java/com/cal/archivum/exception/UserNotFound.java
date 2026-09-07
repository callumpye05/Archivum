package com.cal.archivum.exception;

public class UserNotFound extends RuntimeException {
    public UserNotFound(String userOrEmail) {

        super("User not found with username/email : " + userOrEmail);
    }
}

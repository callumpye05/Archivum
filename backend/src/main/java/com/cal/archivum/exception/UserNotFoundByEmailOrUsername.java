package com.cal.archivum.exception;

public class UserNotFoundByEmailOrUsername extends RuntimeException {
    public UserNotFoundByEmailOrUsername(String userOrEmail) {

        super("User not found with username/email : " + userOrEmail);
    }
}

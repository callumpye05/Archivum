package com.cal.archivum.exception;

public class UserNotFound extends RuntimeException {
    public UserNotFound(Long userId) {


        super("No User was found with the id : " + userId);
    }
}

package com.cal.archivum.exception;

public class WorldNotFound extends RuntimeException {


    public WorldNotFound(Long id) {
        super("The world with the id '"+ id + "' was not found");
    }
}

package com.cal.archivum.exception;

public class LocationNotFound extends RuntimeException {
    public LocationNotFound(Long id) {

        super("Location with id '"+ id + "' was not found");
    }
}

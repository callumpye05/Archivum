package com.cal.archivum.exception;

public class LocationNotFoundByWorld extends RuntimeException {
    public LocationNotFoundByWorld(Long locationId , Long worldId) {
        super("Location wasn't found using location id : " +locationId+" and world id : "+worldId);
    }
}

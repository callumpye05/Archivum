package com.cal.archivum.exception;

public class CharacterNotFoundByWorld extends RuntimeException {
    public CharacterNotFoundByWorld(Long characterId , Long worldId) {
        super("Character wasn't found using character id : " + characterId +" and world id : "+worldId);

    }
}

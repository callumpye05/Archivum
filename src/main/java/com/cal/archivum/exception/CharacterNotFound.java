package com.cal.archivum.exception;

public class CharacterNotFound extends RuntimeException {
    public CharacterNotFound(Long id)
    {
        super("Character with id '"+ id + "' was not found");
    }
}

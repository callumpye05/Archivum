package com.cal.archivum.exception;

import com.cal.archivum.dto.impl.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ErrorResponseDto build_error(String apiPath , HttpStatus error , String errorMessage , LocalDateTime time) {
        return new ErrorResponseDto(apiPath , error.getReasonPhrase() ,error.value() ,errorMessage ,time);
    }

    //404 NOT FOUND
    @ExceptionHandler({

            WorldNotFound.class ,
            CharacterNotFound.class,
            LocationNotFound.class ,
            CharacterNotFoundByWorld.class,
            LocationNotFoundByWorld.class,
            UserNotFound.class ,
            UserNotFoundByEmailOrUsername.class,
            EmailVerificationTokenNotFound.class
    })
    public ResponseEntity<ErrorResponseDto> handleNotFound(RuntimeException ex , WebRequest webRequest) {
        ErrorResponseDto buildError =build_error(webRequest.getDescription(false) , HttpStatus.NOT_FOUND, ex.getMessage() , LocalDateTime.now());
        return new ResponseEntity<>(buildError , HttpStatus.NOT_FOUND);
    }

    //409 CONFLICT
    @ExceptionHandler({
            UsernameAlreadyUsed.class,
            EmailAlreadyUsed.class
    })
    public ResponseEntity<ErrorResponseDto> handleConflict(RuntimeException ex, WebRequest webRequest) {
        ErrorResponseDto buildError = build_error(webRequest.getDescription(false), HttpStatus.CONFLICT, ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(buildError,HttpStatus.CONFLICT);
    }

    //400 BAD REQUEST
    @ExceptionHandler({
            EmailVerificationTokenExpired.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequest(RuntimeException ex, WebRequest webRequest) {
        ErrorResponseDto buildError=build_error(webRequest.getDescription(false), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(buildError,HttpStatus.BAD_REQUEST);
    }

    //403 FORBIDDEN
    @ExceptionHandler({
            EmailNotVerified.class
    })
    public ResponseEntity<ErrorResponseDto> handleForbidden(RuntimeException ex, WebRequest webRequest) {
        ErrorResponseDto buildError= build_error(webRequest.getDescription(false), HttpStatus.FORBIDDEN, ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(buildError, HttpStatus.FORBIDDEN);
    }

    //400 bad request but for invalid DTO's
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleNotValid(MethodArgumentNotValidException ex , WebRequest webRequest) {
        String message=ex.getBindingResult().getFieldErrors().stream().map(error-> error.getDefaultMessage()).collect(Collectors.joining(", "));
        ErrorResponseDto buildError= build_error(webRequest.getDescription(false), HttpStatus.BAD_REQUEST, message, LocalDateTime.now());
        return new ResponseEntity<>(buildError , HttpStatus.BAD_REQUEST);
    }




}

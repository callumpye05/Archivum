package com.cal.archivum.dto.impl;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponseDto(String apiPath, String statusText , int errorCode , String errorMessage , LocalDateTime errorTime) {

}

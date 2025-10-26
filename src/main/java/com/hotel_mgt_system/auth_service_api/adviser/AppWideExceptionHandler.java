package com.hotel_mgt_system.auth_service_api.adviser;


import com.hotel_mgt_system.auth_service_api.exception.EntryNotFoundException;
import com.hotel_mgt_system.auth_service_api.exception.UnAuthorizedException;
import com.hotel_mgt_system.auth_service_api.util.StandardResponseDto;
import jakarta.ws.rs.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppWideExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardResponseDto> handleBadRequestException(BadRequestException e) {
        return new ResponseEntity(
                new StandardResponseDto(400, e.getMessage(), e),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<StandardResponseDto> handleEntryNotFoundException(Exception e) {
        return new ResponseEntity(
                new StandardResponseDto(404, e.getMessage(), e),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<StandardResponseDto> handleUnAuthorizedException(Exception e) {
        return new ResponseEntity(
                new StandardResponseDto(401, e.getMessage(), e),
                HttpStatus.NOT_FOUND
        );
    }


}

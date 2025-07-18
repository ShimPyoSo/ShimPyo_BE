package com.example.shimpyo.global;

import com.example.shimpyo.global.exceptionType.ExceptionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ExceptionType exceptionType = e.getExceptionType();
        return ResponseEntity.status(exceptionType.httpStatus())
                .body(ErrorResponse.of(exceptionType));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid (MethodArgumentNotValidException e) {

        log.warn("handleMethodArgumentNotValidException : {}", "Invalid Arguments");
        String errMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(errMsg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Unhandled Error] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .name(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("서버 에러입니다.")
                        .build());
    }
}

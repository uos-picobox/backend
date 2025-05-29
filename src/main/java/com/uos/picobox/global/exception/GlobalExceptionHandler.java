package com.uos.picobox.global.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static class ErrorResponse {
        private final LocalDateTime timestamp = LocalDateTime.now();
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private Map<String, String> validationErrors;

        public ErrorResponse(HttpStatus status, String message, String path) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.path = path;
        }

        public ErrorResponse(HttpStatus status, String customMessage, Map<String, String> validationErrors, String path) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = customMessage;
            this.validationErrors = validationErrors;
            this.path = path;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public Map<String, String> getValidationErrors() { return validationErrors; }
    }

    // @Valid 유효성 검사 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String requestPath = request.getDescription(false).replace("uri=", "");
        log.warn("Validation error for request path [{}]: {}", requestPath, errors);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "입력값 유효성 검사에 실패했습니다.",
                errors,
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // @Validated 유효성 검사 실패 시
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                       WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getAllErrors().forEach((error) -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put("nonField", error.getDefaultMessage());
            }

        });

        String requestPath = request.getDescription(false).replace("uri=", "");
        log.warn("Validation error for request path [{}]: {}", requestPath, errors);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "입력값 유효성 검사에 실패했습니다.",
                errors,
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.warn("Illegal argument for request path [{}]: {}", requestPath, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // NOT_FOUND
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex,
                                                              WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.warn("Entity not found for request path [{}]: {}", requestPath, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 기타 모든 처리되지 않은 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex,
                                                                    WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.error("Unhandled exception for request path [{}]: {}", requestPath, ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.",
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
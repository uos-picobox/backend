package com.uos.picobox.global.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
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

    // DataIntegrityViolationException 처리 (FK 제약 조건 위반 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        String message = "데이터 무결성 제약 조건 위반입니다. 요청을 확인해주세요.";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            if (ex.getCause().getMessage().contains("ORA-02292")) { // Oracle FK 위반
                message = "다른 데이터에서 참조하고 있어 작업을 완료할 수 없습니다. (예: 하위 레코드가 존재함)";
            }
            // 다른 DB 에러 코드에 대한 처리 추가 가능
        }
        log.warn("Data integrity violation for request path [{}]: {}", requestPath, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
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

    // 409 Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.warn("Illegal state for request path [{}]: {}", requestPath, ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 파일 업로드 크기 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.warn("Max upload size exceeded for request path [{}]: {}", requestPath, ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "업로드 파일 크기가 너무 큽니다.",
                requestPath);
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // 파일 처리 관련 IOException (S3 업로드/다운로드 중 파일 스트림 오류 등)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.error("IOException occurred for request path [{}]: {}", requestPath, ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "파일 처리 중 오류가 발생했습니다. 다시 시도해주세요.",
                requestPath
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // AWS S3 관련 예외 처리 (S3 서비스에서 직접 발생하는 예외)
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponse> handleS3Exception(S3Exception ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.error("S3Exception for request path [{}]: {} (Status Code: {}, AWS Error Code: {})",
                requestPath, ex.getMessage(), ex.statusCode(), ex.awsErrorDetails().errorCode(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "S3 서비스 처리 중 오류가 발생했습니다.";

        if (ex.statusCode() == 403) {
            status = HttpStatus.FORBIDDEN;
            message = "S3 서비스 접근 권한이 없습니다. 설정을 확인해주세요.";
        } else if (ex.statusCode() == 404) {
            status = HttpStatus.NOT_FOUND;
            message = "S3 서비스에서 요청한 리소스를 찾을 수 없습니다 (예: 버킷 또는 객체).";
        }

        ErrorResponse errorResponse = new ErrorResponse(status, message, requestPath);
        return new ResponseEntity<>(errorResponse, status);
    }

    // AWS SDK 일반 예외 처리
    @ExceptionHandler(SdkException.class)
    public ResponseEntity<ErrorResponse> handleSdkException(SdkException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        log.error("AWS SDK SdkException for request path [{}]: {}", requestPath, ex.getMessage(), ex);

        String message = "외부 서비스(AWS) 통신 중 오류가 발생했습니다.";
        if (ex instanceof SdkClientException) {
            message = "외부 서비스(AWS) 통신 중 클라이언트 측 오류(예: 네트워크)가 발생했습니다.";
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, message, requestPath);
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
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
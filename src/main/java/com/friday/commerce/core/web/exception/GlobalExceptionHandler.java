package com.friday.commerce.core.web.exception;

import com.friday.commerce.core.web.response.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 도메인 단건 예외
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleApp(AppException ex) {
        var code = ex.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(ApiErrorResponse.of(code.getStatus(), code.getMessage()));
    }

    // @Valid 바인딩 실패 → 여러 필드 오류를 data에
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalid(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> ApiErrorResponse.FieldError.of(err.getField(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST,
                        AppErrorCode.INVALID_INPUT_VALUE.getMessage(),
                        errors));
    }

    // Validator 직접 사용 시 (여러 필드)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(v -> ApiErrorResponse.FieldError.of(v.getPropertyPath().toString(),
                        v.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST,
                        AppErrorCode.INVALID_INPUT_VALUE.getMessage(),
                        errors));
    }

    // 잘못된 JSON 본문
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST,
                        AppErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    // 허용되지 않은 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED,
                        AppErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    // JPA 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(HttpStatus.NOT_FOUND,
                        AppErrorCode.ENTITY_NOT_FOUND.getMessage()));
    }

    // 컨버터가 바디를 못 쓰는 경우 (대부분 직렬화 실패) → 500
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotWritableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotWritable(org.springframework.http.converter.HttpMessageNotWritableException ex) {
        log.error("응답 직렬화 실패: {}", rootCauseMessage(ex), ex);
        return ResponseEntity.status(AppErrorCode.RESP_BODY_WRITE_ERROR.getStatus())
                .body(ApiErrorResponse.of(
                        AppErrorCode.RESP_BODY_WRITE_ERROR.getStatus(),
                        AppErrorCode.RESP_BODY_WRITE_ERROR.getMessage()
                ));
    }

    // 콘텐츠 협상 실패(예: 지원 미디어타입 없음) → 406
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAcceptable(org.springframework.web.HttpMediaTypeNotAcceptableException ex) {
        log.warn("콘텐츠 협상 실패(406): {}", rootCauseMessage(ex));
        return ResponseEntity.status(AppErrorCode.NOT_ACCEPTABLE.getStatus())
                .body(ApiErrorResponse.of(
                        AppErrorCode.NOT_ACCEPTABLE.getStatus(),
                        AppErrorCode.NOT_ACCEPTABLE.getMessage()
                ));
    }

    // 클라이언트가 지원 안하는 Content-Type로 보낸 경우 → 415 (선택)
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupported(org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        log.warn("미지원 콘텐츠 타입(415): {}", rootCauseMessage(ex));
        return ResponseEntity.status(AppErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus())
                .body(ApiErrorResponse.of(
                        AppErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus(),
                        AppErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage()
                ));
    }

    // 500 안전망: 마지막 발동 Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex) {
        log.error("처리되지 않은 예외: {}", rootCauseMessage(ex), ex);
        return ResponseEntity.status(AppErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiErrorResponse.of(
                        AppErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                        AppErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }

    // private helpers
    private static String rootCauseMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return c.getMessage();
    }
}

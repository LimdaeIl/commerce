package com.friday.commerce.core.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.friday.commerce.core.web.exception.AppException;
import com.friday.commerce.core.web.exception.AppErrorCode;
import com.friday.commerce.core.web.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "ExceptionHandlingFilter")
@RequiredArgsConstructor
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true; // ERROR 디스패치 중복 방지
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true; // (선택) 비동기 디스패치 중복 방지
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AppException ex) {
            writeAppError(response, request, ex);
        } catch (Throwable t) { // 마지막 방어막
            log.error("Unhandled throwable", t);
            writeGenericError(response, request);
        }
    }

    private void writeAppError(HttpServletResponse resp, HttpServletRequest req, AppException ex)
            throws IOException {
        if (resp.isCommitted()) return;

        ErrorCode code = ex.getErrorCode();
        HttpStatus status = (code != null && code.getStatus() != null)
                ? code.getStatus()
                : HttpStatus.BAD_REQUEST;

        String message = (code != null && StringUtils.hasText(code.getMessage()))
                ? code.getMessage()
                : (StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : status.getReasonPhrase());

        logAtProperLevel(code, ex);

        String codeName = (code instanceof Enum<?> e) ? e.name()
                : (code != null ? code.getClass().getSimpleName() : "APP_ERROR");

        resp.resetBuffer();
        resp.setStatus(status.value());
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "code", codeName,
                "message", message,
                "path", req.getRequestURI()
        );
        objectMapper.writeValue(resp.getWriter(), body);
        resp.flushBuffer();
    }

    private void writeGenericError(HttpServletResponse resp, HttpServletRequest req)
            throws IOException {
        if (resp.isCommitted()) return;

        HttpStatus status = AppErrorCode.INTERNAL_SERVER_ERROR.getStatus();
        String message = AppErrorCode.INTERNAL_SERVER_ERROR.getMessage();

        resp.resetBuffer();
        resp.setStatus(status.value());
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "code", AppErrorCode.INTERNAL_SERVER_ERROR.name(),
                "message", message,
                "path", req.getRequestURI()
        );
        resp.getWriter().write(new ObjectMapper().writeValueAsString(body));
        resp.flushBuffer();
    }

    private void logAtProperLevel(ErrorCode code, AppException ex) {
        HttpStatus status = (code != null) ? code.getStatus() : null;
        if (status == null) {
            log.error("AppException without status", ex);
            return;
        }
        if (status.is4xxClientError()) {
            log.warn("Handled AppException: {} ({})", codeName(code), status, ex);
        } else {
            log.error("Handled AppException: {} ({})", codeName(code), status, ex);
        }
    }

    private String codeName(ErrorCode code) {
        return (code instanceof Enum<?> e) ? e.name()
                : (code != null ? code.getClass().getSimpleName() : "APP_ERROR");
    }
}

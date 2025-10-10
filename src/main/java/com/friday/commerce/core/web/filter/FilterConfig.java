package com.friday.commerce.core.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.friday.commerce.core.security.jwt.JwtProvider;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 필터 체인 구성
 *
 * <p> [순서]
 * 1) ExceptionHandlingFilter  (최상단)  : 필터/서블릿 레이어에서 던진 AppException을 JSON 표준 응답으로 변환
 * 2) JwtAuthenticationFilter  (그 다음) : 경로 정책(EXCLUDE/OPTIONAL/REQUIRED)에 따라 JWT 검증 및 사용자 속성 세팅
 *
 * <p> 주의:
 * - Spring MVC의 @ControllerAdvice는 DispatcherServlet 내부에서만 동작한다.
 *   필터 단계에서 던진 예외는 여기서 먼저 처리되어야 JSON 표준 응답을 보장할 수 있다.
 * - ERROR 디스패치는 ExceptionHandlingFilter 내부에서 shouldNotFilterErrorDispatch() = true로 막는다.
 *   따라서 DispatcherType.ERROR는 등록하지 않는다.
 */
@Configuration
public class FilterConfig {

    // 순서는 "숫자가 낮을수록 먼저 실행"
    private static final int ORDER_EXCEPTION = Ordered.HIGHEST_PRECEDENCE;        // 맨 앞
    private static final int ORDER_JWT       = Ordered.HIGHEST_PRECEDENCE + 10;   // 예외 필터 다음

    /**
     * 전역 예외 처리 필터 (디스패처 이전)
     * - 하위 필터/서블릿에서 던진 AppException을 받아 JSON 표준 응답으로 변환
     * - ERROR 디스패치 중복 방지는 필터 내부에서 처리(shouldNotFilterErrorDispatch=true)
     */
    @Bean
    public FilterRegistrationBean<ExceptionHandlingFilter> exceptionHandlingFilter(ObjectMapper om) {
        FilterRegistrationBean<ExceptionHandlingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new ExceptionHandlingFilter(om));
        reg.setName("exceptionHandlingFilter");
        reg.addUrlPatterns("/*"); // 모든 요청
        reg.setDispatcherTypes(EnumSet.of(
                DispatcherType.REQUEST,  // 일반 요청
                DispatcherType.ASYNC,    // 비동기 디스패치 (필요 없으면 제거 가능)
                DispatcherType.FORWARD   // 내부 포워드 (필요 없으면 제거 가능)
        ));
        reg.setAsyncSupported(true);     // 비동기 서블릿 환경에서 안전
        reg.setOrder(ORDER_EXCEPTION);   // 최상단
        return reg;
    }

    /**
     * JWT 인증 필터
     * - 경로 정책에 따라 EXCLUDE/OPTIONAL/REQUIRED 모드로 동작
     * - REQUIRED: 토큰 필수, 실패 시 AppException 던짐 (예외 필터가 JSON 응답으로 변환)
     * - OPTIONAL: 토큰 있으면 검증/속성세팅, 실패/없어도 통과
     * - EXCLUDE : 완전 공개, 토큰 무시
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(
            JwtProvider jwtProvider,
            JwtAuthenticationFilterProperties props
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new JwtAuthenticationFilter(jwtProvider, props));
        reg.setName("jwtAuthenticationFilter");
        reg.addUrlPatterns("/*");
        reg.setDispatcherTypes(EnumSet.of(
                DispatcherType.REQUEST,
                DispatcherType.ASYNC,
                DispatcherType.FORWARD
        ));
        reg.setAsyncSupported(true);
        reg.setOrder(ORDER_JWT); // 예외 필터 다음
        return reg;
    }
}

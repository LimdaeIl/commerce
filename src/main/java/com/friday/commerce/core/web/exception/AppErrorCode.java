package com.friday.commerce.core.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum AppErrorCode implements ErrorCode {

    // 시스템/인프라
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공통: 서버 내부 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "공통: 서비스가 일시적으로 불가능합니다."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "공통: 게이트웨이 응답 시간이 초과되었습니다."),
    DEPENDENCY_FAILURE(HttpStatus.BAD_GATEWAY, "공통: 외부/하위 시스템 연동에 실패했습니다."),
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공통: 입출력 처리 중 오류가 발생했습니다."),
    REQUEST_CONTEXT_NOT_FOUND(HttpStatus.NOT_FOUND,"공통: 요청 컨텍스트를 찾을 수 없습니다."),

    // 요청/형식/프로토콜
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "공통: 잘못된 입력입니다."),
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "공통: 현재 사용자 정보가 올바르지 않습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "공통: 입력값 검증에 실패했습니다."),
    BINDING_ERROR(HttpStatus.BAD_REQUEST, "공통: 요청 데이터 바인딩에 실패했습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "공통: 파라미터 타입이 일치하지 않습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "공통: 요청 본문을 파싱할 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "공통: 허용되지 않은 HTTP 메서드입니다."),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "공통: 허용되지 않는 응답 형식입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "공통: 지원하지 않는 콘텐츠 타입입니다."),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "공통: 요청 본문이 너무 큽니다."),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "공통: 요청 시간이 초과되었습니다."),
    PRECONDITION_FAILED(HttpStatus.PRECONDITION_FAILED, "공통: 사전 조건이 충족되지 않았습니다."),
    RANGE_NOT_SATISFIABLE(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "공통: 요청 범위를 만족할 수 없습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "공통: 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    CONFLICT(HttpStatus.CONFLICT, "공통: 리소스 상태 충돌이 발생했습니다."),

    // [헤더/메타데이터]
    INVALID_HEADER(HttpStatus.BAD_REQUEST, "공통: 잘못된 헤더 정보입니다."),
    MISSING_HEADER(HttpStatus.BAD_REQUEST, "공통: 필수 헤더가 누락되었습니다."),
    MISSING_HEADER_USER_ID(HttpStatus.BAD_REQUEST, "공통: 회원 ID 헤더가 누락되었습니다."),
    MISSING_HEADER_USER_ROLE(HttpStatus.BAD_REQUEST, "공통: 회원 권한 헤더가 누락되었습니다."),
    INVALID_HEADER_USER_ID(HttpStatus.BAD_REQUEST, "공통: 회원 ID 헤더가 잘못된 값입니다."),
    INVALID_HEADER_USER_ID_NOT_INTEGER(HttpStatus.BAD_REQUEST, "공통: 회원 ID 헤더 값이 숫자가 아닙니다."),
    INVALID_HEADER_USER_ROLE(HttpStatus.BAD_REQUEST, "공통: 값이 올바른 회원 권한 형식이 아닙니다."),
    INVALID_QUERY_PARAMETER(HttpStatus.BAD_REQUEST, "공통: 잘못된 쿼리 파라미터입니다."),
    NULL_USER_ROLE(HttpStatus.BAD_REQUEST, "공통: 권한이 NULL 입니다."),
    EMPTY_USER_ROLE(HttpStatus.BAD_REQUEST, "공통: 권한이 빈 값입니다."),


    // [인증/인가 (공통)]
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "공통: 인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "공통: 접근 권한이 없습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "공통: 인증에 실패했습니다."),
    CREDENTIALS_INVALID(HttpStatus.UNAUTHORIZED, "공통: 자격 증명이 유효하지 않습니다."),
    TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "공통: 인증 토큰이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "공통: 인증 토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "공통: 인증 토큰이 만료되었습니다."),
    SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "공통: 토큰 서명이 유효하지 않습니다."),

    // 리소스/상태
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "공통: 요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "공통: 동일한 리소스가 이미 존재합니다."),
    RESOURCE_LOCKED(HttpStatus.LOCKED, "공통: 리소스가 잠겨 있어 처리할 수 없습니다."),
    OPTIMISTIC_LOCK_FAILED(HttpStatus.CONFLICT, "공통: 동시 수정으로 인해 갱신에 실패했습니다."),
    ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "공통: 현재 상태에서 허용되지 않는 작업입니다."),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "공통: 잘못된 인자 값입니다."),
    RESP_BODY_WRITE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "공통: 응답 본문을 생성/쓰기 중 오류가 발생했습니다."),
    MEDIA_TYPE_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "공통: 요청한 응답 형식을 제공할 수 없습니다."),

    // Idempotency
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "공통: Idempotency-Key 헤더가 필요합니다."),
    IDEMPOTENCY_REPLAY(HttpStatus.CONFLICT, "공통: 중복 요청이 감지되었습니다."),

    // Snowflake
    SNOWFLAKE_NODE_ID_REQUIRED(HttpStatus.INTERNAL_SERVER_ERROR,
            "식별자: Snowflake node-id가 설정되지 않았습니다. auto-detect-node-id=false인 경우 필수입니다.");


    private final HttpStatus status;
    private final String message;
}

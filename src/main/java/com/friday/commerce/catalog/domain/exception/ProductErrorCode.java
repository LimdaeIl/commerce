package com.friday.commerce.catalog.domain.exception;

import com.friday.commerce.core.web.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // ===== Product 공통 =====
    PRODUCT_INVALID(HttpStatus.BAD_REQUEST, "상품: 잘못된 상품 정보입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품: 상품 정보를 찾을 수 없습니다."),
    PRODUCT_ID_DUPLICATED(HttpStatus.CONFLICT, "상품: 이미 사용 중인 상품 ID입니다."),
    PRODUCT_DELETED(HttpStatus.FORBIDDEN, "상품: 삭제된 상품입니다."),
    PRODUCT_ARCHIVED(HttpStatus.FORBIDDEN, "상품: 보관된 상품입니다."),
    PRODUCT_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "상품: 제목은 필수입니다."),
    PRODUCT_TITLE_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "상품: 제목 길이가 허용 범위를 초과했습니다."),
    PRODUCT_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "상품: 콘텐츠(마크다운)는 필수입니다."),
    PRODUCT_STATUS_SAME_BEFORE(HttpStatus.CONFLICT, "상품: 현재 상태와 동일한 상태로 변경할 수 없습니다."),
    PRODUCT_STATUS_INVALID_TRANSITION(HttpStatus.FORBIDDEN, "상품: 해당 상태로 전환할 수 없습니다."),
    PRODUCT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "상품: 현재 상태에서는 수정할 수 없습니다."),
    PRODUCT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "상품: 현재 상태에서는 삭제할 수 없습니다."),

    // ===== 발행(PUBLISH) 검증 =====
    PRODUCT_PUBLISH_NO_SKU(HttpStatus.BAD_REQUEST, "상품 발행: 최소 1개 이상의 SKU가 필요합니다."),
    PRODUCT_PUBLISH_NO_IMAGE(HttpStatus.BAD_REQUEST, "상품 발행: 최소 1개 이상의 이미지가 필요합니다."),
    PRODUCT_PUBLISH_NO_CATEGORY(HttpStatus.BAD_REQUEST, "상품 발행: 최소 1개 이상의 카테고리 연결이 필요합니다."),
    PRODUCT_PUBLISH_INVALID_PRICE(HttpStatus.BAD_REQUEST, "상품 발행: 가격이 올바르지 않습니다."),
    PRODUCT_PUBLISH_INVALID_STOCK(HttpStatus.BAD_REQUEST, "상품 발행: 재고가 올바르지 않습니다."),
    PRODUCT_PUBLISH_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "상품 발행: 콘텐츠(마크다운)와 정제된 HTML이 필요합니다."),

    // ===== SKU =====
    SKU_NOT_FOUND(HttpStatus.NOT_FOUND, "SKU: SKU 정보를 찾을 수 없습니다."),
    SKU_DUPLICATED(HttpStatus.CONFLICT, "SKU: 이미 존재하는 SKU입니다."),
    SKU_PRICE_INVALID(HttpStatus.BAD_REQUEST, "SKU: 가격이 올바르지 않습니다."),
    SKU_STOCK_INVALID(HttpStatus.BAD_REQUEST, "SKU: 재고가 올바르지 않습니다."),
    SKU_STOCK_INSUFFICIENT(HttpStatus.BAD_REQUEST, "SKU: 재고가 부족합니다."),
    SKU_ASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "SKU: 해당 상품에 SKU를 할당할 수 없습니다."),

    // ===== 이미지 =====
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지: 상품 이미지를 찾을 수 없습니다."),
    IMAGE_URL_INVALID(HttpStatus.BAD_REQUEST, "이미지: 이미지 URL 형식이 올바르지 않습니다."),
    IMAGE_SORT_ORDER_DUPLICATED(HttpStatus.CONFLICT, "이미지: 정렬 순서가 중복되었습니다."),
    IMAGE_ASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "이미지: 해당 상품에 이미지를 추가할 수 없습니다."),

    // ===== 카테고리 & 연결 =====
    CATEGORY_NAME_INVALID(HttpStatus.BAD_REQUEST, "카테고리: 카테고리 이름이 부적절합니다."),
    CATEGORY_NAME_DUPLICATED(HttpStatus.CONFLICT, "카테고리: 이미 존재하는 카테고리입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리: 카테고리를 찾을 수 없습니다."),
    CATEGORY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "카테고리: 허용된 최대 깊이를 초과했습니다."),
    CATEGORY_PATH_INVALID(HttpStatus.BAD_REQUEST, "카테고리: 경로가 올바르지 않습니다."),
    CATEGORY_CYCLE_DETECTED(HttpStatus.BAD_REQUEST, "카테고리: 순환 참조가 감지되었습니다."),
    CATEGORY_PARENT_NULL(HttpStatus.BAD_REQUEST, "카테고리: 부모 카테고리가 NULL 입니니다."),
    PRODUCT_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리 연결: 상품-카테고리 연결을 찾을 수 없습니다."),
    PRODUCT_CATEGORY_DUPLICATED(HttpStatus.CONFLICT, "카테고리 연결: 이미 연결된 카테고리입니다."),


    // ===== 내용 정제/포맷 =====
    CONTENT_HTML_SANITIZE_FAILED(HttpStatus.BAD_REQUEST, "콘텐츠: HTML 정제 과정에서 오류가 발생했습니다."),
    CONTENT_MARKDOWN_INVALID(HttpStatus.BAD_REQUEST, "콘텐츠: 마크다운 형식이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
package com.friday.commerce.order.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.core.web.response.PageResponse;
import com.friday.commerce.order.application.dto.request.CreateOrderRequest;
import com.friday.commerce.order.application.dto.response.CreateOrderResponse;
import com.friday.commerce.order.application.dto.response.GetOrderResponse;
import com.friday.commerce.order.application.usecase.OrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order-v1", description = "주문 생성/조회 API")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/orders", produces = "application/json")
@RestController
public class OrderController {

    private final OrderUseCase orderUseCase;

    @Operation(summary = "주문 생성", description = "로그인 사용자의 주문을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER, UserRole.USER})
    @PostMapping(consumes = "application/json")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        CreateOrderResponse response = orderUseCase.createOrder(request, info);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "주문 목록 조회",
            description = "키워드/기간/페이지네이션으로 내 주문 목록을 조회합니다. 날짜는 `YYYY-MM-DD` 형식."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER, UserRole.USER})
    @GetMapping
    public ResponseEntity<PageResponse<GetOrderResponse>> getOrders(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info,

            @Parameter(description = "검색 키워드(상품명/주문번호 일부 등)", example = "이어폰")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "조회 시작일(포함) - YYYY-MM-DD", example = "2025-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,

            @Parameter(description = "조회 종료일(포함) - YYYY-MM-DD", example = "2025-10-10")
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,

            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Direction.DESC)
            }) Pageable pageable
    ) {
        PageResponse<GetOrderResponse> response =
                orderUseCase.getOrders(info, keyword, from, to, pageable);
        return ResponseEntity.ok(response);
    }
}

package com.friday.commerce.order.presentation;

import com.friday.commerce.order.application.dto.response.OrderBriefResponse;
import com.friday.commerce.order.application.facade.OrderFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order-V1", description = "주문 브리프 조회(공개) API")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/orders", produces = "application/json")
public class OrderQueryController {

    private final OrderFacade orderFacade;

    @Operation(
            summary = "주문 브리프 조회(공개)",
            description = "주문 요약 정보를 조회합니다. 인증 없이 접근 가능하도록 구성되어 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderBriefResponse.class))),
            @ApiResponse(responseCode = "404", description = "주문 없음")
    })
    @GetMapping("/{orderId}/brief")
    public ResponseEntity<OrderBriefResponse> getOrderBrief(
            @Parameter(description = "주문 ID", example = "9876543210...")
            @PathVariable Long orderId
    ) {
        return orderFacade.findOrderBrief(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

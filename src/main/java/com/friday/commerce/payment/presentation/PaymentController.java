package com.friday.commerce.payment.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.payment.application.dto.request.ConfirmPaymentRequest;
import com.friday.commerce.payment.application.dto.response.ConfirmPaymentResponse;
import com.friday.commerce.payment.application.usecase.PaymentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment-V1", description = "결제 확정/리다이렉트 핸들러 API")
@RestController
@RequestMapping(value = "/api/v1/payments", produces = "application/json")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @Operation(
            summary = "결제 확정",
            description = "프론트에서 받은 `paymentKey`와 `orderId`로 결제를 확정합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConfirmPaymentRequest.class),
                            examples = @ExampleObject(
                                    name = "confirm 예시",
                                    value = """
                                            {
                                              "paymentKey": "pay_20251010_abcdef",
                                              "orderId": 987654321
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확정 성공",
                    content = @Content(schema = @Schema(implementation = ConfirmPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "409", description = "중복/충돌(이미 확정)")
    })
    @PostMapping(value = "/confirm", consumes = "application/json")
    public ResponseEntity<ConfirmPaymentResponse> confirm(
            @RequestBody ConfirmPaymentRequest request,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        ConfirmPaymentResponse response = paymentUseCase.confirm(request, info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "결제 성공 리다이렉트(공개)",
            description = "PG(Toss) 성공 리다이렉트가 호출하는 엔드포인트입니다. 쿼리스트링의 `paymentKey`, `orderId`를 사용해 확정을 시도합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확정 성공",
                    content = @Content(schema = @Schema(implementation = ConfirmPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @GetMapping("/success")
    public ResponseEntity<ConfirmPaymentResponse> success(
            @Parameter(description = "PG에서 전달한 결제 키", example = "pay_20251010_abcdef")
            @RequestParam String paymentKey,
            @Parameter(description = "주문 ID", example = "987654321")
            @RequestParam Long orderId
    ) {
        ConfirmPaymentRequest request = new ConfirmPaymentRequest(paymentKey, orderId);
        ConfirmPaymentResponse response = paymentUseCase.confirm(request, null);
        return ResponseEntity.ok(response);
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "실패 응답",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    description = "`code`/`message` 문자열을 포함한 단순 에러 맵",
                                    type = "object",
                                    additionalPropertiesSchema = String.class
                            ),
                            examples = @ExampleObject(
                                    name = "실패 예시",
                                    value = """
                                            { "code": "PAYMENT_FAILED", "message": "승인 거절(한도 초과)" }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(
            @Parameter(description = "PG 오류 코드", example = "PAYMENT_FAILED")
            @RequestParam(required = false) String code,
            @Parameter(description = "PG 오류 메시지", example = "승인 거절(한도 초과)")
            @RequestParam(required = false) String message
    ) {
        return ResponseEntity.status(400).body(Map.of(
                "code", code == null ? "UNKNOWN" : code,
                "message", message == null ? "결제 실패" : message
        ));
    }
}

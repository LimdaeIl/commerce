package com.friday.commerce.payment.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.order.application.dto.response.OrderBriefResponse;
import com.friday.commerce.payment.application.dto.response.ConfirmPaymentResponse;
import com.friday.commerce.payment.application.dto.request.ConfirmPaymentRequest;
import com.friday.commerce.payment.application.usecase.PaymentUseCase;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirm(
            @RequestBody ConfirmPaymentRequest request,
            @CurrentUser CurrentUserInfo info
    ) {
        ConfirmPaymentResponse response = paymentUseCase.confirm(request, info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/success")
    public ResponseEntity<ConfirmPaymentResponse> success(
            @RequestParam String paymentKey,
            @RequestParam Long orderId
    ) {
        ConfirmPaymentRequest request = new ConfirmPaymentRequest(paymentKey, orderId);
        ConfirmPaymentResponse response = paymentUseCase.confirm(request, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message
    ) {
        return ResponseEntity.status(400).body(Map.of(
                "code", code == null ? "UNKNOWN" : code,
                "message", message == null ? "결제 실패" : message
        ));
    }
}

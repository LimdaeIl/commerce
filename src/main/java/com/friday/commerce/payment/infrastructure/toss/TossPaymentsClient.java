package com.friday.commerce.payment.infrastructure.toss;

import com.friday.commerce.payment.infrastructure.toss.dto.TossConfirmResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final RestClient tossRestClient;

    public TossConfirmResponse confirm(
            String paymentKey,
            String orderId,
            int amount,
            String idempotencyKey
    ) {
        Map<String, ? extends Serializable> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        return tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .body(body)
                .retrieve()
                .body(TossConfirmResponse.class);
    }

    public Map<String, Object> cancel(String paymentKey, String reason, Integer cancelAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", reason);
        if (cancelAmount != null) {
            body.put("cancelAmount", cancelAmount);
        }

        return tossRestClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}

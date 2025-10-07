package com.friday.commerce.payment.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
        @NotBlank(message = "결제: 결제 키는 필수입니다.")
        String paymentKey,

        @NotNull(message = "결제: 주문 ID는 필수입니다.")
        Long orderId,

        @NotNull(message = "결제: 전체 결제금액은 필수 입니다.")
        Integer amount
) {

}

package com.friday.commerce.order.application.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.core.web.response.PageResponse;
import com.friday.commerce.order.application.dto.request.CreateOrderItemRequest;
import com.friday.commerce.order.application.dto.request.CreateOrderRequest;
import com.friday.commerce.order.application.dto.request.DeliveryAddressRequest;
import com.friday.commerce.order.application.dto.response.CreateOrderResponse;
import com.friday.commerce.order.application.dto.response.GetOrderResponse;
import com.friday.commerce.order.application.port.out.CatalogPort;
import com.friday.commerce.order.application.usecase.OrderUseCase;
import com.friday.commerce.order.domain.entity.Order;
import com.friday.commerce.order.domain.entity.OrderItem;
import com.friday.commerce.order.domain.entity.OrderStatus;
import com.friday.commerce.order.domain.entity.OrdererAddress;
import com.friday.commerce.order.domain.entity.ProductBrief;
import com.friday.commerce.order.domain.exception.OrderErrorCode;
import com.friday.commerce.order.domain.exception.OrderException;
import com.friday.commerce.order.domain.repository.OrderRepository;
import com.friday.commerce.order.domain.repository.OrderRepository.ListRow;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService implements OrderUseCase {

    private final Snowflake snowflake;
    private final OrderRepository orderRepository;
    private final CatalogPort catalogPort;


    @Transactional
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request, CurrentUserInfo info) {
        // 1) 요청 상품 ID 모으기
        List<Long> productIds = request.items().stream()
                .map(CreateOrderItemRequest::productId)
                .distinct()
                .toList();

        // 2) 카탈로그에서 배치 조회 (N+1 방지)
        Map<Long, ProductBrief> briefMap = catalogPort.getProductsByIds(productIds);

        // 3) 동일 상품 중복 라인 집계 → 재고 검증을 상품별 총수량으로
        Map<Long, Integer> requestedQtyByProduct = request.items().stream()
                .collect(groupingBy(CreateOrderItemRequest::productId,
                        summingInt(CreateOrderItemRequest::quantity)));

        for (Map.Entry<Long, Integer> e : requestedQtyByProduct.entrySet()) {
            Long pid = e.getKey();
            int need = e.getValue();
            ProductBrief p = briefMap.get(pid);
            if (p == null) {
                throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
            }
            if (need <= 0) {
                throw new OrderException(OrderErrorCode.ORDER_ITEMS_INVALID);
            }
            if (p.stock() < need) {
                throw new OrderException(OrderErrorCode.STOCK_INSUFFICIENT);
            }
        }

        // 4) 총액 계산 (long으로 누적 후 안전 변환)
        long totalLong = 0L;
        for (CreateOrderItemRequest item : request.items()) {
            ProductBrief p = briefMap.get(item.productId());
            totalLong += p.price() * (long) item.quantity();
        }
        int totalAmount = Math.toIntExact(totalLong);

        // 5) 주소 스냅샷
        DeliveryAddressRequest deliveryAddressRequest = request.deliveryAddress();
        OrdererAddress ordererAddress = OrdererAddress.create(
                deliveryAddressRequest.recipientName(),
                deliveryAddressRequest.ordererEmail(),
                deliveryAddressRequest.zipCode(),
                deliveryAddressRequest.addressLine1(),
                deliveryAddressRequest.addressLine2(),
                deliveryAddressRequest.city(),
                deliveryAddressRequest.state()
        );

        // 6) 주문 생성(아이템은 이후 add)
        Order order = Order.create(
                snowflake.nextId(),
                info.userId(),
                OrderStatus.CREATED,
                new ArrayList<>(),      // 빈 리스트로 생성
                ordererAddress,
                totalAmount,
                info.userId()
        );

        // 7) 주문 아이템 생성 + 연결
        for (CreateOrderItemRequest item : request.items()) {
            ProductBrief p = briefMap.get(item.productId());

            int unitPrice = Math.toIntExact(p.price());
            int lineTotal = Math.toIntExact(p.price() * (long) item.quantity());

            OrderItem oi = OrderItem.create(
                    snowflake.nextId(),
                    p.productId(),
                    order,                         // 역참조 설정
                    p.productTitle(),
                    unitPrice,
                    item.quantity(),
                    lineTotal
            );
            order.getOrderItems().add(oi);        // 컬렉션에 추가(영속 전 cascade)
        }

        // 8) 재고 차감 — 지금 동시성은 미루는 조건이니 포트만 남겨두고 나중에 구현
        catalogPort.decreaseStocks(requestedQtyByProduct);

        // 9) 저장 + 응답
        Order save = orderRepository.save(order);

        return CreateOrderResponse.of(save);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<GetOrderResponse> getOrders(
            CurrentUserInfo info,
            String keyword,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        // 1) 키워드 정규화 (빈 문자열은 검색 제외)
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        // 2) 날짜 경계 계산 (to는 다음날 0시 미만)
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to   != null) ? to.plusDays(1).atStartOfDay() : null;

        // 3) 범위 유효성
        if (fromDt != null && toDt != null && !fromDt.isBefore(toDt)) {
            throw new OrderException(OrderErrorCode.ORDER_INVALID);
        }

        // 4) 조회 + 매핑
        Page<ListRow> page = orderRepository.findOrderList(
                info.userId(), kw, fromDt, toDt, pageable
        );

        Page<GetOrderResponse> mapped = page.map(r ->
                new GetOrderResponse(
                        r.getOrderId(),
                        r.getOrderStatus().name(),
                        r.getTotalAmount(),
                        r.getItemCount(),
                        r.getCreatedAt(),
                        r.getPrimaryProductName(),
                        r.getPrimaryImageUrl()
                )
        );

        // 5) 표준 페이징 응답
        return PageResponse.from(mapped);
    }
}

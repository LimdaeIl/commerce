package com.friday.commerce.order.infrastructure.jpa;

import com.friday.commerce.order.domain.entity.Order;
import com.friday.commerce.order.domain.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<Order, Long>, OrderRepository {


    List<Order> findOrdersByUserId(Long userId);

    @Override
    @Query(
            value = """
        select
          o.orderId     as orderId,
          o.createdAt   as createdAt,
          o.orderStatus as orderStatus,
          o.totalAmount as totalAmount,
          (select count(oi) from OrderItem oi where oi.order = o) as itemCount,

          (
            select oi1.productName
            from OrderItem oi1
            where oi1.order = o
              and oi1.orderItemId = (
                  select min(oi2.orderItemId) from OrderItem oi2 where oi2.order = o
              )
          ) as primaryProductName,

          (
            select i.imageUrl
            from ProductImage i
            where i.product.productId = (
                select oi3.productId
                from OrderItem oi3
                where oi3.order = o
                  and oi3.orderItemId = (
                      select min(oi4.orderItemId) from OrderItem oi4 where oi4.order = o
                  )
            )
            and i.sortOrder = 0
          ) as primaryImageUrl

        from Order o
        where o.userId = :userId
          and (:from is null or o.createdAt >= :from)
          and (:to   is null or o.createdAt <  :to)
          and (
               :keyword is null
               or exists (
                    select 1
                    from OrderItem soi
                    where soi.order = o
                      and lower(soi.productName) like lower(concat('%', :keyword, '%'))
               )
          )
        """,
            countQuery = """
        select count(o)
        from Order o
        where o.userId = :userId
          and (:from is null or o.createdAt >= :from)
          and (:to   is null or o.createdAt <  :to)
          and (
               :keyword is null
               or exists (
                    select 1
                    from OrderItem soi
                    where soi.order = o
                      and lower(soi.productName) like lower(concat('%', :keyword, '%'))
               )
          )
        """
    )
    Page<ListRow> findOrderList(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}

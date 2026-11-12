
package com.inditex.pricing.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prices", indexes = {
        @Index(
                name = "idx_prices_lookup",
                columnList = "brand_id, product_id, priority"
        )
})
@Getter
@NoArgsConstructor
public class PriceEntity {

    @Id
    private Long id;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price_list", nullable = false)
    private Long priceList;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "curr", nullable = false, length = 3)
    private String currency;
}

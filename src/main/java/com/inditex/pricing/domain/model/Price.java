
package com.inditex.pricing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Price(
        Long id,
        Long brandId,
        Long productId,
        Long priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer priority,
        BigDecimal amount,
        String currency
) {
}


package com.inditex.pricing.domain.port;

import com.inditex.pricing.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepositoryPort {

    Optional<Price> findApplicablePrice(
            LocalDateTime applicationDate,
            Long productId,
            Long brandId
    );
}

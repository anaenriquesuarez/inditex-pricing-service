
package com.inditex.pricing.application.usecase;

import com.inditex.pricing.domain.model.Price;

import java.time.LocalDateTime;

public interface GetApplicablePriceUseCase {

    Price execute(
            LocalDateTime applicationDate,
            Long productId,
            Long brandId
    );
}

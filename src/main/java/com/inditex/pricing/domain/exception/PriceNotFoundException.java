
package com.inditex.pricing.domain.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(
            Long productId,
            Long brandId,
            LocalDateTime applicationDate
    ) {
        super(String.format(
                "No applicable price found for product %d, " +
                        "brand %d and application date %s",
                productId,
                brandId,
                applicationDate
        ));
    }
}

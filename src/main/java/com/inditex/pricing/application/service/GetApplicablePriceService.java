
package com.inditex.pricing.application.service;

import com.inditex.pricing.application.usecase.GetApplicablePriceUseCase;
import com.inditex.pricing.domain.exception.PriceNotFoundException;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.PriceRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GetApplicablePriceService
        implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    public GetApplicablePriceService(
            PriceRepositoryPort priceRepositoryPort
    ) {
        this.priceRepositoryPort = priceRepositoryPort;
    }

    @Override
    public Price execute(
            LocalDateTime applicationDate,
            Long productId,
            Long brandId
    ) {
        return priceRepositoryPort
                .findApplicablePrice(
                        applicationDate,
                        productId,
                        brandId
                )
                .orElseThrow(() -> new PriceNotFoundException(
                        productId,
                        brandId,
                        applicationDate
                ));
    }
}

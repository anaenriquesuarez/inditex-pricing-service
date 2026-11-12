
package com.inditex.pricing.infrastructure.adapter.out.persistence;

import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.PriceRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PricePersistenceAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository priceJpaRepository;

    public PricePersistenceAdapter(
            PriceJpaRepository priceJpaRepository
    ) {
        this.priceJpaRepository = priceJpaRepository;
    }

    @Override
    public Optional<Price> findApplicablePrice(
            LocalDateTime applicationDate,
            Long productId,
            Long brandId
    ) {
        return priceJpaRepository
                .findApplicablePrice(
                        applicationDate,
                        productId,
                        brandId,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    private Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getId(),
                entity.getBrandId(),
                entity.getProductId(),
                entity.getPriceList(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPriority(),
                entity.getPrice(),
                entity.getCurrency()
        );
    }
}

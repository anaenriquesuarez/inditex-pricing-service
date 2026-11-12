
package com.inditex.pricing.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceJpaRepository
        extends JpaRepository<PriceEntity, Long> {

    @Query("""
            SELECT p
            FROM PriceEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND p.startDate <= :applicationDate
              AND p.endDate >= :applicationDate
            ORDER BY p.priority DESC, p.id DESC
            """)
    List<PriceEntity> findApplicablePrice(
            @Param("applicationDate") LocalDateTime applicationDate,
            @Param("productId") Long productId,
            @Param("brandId") Long brandId,
            Pageable pageable
    );
}

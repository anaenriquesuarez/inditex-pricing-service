
package com.inditex.pricing.infrastructure.adapter.in.rest;

import com.inditex.pricing.application.usecase.GetApplicablePriceUseCase;
import com.inditex.pricing.domain.model.Price;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@RestController
@Validated
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(
            GetApplicablePriceUseCase getApplicablePriceUseCase
    ) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @GetMapping
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime applicationDate,

            @RequestParam @Positive Long productId,

            @RequestParam @Positive Long brandId
    ) {
        Price price = getApplicablePriceUseCase.execute(
                applicationDate,
                productId,
                brandId
        );

        PriceResponse response = new PriceResponse(
                price.productId(),
                price.brandId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.amount(),
                price.currency()
        );

        return ResponseEntity.ok(response);
    }
}

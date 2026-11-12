
package com.inditex.pricing.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the product pricing REST API.
 * Tests the complete request flow, including the REST controller,
 * application service, persistence adapter and H2 database.
 * The first five tests cover the scenarios required by the
 * technical assessment. Additional tests validate error handling,
 * parameter validation and price validity boundaries.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    private static final String ENDPOINT = "/api/v1/prices";
    private static final String PRODUCT_ID = "35455";
    private static final String BRAND_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    /**
     * Executes a pricing request and verifies the response.
     * Validates the HTTP status, product and brand identifiers,
     * applicable price list, final price, validity period
     * and currency.
     *
     * @param date application date and time
     * @param expectedPriceList expected price list identifier
     * @param expectedPrice expected final price
     * @param expectedStartDate expected validity start date
     * @param expectedEndDate expected validity end date
     */
    private void verifyPrice(
            String date,
            int expectedPriceList,
            BigDecimal expectedPrice,
            String expectedStartDate,
            String expectedEndDate
    ) throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", date)
                        .param("productId", PRODUCT_ID)
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList")
                        .value(expectedPriceList))
                .andExpect(jsonPath(
                        "$.price",
                        comparesEqualTo(expectedPrice),
                        BigDecimal.class
                ))
                .andExpect(jsonPath("$.startDate")
                        .value(expectedStartDate))
                .andExpect(jsonPath("$.endDate")
                        .value(expectedEndDate))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    // ---------------------------------------------------------
    // REQUIRED TEST SCENARIOS
    // ---------------------------------------------------------

    /**
     * Test 1: June 14th, 2020 at 10:00.
     * Only the base price is applicable.
     * Expected price list: 1
     * Expected price: 35.50 EUR
     */
    @Test
    void test1_basePrice() throws Exception {
        verifyPrice(
                "2020-06-14T10:00:00",
                1,
                new BigDecimal("35.50"),
                "2020-06-14T00:00:00",
                "2020-12-31T23:59:59"
        );
    }

    /**
     * Test 2: June 14th, 2020 at 16:00.
     * Two price lists are applicable.
     * The price list with the highest priority must be selected.
     * Expected price list: 2
     * Expected price: 25.45 EUR
     */
    @Test
    void test2_higherPriorityPrice() throws Exception {
        verifyPrice(
                "2020-06-14T16:00:00",
                2,
                new BigDecimal("25.45"),
                "2020-06-14T15:00:00",
                "2020-06-14T18:30:00"
        );
    }

    /**
     * Test 3: June 14th, 2020 at 21:00.
     * The higher-priority promotional price has expired.
     * The base price must be applied again.
     * Expected price list: 1
     * Expected price: 35.50 EUR
     */
    @Test
    void test3_basePriceAfterPromotion() throws Exception {
        verifyPrice(
                "2020-06-14T21:00:00",
                1,
                new BigDecimal("35.50"),
                "2020-06-14T00:00:00",
                "2020-12-31T23:59:59"
        );
    }

    /**
     * Test 4: June 15th, 2020 at 10:00.
     * A special price overlaps with the base price.
     * The higher-priority price list must be selected.
     * Expected price list: 3
     * Expected price: 30.50 EUR
     */
    @Test
    void test4_nextDaySpecialPrice() throws Exception {
        verifyPrice(
                "2020-06-15T10:00:00",
                3,
                new BigDecimal("30.50"),
                "2020-06-15T00:00:00",
                "2020-06-15T11:00:00"
        );
    }

    /**
     * Test 5: June 16th, 2020 at 21:00.
     * Price lists 1 and 4 overlap.
     * Price list 4 has the highest priority.
     * Expected price list: 4
     * Expected price: 38.95 EUR
     */
    @Test
    void test5_lastApplicablePrice() throws Exception {
        verifyPrice(
                "2020-06-16T21:00:00",
                4,
                new BigDecimal("38.95"),
                "2020-06-15T16:00:00",
                "2020-12-31T23:59:59"
        );
    }

    // ---------------------------------------------------------
    // ERROR HANDLING TESTS
    // ---------------------------------------------------------

    /**
     * Verifies that requesting a nonexistent product returns
     * HTTP 404 and the expected error code.
     */
    @Test
    void shouldReturn404WhenPriceDoesNotExist()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate",
                                "2020-06-14T10:00:00")
                        .param("productId", "99999")
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("PRICE_NOT_FOUND"));
    }

    /**
     * Verifies that an invalid application date format
     * returns HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenDateIsInvalid()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "invalid-date")
                        .param("productId", PRODUCT_ID)
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that negative product identifiers
     * are rejected with HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenProductIdIsNegative()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate",
                                "2020-06-14T10:00:00")
                        .param("productId", "-1")
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("INVALID_PARAMETERS"));
    }

    /**
     * Verifies that product identifiers equal to zero
     * are rejected with HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenProductIdIsZero()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate",
                                "2020-06-14T10:00:00")
                        .param("productId", "0")
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("INVALID_PARAMETERS"));
    }

    /**
     * Verifies that brand identifiers equal to zero
     * are rejected with HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenBrandIdIsZero()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate",
                                "2020-06-14T10:00:00")
                        .param("productId", PRODUCT_ID)
                        .param("brandId", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("INVALID_PARAMETERS"));
    }

    /**
     * Verifies that omitting the required application date
     * returns HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenApplicationDateIsMissing()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("productId", PRODUCT_ID)
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that an existing product returns HTTP 404
     * when no price is valid for the requested date.
     */
    @Test
    void shouldReturn404WhenNoPriceIsApplicable()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate",
                                "2021-01-01T10:00:00")
                        .param("productId", PRODUCT_ID)
                        .param("brandId", BRAND_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("PRICE_NOT_FOUND"));
    }

    /**
     * Verifies that prices belonging to another brand
     * are not returned for the requested brand.
     */
    @Test
    void shouldReturn404WhenBrandDoesNotExist()
            throws Exception {

        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate",
                                "2020-06-14T16:00:00")
                        .param("productId", PRODUCT_ID)
                        .param("brandId", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("PRICE_NOT_FOUND"));
    }

    // ---------------------------------------------------------
    // PRICE VALIDITY BOUNDARY TESTS
    // ---------------------------------------------------------

    /**
     * Verifies that the start date of a price validity
     * period is inclusive.
     * At exactly 15:00, price list 2 must already apply.
     */
    @Test
    void shouldApplyPriceAtStartDate() throws Exception {
        verifyPrice(
                "2020-06-14T15:00:00",
                2,
                new BigDecimal("25.45"),
                "2020-06-14T15:00:00",
                "2020-06-14T18:30:00"
        );
    }

    /**
     * Verifies that the end date of a price validity
     * period is inclusive.
     * At exactly 18:30, price list 2 must still apply.
     */
    @Test
    void shouldApplyPriceAtEndDate() throws Exception {
        verifyPrice(
                "2020-06-14T18:30:00",
                2,
                new BigDecimal("25.45"),
                "2020-06-14T15:00:00",
                "2020-06-14T18:30:00"
        );
    }
}

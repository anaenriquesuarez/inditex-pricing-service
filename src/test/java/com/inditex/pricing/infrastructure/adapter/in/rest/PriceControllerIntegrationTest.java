
package com.inditex.pricing.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the product pricing REST API.
 * Tests the complete request flow, including the REST controller,
 * application service, persistence adapter and H2 database.
 * The first five tests cover the scenarios required by the
 * technical assessment. Additional tests validate error handling
 * and price validity boundaries.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

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
            double expectedPrice,
            String expectedStartDate,
            String expectedEndDate
    ) throws Exception {

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", date)
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList")
                        .value(expectedPriceList))
                .andExpect(jsonPath("$.price")
                        .value(expectedPrice))
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
                35.50,
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
                25.45,
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
                35.50,
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
                30.50,
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
                38.95,
                "2020-06-15T16:00:00",
                "2020-12-31T23:59:59"
        );
    }

    // ---------------------------------------------------------
    // ERROR HANDLING TESTS
    // ---------------------------------------------------------

    /**
     * Verifies that requesting a product without an applicable
     * price returns HTTP 404 and the expected error code.
     */
    @Test
    void shouldReturn404WhenPriceDoesNotExist()
            throws Exception {

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate",
                                "2020-06-14T10:00:00")
                        .param("productId", "99999")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("PRICE_NOT_FOUND"));
    }

    /**
     * Verifies that an invalid date format is rejected
     * with HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenDateIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", "invalid-date")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that product identifiers must be positive.
     * A negative productId violates the validation constraint
     * and must return HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenProductIdIsNegative()
            throws Exception {

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate",
                                "2020-06-14T10:00:00")
                        .param("productId", "-1")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // PRICE VALIDITY BOUNDARY TESTS
    // ---------------------------------------------------------

    /**
     * Verifies that the start date of a price validity period
     * is inclusive.
     * At exactly 15:00, price list 2 must already apply.
     */
    @Test
    void shouldApplyPriceAtStartDate() throws Exception {
        verifyPrice(
                "2020-06-14T15:00:00",
                2,
                25.45,
                "2020-06-14T15:00:00",
                "2020-06-14T18:30:00"
        );
    }

    /**
     * Verifies that the end date of a price validity period
     * is inclusive.
     * At exactly 18:30, price list 2 must still apply.
     */
    @Test
    void shouldApplyPriceAtEndDate() throws Exception {
        verifyPrice(
                "2020-06-14T18:30:00",
                2,
                25.45,
                "2020-06-14T15:00:00",
                "2020-06-14T18:30:00"
        );
    }

    /**
     * Verifies that a product identifier equal to zero
     * is rejected with HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenProductIdIsZero() throws Exception {

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "0")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("INVALID_PARAMETERS"));
    }

    /**
     * Verifies that a brand identifier equal to zero
     * is rejected with HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenBrandIdIsZero() throws Exception {

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
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

        mockMvc.perform(get("/api/v1/prices")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

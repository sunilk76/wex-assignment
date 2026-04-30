package com.wex.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.transaction.dto.ConversionResponse;
import com.wex.transaction.dto.ExchangeRateResult;
import com.wex.transaction.exception.CurrencyConversionException;
import com.wex.transaction.exception.TransactionNotFoundException;
import com.wex.transaction.service.TreasuryExchangeRateService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PurchaseTransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Mocked so convert tests don't make real HTTP calls; store tests are unaffected.
    @MockBean TreasuryExchangeRateService treasuryExchangeRateService;

    // ── Store transaction ─────────────────────────────────────────────────────

    @Test
    void storeTransaction_validRequest_returns201WithId() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "description", "Office supplies",
                "transactionDate", "2024-07-04",
                "purchaseAmount", "49.99"
        ));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.description").value("Office supplies"))
                .andExpect(jsonPath("$.transactionDate").value("2024-07-04"))
                .andExpect(jsonPath("$.purchaseAmount").value(49.99));
    }

    @Test
    void storeTransaction_descriptionTooLong_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "description", "A".repeat(51),
                "transactionDate", "2024-07-04",
                "purchaseAmount", "10.00"
        ));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("Description must not exceed 50 characters"));
    }

    @Test
    void storeTransaction_negativeAmount_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "description", "Test purchase",
                "transactionDate", "2024-07-04",
                "purchaseAmount", "-5.00"
        ));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void storeTransaction_zeroAmount_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "description", "Test",
                "transactionDate", "2024-07-04",
                "purchaseAmount", "0.00"
        ));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void storeTransaction_invalidDate_returns400() throws Exception {
        String body = "{\"description\":\"Test\",\"transactionDate\":\"not-a-date\",\"purchaseAmount\":\"10.00\"}";

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void storeTransaction_amountWithMoreThanTwoDecimals_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "description", "Rounded amount test",
                "transactionDate", "2024-07-04",
                "purchaseAmount", "19.999"
        ));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void storeTransaction_missingDescription_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "transactionDate", "2024-07-04",
                "purchaseAmount", "10.00"
        ));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── Convert transaction ───────────────────────────────────────────────────

    @Test
    void convertTransaction_validRequest_returns200WithConvertedAmount() throws Exception {
        // Store a transaction first to get a real ID
        String storeBody = objectMapper.writeValueAsString(Map.of(
                "description", "Office supplies",
                "transactionDate", "2024-07-04",
                "purchaseAmount", "49.99"
        ));
        MvcResult storeResult = mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storeBody))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = storeResult.getResponse().getContentAsString();
        UUID transactionId = UUID.fromString(
                objectMapper.readTree(responseJson).get("id").asText());

        // Mock the treasury service to return a rate
        when(treasuryExchangeRateService.findRate(eq("Euro Zone-Euro"), any(LocalDate.class)))
                .thenReturn(Optional.of(new ExchangeRateResult(
                        "Euro Zone-Euro", new BigDecimal("0.921"), LocalDate.of(2024, 6, 30))));

        mockMvc.perform(get("/transactions/{id}/convert", transactionId)
                        .param("targetCurrency", "Euro Zone-Euro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.description").value("Office supplies"))
                .andExpect(jsonPath("$.transactionDate").value("2024-07-04"))
                .andExpect(jsonPath("$.purchaseAmountUsd").value(49.99))
                .andExpect(jsonPath("$.targetCurrency").value("Euro Zone-Euro"))
                .andExpect(jsonPath("$.exchangeRate").value(0.921))
                .andExpect(jsonPath("$.exchangeRateDate").value("2024-06-30"))
                .andExpect(jsonPath("$.convertedAmount").value(46.04));
    }

    @Test
    void convertTransaction_transactionNotFound_returns404() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/transactions/{id}/convert", unknownId)
                        .param("targetCurrency", "Euro Zone-Euro"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found: " + unknownId));
    }

    @Test
    void convertTransaction_noRateWithinSixMonths_returns422() throws Exception {
        // Store a transaction
        String storeBody = objectMapper.writeValueAsString(Map.of(
                "description", "Test purchase",
                "transactionDate", "2020-01-15",
                "purchaseAmount", "25.00"
        ));
        MvcResult storeResult = mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storeBody))
                .andExpect(status().isCreated())
                .andReturn();

        UUID transactionId = UUID.fromString(
                objectMapper.readTree(storeResult.getResponse().getContentAsString()).get("id").asText());

        when(treasuryExchangeRateService.findRate(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}/convert", transactionId)
                        .param("targetCurrency", "Obscure-Currency"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value(
                        org.hamcrest.Matchers.containsString("No exchange rate available")));
    }

    @Test
    void convertTransaction_missingTargetCurrency_returns400() throws Exception {
        UUID anyId = UUID.randomUUID();

        mockMvc.perform(get("/transactions/{id}/convert", anyId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Required parameter 'targetCurrency' is missing"));
    }

    @Test
    void convertTransaction_invalidUuidPath_returns400() throws Exception {
        mockMvc.perform(get("/transactions/not-a-uuid/convert")
                        .param("targetCurrency", "Euro Zone-Euro"))
                .andExpect(status().isBadRequest());
    }
}

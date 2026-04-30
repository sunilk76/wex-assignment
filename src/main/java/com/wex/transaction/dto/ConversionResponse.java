package com.wex.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ConversionResponse {

    private UUID id;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal purchaseAmountUsd;
    private String targetCurrency;
    private BigDecimal exchangeRate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exchangeRateDate;

    private BigDecimal convertedAmount;

    public ConversionResponse(UUID id, String description, LocalDate transactionDate,
                               BigDecimal purchaseAmountUsd, String targetCurrency,
                               BigDecimal exchangeRate, LocalDate exchangeRateDate,
                               BigDecimal convertedAmount) {
        this.id = id;
        this.description = description;
        this.transactionDate = transactionDate;
        this.purchaseAmountUsd = purchaseAmountUsd;
        this.targetCurrency = targetCurrency;
        this.exchangeRate = exchangeRate;
        this.exchangeRateDate = exchangeRateDate;
        this.convertedAmount = convertedAmount;
    }

    public UUID getId() { return id; }
    public String getDescription() { return description; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public BigDecimal getPurchaseAmountUsd() { return purchaseAmountUsd; }
    public String getTargetCurrency() { return targetCurrency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public LocalDate getExchangeRateDate() { return exchangeRateDate; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
}

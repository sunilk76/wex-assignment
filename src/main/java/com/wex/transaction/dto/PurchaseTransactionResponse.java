package com.wex.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wex.transaction.entity.PurchaseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PurchaseTransactionResponse {

    private UUID id;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal purchaseAmount;

    public static PurchaseTransactionResponse from(PurchaseTransaction transaction) {
        PurchaseTransactionResponse response = new PurchaseTransactionResponse();
        response.id = transaction.getId();
        response.description = transaction.getDescription();
        response.transactionDate = transaction.getTransactionDate();
        response.purchaseAmount = transaction.getPurchaseAmount();
        return response;
    }

    public UUID getId() { return id; }
    public String getDescription() { return description; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public BigDecimal getPurchaseAmount() { return purchaseAmount; }
}

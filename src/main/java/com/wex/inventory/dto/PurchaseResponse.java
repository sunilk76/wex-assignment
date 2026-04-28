package com.wex.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.wex.inventory.entity.Purchase;
import lombok.Data;

@Data
public class PurchaseResponse {
    private Long id;
    private String description;
    private LocalDate transactionDate;

    private BigDecimal originalAmountUsd;
    private BigDecimal exchangeRateUsed;
    private BigDecimal convertedAmount;
    private String currency;

    public PurchaseResponse(Purchase p, BigDecimal rate, BigDecimal converted, String currency){
        this.id = p.getId();
        this.description = p.getDescription();
        this.transactionDate = p.getTransactionDate();
        this.originalAmountUsd = p.getPurchaseAmount();
        this.exchangeRateUsed = rate;
        this.convertedAmount = converted;
        this.currency = currency;
    }
    
}

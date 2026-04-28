package com.wex.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.wex.inventory.entity.Purchase;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public BigDecimal getOriginalAmountUsd() {
		return originalAmountUsd;
	}

	public void setOriginalAmountUsd(BigDecimal originalAmountUsd) {
		this.originalAmountUsd = originalAmountUsd;
	}

	public BigDecimal getExchangeRateUsed() {
		return exchangeRateUsed;
	}

	public void setExchangeRateUsed(BigDecimal exchangeRateUsed) {
		this.exchangeRateUsed = exchangeRateUsed;
	}

	public BigDecimal getConvertedAmount() {
		return convertedAmount;
	}

	public void setConvertedAmount(BigDecimal convertedAmount) {
		this.convertedAmount = convertedAmount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
    
    
    
}

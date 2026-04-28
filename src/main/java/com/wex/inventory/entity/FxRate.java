package com.wex.inventory.entity;

import java.math.BigDecimal;

import java.time.LocalDate;


public class FxRate {
    
     private String currencyCode;   

    private LocalDate rateDate;    

    private BigDecimal rate;

    private LocalDate date;

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public LocalDate getRateDate() {
		return rateDate;
	}

	public void setRateDate(LocalDate rateDate) {
		this.rateDate = rateDate;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
    
    
}

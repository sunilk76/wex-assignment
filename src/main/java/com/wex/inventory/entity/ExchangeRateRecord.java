package com.wex.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateRecord {

    @JsonProperty("record_date")
    private LocalDate recordDate;

    private String country;
    private String currency;

    @JsonProperty("country_currency_desc")
    private String countryCurrencyDesc;

    @JsonProperty("exchange_rate")
    private BigDecimal exchangeRate;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("src_line_nbr")
    private Integer sourceLineNumber;

    @JsonProperty("record_fiscal_year")
    private Integer fiscalYear;

    @JsonProperty("record_fiscal_quarter")
    private Integer fiscalQuarter;

    @JsonProperty("record_calendar_year")
    private Integer calendarYear;

    @JsonProperty("record_calendar_quarter")
    private Integer calendarQuarter;

    @JsonProperty("record_calendar_month")
    private Integer calendarMonth;

    @JsonProperty("record_calendar_day")
    private Integer calendarDay;

	public LocalDate getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(LocalDate recordDate) {
		this.recordDate = recordDate;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCountryCurrencyDesc() {
		return countryCurrencyDesc;
	}

	public void setCountryCurrencyDesc(String countryCurrencyDesc) {
		this.countryCurrencyDesc = countryCurrencyDesc;
	}

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public LocalDate getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(LocalDate effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Integer getSourceLineNumber() {
		return sourceLineNumber;
	}

	public void setSourceLineNumber(Integer sourceLineNumber) {
		this.sourceLineNumber = sourceLineNumber;
	}

	public Integer getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(Integer fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	public Integer getFiscalQuarter() {
		return fiscalQuarter;
	}

	public void setFiscalQuarter(Integer fiscalQuarter) {
		this.fiscalQuarter = fiscalQuarter;
	}

	public Integer getCalendarYear() {
		return calendarYear;
	}

	public void setCalendarYear(Integer calendarYear) {
		this.calendarYear = calendarYear;
	}

	public Integer getCalendarQuarter() {
		return calendarQuarter;
	}

	public void setCalendarQuarter(Integer calendarQuarter) {
		this.calendarQuarter = calendarQuarter;
	}

	public Integer getCalendarMonth() {
		return calendarMonth;
	}

	public void setCalendarMonth(Integer calendarMonth) {
		this.calendarMonth = calendarMonth;
	}

	public Integer getCalendarDay() {
		return calendarDay;
	}

	public void setCalendarDay(Integer calendarDay) {
		this.calendarDay = calendarDay;
	}
    
}

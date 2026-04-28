package com.wex.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
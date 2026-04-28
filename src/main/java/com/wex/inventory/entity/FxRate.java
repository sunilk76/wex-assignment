package com.wex.inventory.entity;

import java.math.BigDecimal;

import java.time.LocalDate;

import lombok.Data;

@Data
public class FxRate {
    
     private String currencyCode;   

    private LocalDate rateDate;    

    private BigDecimal rate;

    private LocalDate date;
}

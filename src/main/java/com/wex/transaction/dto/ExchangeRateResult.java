package com.wex.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResult(String currency, BigDecimal rate, LocalDate rateDate) {}

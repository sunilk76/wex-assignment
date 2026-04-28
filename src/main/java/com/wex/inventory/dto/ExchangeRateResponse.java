package com.wex.inventory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wex.inventory.entity.ExchangeRateRecord;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {

    private List<ExchangeRateRecord> data;
}
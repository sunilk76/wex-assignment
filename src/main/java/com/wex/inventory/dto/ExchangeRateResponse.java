package com.wex.inventory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wex.inventory.entity.ExchangeRateRecord;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {

    private List<ExchangeRateRecord> data;

	public List<ExchangeRateRecord> getData() {
		return data;
	}

	public void setData(List<ExchangeRateRecord> data) {
		this.data = data;
	}
    
}
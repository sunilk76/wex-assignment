package com.wex.inventory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.wex.inventory.dto.ExchangeRateResponse;
import com.wex.inventory.entity.FxRate;

@Service
public class FxRateClient {
    @Autowired
    RestTemplate restTemplate;

    public List<FxRate> getRates(String currency) {

        // Call Treasury API
        String url = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?filter=record_date:gte:2021-04-01,record_date:lte:2026-03-31";
        ExchangeRateResponse list = restTemplate.getForObject(url, ExchangeRateResponse.class);
        System.out.println("List is \n"+list);
        // Parse JSON → map to FxRate objects
        return new ArrayList<>();
    }
}

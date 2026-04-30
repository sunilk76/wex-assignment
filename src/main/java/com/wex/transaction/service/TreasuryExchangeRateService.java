package com.wex.transaction.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wex.transaction.dto.ExchangeRateResult;
import com.wex.transaction.exception.TreasuryApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TreasuryExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(TreasuryExchangeRateService.class);
    private static final String EXCHANGE_RATE_PATH = "/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TreasuryExchangeRateService(RestTemplate restTemplate,
                                        @Value("${treasury.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the most recent exchange rate for the given currency on or before the purchase date,
     * within a 6-month window. Returns empty if no rate exists in that window.
     */
    public Optional<ExchangeRateResult> findRate(String currency, LocalDate purchaseDate) {
        LocalDate sixMonthsAgo = purchaseDate.minusMonths(6);
        URI uri = buildUri(currency, purchaseDate, sixMonthsAgo);
        log.info("Calling Treasury exchange rate API: {}", uri);

        try {
            TreasuryApiResponse response = restTemplate.getForObject(uri, TreasuryApiResponse.class);
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                log.info("No exchange rate found for '{}' between {} and {}", currency, sixMonthsAgo, purchaseDate);
                return Optional.empty();
            }
            RateData rateData = response.getData().get(0);
            log.info("Found exchange rate {} for '{}' dated {}", rateData.getExchangeRate(), currency, rateData.getRecordDate());
            return Optional.of(new ExchangeRateResult(
                    currency,
                    new BigDecimal(rateData.getExchangeRate()),
                    LocalDate.parse(rateData.getRecordDate())
            ));
        } catch (HttpClientErrorException e) {
            log.error("Treasury API rejected request [{}] for currency '{}': HTTP {} - {}",
                    uri, currency, e.getStatusCode(), e.getResponseBodyAsString());
            throw new TreasuryApiException("Treasury API returned a client error: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Treasury API server error for currency '{}': HTTP {}", currency, e.getStatusCode());
            throw new TreasuryApiException("Treasury API exchange rate service returned a server error", e);
        } catch (ResourceAccessException e) {
            log.error("Cannot reach Treasury API at [{}]: {}", uri, e.getMessage());
            throw new TreasuryApiException("Cannot connect to Treasury exchange rate service — check network access", e);
        } catch (RestClientException e) {
            log.error("Unexpected error calling Treasury API [{}]: {}", uri, e.getMessage(), e);
            throw new TreasuryApiException("Failed to retrieve exchange rates from Treasury API", e);
        }
    }

    private URI buildUri(String currency, LocalDate purchaseDate, LocalDate sixMonthsAgo) {
        return UriComponentsBuilder
                .fromHttpUrl(baseUrl + EXCHANGE_RATE_PATH)
                .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
                .queryParam("filter",
                        "country_currency_desc:eq:{currency},record_date:lte:{lte},record_date:gte:{gte}")
                .queryParam("sort", "-record_date")
                .queryParam("page[size]", "1")
                .buildAndExpand(Map.of("currency", currency, "lte", purchaseDate, "gte", sixMonthsAgo))
                .encode()
                .toUri();
    }

    static class TreasuryApiResponse {
        private List<RateData> data;
        private Meta meta;

        public List<RateData> getData() { return data; }
        public void setData(List<RateData> data) { this.data = data; }
        public Meta getMeta() { return meta; }
        public void setMeta(Meta meta) { this.meta = meta; }
    }

    static class RateData {
        @JsonProperty("country_currency_desc")
        private String countryCurrencyDesc;
        @JsonProperty("exchange_rate")
        private String exchangeRate;
        @JsonProperty("record_date")
        private String recordDate;

        public String getCountryCurrencyDesc() { return countryCurrencyDesc; }
        public void setCountryCurrencyDesc(String v) { this.countryCurrencyDesc = v; }
        public String getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(String v) { this.exchangeRate = v; }
        public String getRecordDate() { return recordDate; }
        public void setRecordDate(String v) { this.recordDate = v; }
    }

    static class Meta {
        @JsonProperty("total-count")
        private int totalCount;

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int v) { this.totalCount = v; }
    }
}

package com.wex.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.wex.transaction.dto.ExchangeRateResult;
import com.wex.transaction.exception.TreasuryApiException;
import com.wex.transaction.service.TreasuryExchangeRateService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
class TreasuryExchangeRateServiceTest {

    @Autowired
    private TreasuryExchangeRateService service;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void findRate_validCurrencyAndDate_returnsRate() {
        mockServer.expect(requestTo(containsString("rates_of_exchange")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(SINGLE_RATE_RESPONSE, MediaType.APPLICATION_JSON));

        Optional<ExchangeRateResult> result = service.findRate("Euro Zone-Euro", LocalDate.of(2024, 7, 4));

        assertThat(result).isPresent();
        assertThat(result.get().rate()).isEqualByComparingTo("0.921");
        assertThat(result.get().rateDate()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(result.get().currency()).isEqualTo("Euro Zone-Euro");
        mockServer.verify();
    }

    @Test
    void findRate_noDataReturned_returnsEmpty() {
        mockServer.expect(requestTo(containsString("rates_of_exchange")))
                .andRespond(withSuccess(EMPTY_DATA_RESPONSE, MediaType.APPLICATION_JSON));

        Optional<ExchangeRateResult> result = service.findRate("Unknown-Currency", LocalDate.of(2024, 7, 4));

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void findRate_requestFilterIncludesCurrencyName() {
        mockServer.expect(requestTo(containsString("country_currency_desc:eq:Japan-Yen")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(EMPTY_DATA_RESPONSE, MediaType.APPLICATION_JSON));

        service.findRate("Japan-Yen", LocalDate.of(2024, 7, 4));
        mockServer.verify();
    }

    @Test
    void findRate_requestFilterIncludesSixMonthDateWindow() {
        LocalDate purchaseDate = LocalDate.of(2024, 7, 4);
        LocalDate sixMonthsAgo = purchaseDate.minusMonths(6); // 2024-01-04

        mockServer.expect(requestTo(allOf(
                        containsString("record_date:lte:" + purchaseDate),
                        containsString("record_date:gte:" + sixMonthsAgo)
                )))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(EMPTY_DATA_RESPONSE, MediaType.APPLICATION_JSON));

        service.findRate("Euro Zone-Euro", purchaseDate);
        mockServer.verify();
    }

    @Test
    void findRate_requestSortsByDateDescending() {
        mockServer.expect(requestTo(containsString("sort=-record_date")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(EMPTY_DATA_RESPONSE, MediaType.APPLICATION_JSON));

        service.findRate("Euro Zone-Euro", LocalDate.of(2024, 7, 4));
        mockServer.verify();
    }

    @Test
    void findRate_requestLimitsToOnePage() {
        mockServer.expect(requestTo(containsString("page%5Bsize%5D=1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(EMPTY_DATA_RESPONSE, MediaType.APPLICATION_JSON));

        service.findRate("Euro Zone-Euro", LocalDate.of(2024, 7, 4));
        mockServer.verify();
    }

    @Test
    void findRate_apiServerError_throwsTreasuryApiException() {
        mockServer.expect(requestTo(containsString("rates_of_exchange")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.findRate("Euro Zone-Euro", LocalDate.of(2024, 7, 4)))
                .isInstanceOf(TreasuryApiException.class)
                .hasMessageContaining("Treasury API");
        mockServer.verify();
    }

    @Test
    void findRate_currencyWithSpaces_urlEncodesName() {
        mockServer.expect(requestTo(containsString("Euro%20Zone-Euro")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(SINGLE_RATE_RESPONSE, MediaType.APPLICATION_JSON));

        service.findRate("Euro Zone-Euro", LocalDate.of(2024, 7, 4));
        mockServer.verify();
    }

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private static final String SINGLE_RATE_RESPONSE = """
            {
              "data": [
                {
                  "country_currency_desc": "Euro Zone-Euro",
                  "exchange_rate": "0.921",
                  "record_date": "2024-06-30"
                }
              ],
              "meta": { "total-count": 1 }
            }
            """;

    private static final String EMPTY_DATA_RESPONSE = """
            {
              "data": [],
              "meta": { "total-count": 0 }
            }
            """;
}

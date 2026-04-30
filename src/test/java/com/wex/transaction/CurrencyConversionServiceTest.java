package com.wex.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wex.transaction.dto.ConversionResponse;
import com.wex.transaction.dto.ExchangeRateResult;
import com.wex.transaction.entity.PurchaseTransaction;
import com.wex.transaction.exception.CurrencyConversionException;
import com.wex.transaction.exception.TransactionNotFoundException;
import com.wex.transaction.repository.PurchaseTransactionRepository;
import com.wex.transaction.service.CurrencyConversionService;
import com.wex.transaction.service.TreasuryExchangeRateService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    @Mock
    private PurchaseTransactionRepository repository;

    @Mock
    private TreasuryExchangeRateService treasuryService;

    @InjectMocks
    private CurrencyConversionService service;

    private static final UUID TRANSACTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDate PURCHASE_DATE = LocalDate.of(2024, 7, 4);
    private static final String EUR = "Euro Zone-Euro";

    @Test
    void convert_validTransactionAndRate_returnsConversionResponse() {
        PurchaseTransaction tx = mockTransaction(TRANSACTION_ID, "Office supplies", PURCHASE_DATE, new BigDecimal("49.99"));
        ExchangeRateResult rate = new ExchangeRateResult(EUR, new BigDecimal("0.921"), LocalDate.of(2024, 6, 30));

        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));
        when(treasuryService.findRate(EUR, PURCHASE_DATE)).thenReturn(Optional.of(rate));

        ConversionResponse response = service.convert(TRANSACTION_ID, EUR);

        assertThat(response.getId()).isEqualTo(TRANSACTION_ID);
        assertThat(response.getDescription()).isEqualTo("Office supplies");
        assertThat(response.getTransactionDate()).isEqualTo(PURCHASE_DATE);
        assertThat(response.getPurchaseAmountUsd()).isEqualByComparingTo("49.99");
        assertThat(response.getTargetCurrency()).isEqualTo(EUR);
        assertThat(response.getExchangeRate()).isEqualByComparingTo("0.921");
        assertThat(response.getExchangeRateDate()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(response.getConvertedAmount()).isEqualByComparingTo("46.04");
        
        verify(repository).findById(TRANSACTION_ID);
        verify(treasuryService).findRate(EUR, PURCHASE_DATE);
    }

    @Test
    void convert_transactionNotFound_throwsNotFoundException() {
        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert(TRANSACTION_ID, EUR))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(TRANSACTION_ID.toString());

        verifyNoInteractions(treasuryService);
    }

    @Test
    void convert_noExchangeRateWithinSixMonths_throwsCurrencyConversionException() {
        PurchaseTransaction tx = mock(PurchaseTransaction.class);
        
        when(tx.getTransactionDate()).thenReturn(PURCHASE_DATE);
        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));
        when(treasuryService.findRate(EUR, PURCHASE_DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert(TRANSACTION_ID, EUR))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessageContaining(EUR)
                .hasMessageContaining(PURCHASE_DATE.toString());
        
        verify(repository).findById(TRANSACTION_ID);
        verify(treasuryService).findRate(EUR, PURCHASE_DATE);
    }

    @Test
    void convert_convertedAmountRoundsDown() {
        PurchaseTransaction tx = mockTransaction(TRANSACTION_ID, "Test", PURCHASE_DATE, new BigDecimal("10.00"));
        ExchangeRateResult rate = new ExchangeRateResult(EUR, new BigDecimal("0.9213"), PURCHASE_DATE);

        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));
        when(treasuryService.findRate(EUR, PURCHASE_DATE)).thenReturn(Optional.of(rate));

        assertThat(service.convert(TRANSACTION_ID, EUR).getConvertedAmount())
                .isEqualByComparingTo("9.21");
        
        verify(repository).findById(TRANSACTION_ID);
        verify(treasuryService).findRate(EUR, PURCHASE_DATE);
    }

    @Test
    void convert_convertedAmountRoundsHalfUp() {
        PurchaseTransaction tx = mockTransaction(TRANSACTION_ID, "Test", PURCHASE_DATE, new BigDecimal("10.00"));
        ExchangeRateResult rate = new ExchangeRateResult(EUR, new BigDecimal("0.9215"), PURCHASE_DATE);

        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));
        when(treasuryService.findRate(EUR, PURCHASE_DATE)).thenReturn(Optional.of(rate));

        assertThat(service.convert(TRANSACTION_ID, EUR).getConvertedAmount())
                .isEqualByComparingTo("9.22");
        
        verify(repository).findById(TRANSACTION_ID);
        verify(treasuryService).findRate(EUR, PURCHASE_DATE);
    }

    @Test
    void convert_largeExchangeRate_calculatesCorrectly() {
        // e.g. Japanese Yen: 1 USD ≈ 156 JPY
        // 49.99 * 156.32 = 7814.4368 → 7814.44
        PurchaseTransaction tx = mockTransaction(TRANSACTION_ID, "Test", PURCHASE_DATE, new BigDecimal("49.99"));
        ExchangeRateResult rate = new ExchangeRateResult("Japan-Yen", new BigDecimal("156.32"), PURCHASE_DATE);

        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));
        when(treasuryService.findRate("Japan-Yen", PURCHASE_DATE)).thenReturn(Optional.of(rate));

        assertThat(service.convert(TRANSACTION_ID, "Japan-Yen").getConvertedAmount())
                .isEqualByComparingTo("7814.44");
        
        verify(repository).findById(TRANSACTION_ID);
        verify(treasuryService).findRate("Japan-Yen", PURCHASE_DATE);
    }

    @Test
    void convert_passesPurchaseDateToTreasuryService() {
        PurchaseTransaction tx = mockTransaction(TRANSACTION_ID, "Test", PURCHASE_DATE, new BigDecimal("10.00"));
        ExchangeRateResult rate = new ExchangeRateResult(EUR, new BigDecimal("0.92"), PURCHASE_DATE);

        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));
        when(treasuryService.findRate(EUR, PURCHASE_DATE)).thenReturn(Optional.of(rate));

        service.convert(TRANSACTION_ID, EUR);

        verify(repository).findById(TRANSACTION_ID);
        verify(treasuryService).findRate(EUR, PURCHASE_DATE);
    }

    private PurchaseTransaction mockTransaction(UUID id, String description,
                                                 LocalDate date, BigDecimal amount) {
        PurchaseTransaction tx = mock(PurchaseTransaction.class);
        when(tx.getId()).thenReturn(id);
        when(tx.getDescription()).thenReturn(description);
        when(tx.getTransactionDate()).thenReturn(date);
        when(tx.getPurchaseAmount()).thenReturn(amount);
        return tx;
    }
}

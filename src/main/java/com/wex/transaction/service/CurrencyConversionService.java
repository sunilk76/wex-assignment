package com.wex.transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wex.transaction.dto.ConversionResponse;
import com.wex.transaction.dto.ExchangeRateResult;
import com.wex.transaction.entity.PurchaseTransaction;
import com.wex.transaction.exception.CurrencyConversionException;
import com.wex.transaction.exception.TransactionNotFoundException;
import com.wex.transaction.repository.PurchaseTransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class CurrencyConversionService {

    private final PurchaseTransactionRepository repository;
    private final TreasuryExchangeRateService treasuryService;

    public CurrencyConversionService(PurchaseTransactionRepository repository,
                                      TreasuryExchangeRateService treasuryService) {
        this.repository = repository;
        this.treasuryService = treasuryService;
    }

    @Transactional(readOnly = true)
    public ConversionResponse convert(UUID transactionId, String targetCurrency) {
        PurchaseTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        ExchangeRateResult rateResult = treasuryService
                .findRate(targetCurrency, transaction.getTransactionDate())
                .orElseThrow(() -> new CurrencyConversionException(
                        "No exchange rate available for '%s' within 6 months on or before %s"
                                .formatted(targetCurrency, transaction.getTransactionDate())
                ));

        BigDecimal convertedAmount = transaction.getPurchaseAmount()
                .multiply(rateResult.rate())
                .setScale(2, RoundingMode.HALF_UP);

        return new ConversionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount(),
                targetCurrency,
                rateResult.rate(),
                rateResult.rateDate(),
                convertedAmount
        );
    }
}

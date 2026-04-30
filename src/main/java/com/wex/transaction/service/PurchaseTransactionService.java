package com.wex.transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wex.transaction.dto.CreateTransactionRequest;
import com.wex.transaction.entity.PurchaseTransaction;
import com.wex.transaction.repository.PurchaseTransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PurchaseTransactionService {

    private final PurchaseTransactionRepository repository;

    public PurchaseTransactionService(PurchaseTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PurchaseTransaction store(CreateTransactionRequest request) {
        // Round to nearest cent before persisting
        BigDecimal roundedAmount = request.getPurchaseAmount()
                .setScale(2, RoundingMode.HALF_UP);

        PurchaseTransaction transaction = new PurchaseTransaction(
                request.getDescription().trim(),
                request.getTransactionDate(),
                roundedAmount
        );

        return repository.save(transaction);
    }
}

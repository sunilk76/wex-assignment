package com.wex.transaction.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wex.transaction.dto.ConversionResponse;
import com.wex.transaction.dto.CreateTransactionRequest;
import com.wex.transaction.dto.PurchaseTransactionResponse;
import com.wex.transaction.entity.PurchaseTransaction;
import com.wex.transaction.service.CurrencyConversionService;
import com.wex.transaction.service.PurchaseTransactionService;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class PurchaseTransactionController {

    private final PurchaseTransactionService service;
    private final CurrencyConversionService conversionService;

    public PurchaseTransactionController(PurchaseTransactionService service,
                                          CurrencyConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<PurchaseTransactionResponse> store(
            @Valid @RequestBody CreateTransactionRequest request) {
        PurchaseTransaction saved = service.store(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PurchaseTransactionResponse.from(saved));
    }

    @GetMapping("/{id}/convert")
    public ResponseEntity<ConversionResponse> convert(
            @PathVariable UUID id,
            @RequestParam String targetCurrency) {
        return ResponseEntity.ok(conversionService.convert(id, targetCurrency));
    }
}

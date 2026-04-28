package com.wex.inventory.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wex.inventory.dto.PurchaseResponse;
import com.wex.inventory.entity.FxRate;
import com.wex.inventory.entity.Purchase;
import com.wex.inventory.repository.PurchaseRepository;

@Service
public class PurchaseService {
        @Autowired
        private PurchaseRepository repo;
        @Autowired
        private FxRateClient fxClient;

        public PurchaseResponse convert(Long id, String currency) {

                Purchase p = repo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Purchase not found"));

                List<FxRate> rates = fxClient.getRates(currency);

                LocalDate maxDate = p.getTransactionDate();
                LocalDate minDate = maxDate.minusMonths(6);

                FxRate rate = rates.stream()
                                .filter(r -> r.getCurrencyCode().equalsIgnoreCase(currency))
                                .filter(r -> !r.getDate().isAfter(maxDate)) // ≤ purchase date
                                .filter(r -> !r.getDate().isBefore(minDate)) // last 6 months
                                .max(Comparator.comparing(FxRate::getDate)) // closest valid
                                .orElseThrow(() -> new RuntimeException("No FX rate available in last 6 months"));

                BigDecimal converted = p.getPurchaseAmount()
                                .multiply(rate.getRate())
                                .setScale(2, RoundingMode.HALF_UP);

                return new PurchaseResponse(p, rate.getRate(), converted, currency);
        }
}
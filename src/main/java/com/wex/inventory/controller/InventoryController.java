package com.wex.inventory.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wex.inventory.dto.PurchaseResponse;
import com.wex.inventory.entity.Purchase;
import com.wex.inventory.service.InventoryService;
import com.wex.inventory.service.PurchaseService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/v1")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/purchases")
    public ResponseEntity<Long> savePurchase(@RequestBody Purchase purchase) {
        long id = this.inventoryService.savePurchase(purchase);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/currency/{currency}")
    public PurchaseResponse getInCurrency(@PathVariable String currency) {
        return purchaseService.convert(1l, currency);
    }
}

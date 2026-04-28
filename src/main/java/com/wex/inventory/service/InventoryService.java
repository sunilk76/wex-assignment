package com.wex.inventory.service;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wex.inventory.entity.Purchase;

@Service
public class InventoryService {
    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Long savePurchase(Purchase purchase){
        this.entityManager.persist(purchase);
        return purchase.getId();
    }
}
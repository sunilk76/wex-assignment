package com.wex.inventory.repository;

import com.wex.inventory.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
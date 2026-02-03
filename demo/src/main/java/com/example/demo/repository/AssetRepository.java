package com.example.demo.repository;

import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Asset entity CRUD operations.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Find all assets of a specific type.
     */
    List<Asset> findByType(AssetType type);

    /**
     * Find assets where symbol contains the search term (case insensitive).
     */
    List<Asset> findBySymbolContainingIgnoreCase(String symbol);

    /**
     * Find assets where name contains the search term (case insensitive).
     */
    List<Asset> findByNameContainingIgnoreCase(String name);

    /**
     * Find assets created within a date range.
     */
    List<Asset> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find assets by symbol (exact match, case insensitive).
     */
    List<Asset> findBySymbolIgnoreCase(String symbol);

    /**
     * Get total portfolio value (sum of quantity * buy_price for all assets).
     */
    @Query("SELECT COALESCE(SUM(a.quantity * a.buyPrice), 0) FROM Asset a")
    java.math.BigDecimal getTotalCostBasis();

    /**
     * Count assets by type.
     */
    long countByType(AssetType type);
}

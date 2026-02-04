package com.example.demo.repository;

import com.example.demo.entity.BaseAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.math.BigDecimal;
import java.util.List;

/**
 * Base repository interface for all asset types.
 * Use @NoRepositoryBean to prevent Spring from creating an instance.
 */
@NoRepositoryBean
public interface BaseAssetRepository<T extends BaseAsset> extends JpaRepository<T, Long> {

    List<T> findBySymbolContainingIgnoreCase(String symbol);

    List<T> findByNameContainingIgnoreCase(String name);

    @Query("SELECT COALESCE(SUM(a.quantity * a.buyPrice), 0) FROM #{#entityName} a")
    BigDecimal calculateTotalCostBasis();
}
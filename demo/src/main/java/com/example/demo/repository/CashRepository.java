package com.example.demo.repository;

import com.example.demo.entity.Cash;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Cash entities.
 */
@Repository
public interface CashRepository extends BaseAssetRepository<Cash> {

    Optional<Cash> findBySymbolIgnoreCase(String symbol);

    List<Cash> findByCurrencyIgnoreCase(String currency);

    List<Cash> findByAccountType(Cash.AccountType accountType);

    List<Cash> findByBankNameIgnoreCase(String bankName);
}
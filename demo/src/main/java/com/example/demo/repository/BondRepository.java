package com.example.demo.repository;

import com.example.demo.entity.Bond;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Bond entities.
 */
@Repository
public interface BondRepository extends BaseAssetRepository<Bond> {

    Optional<Bond> findBySymbolIgnoreCase(String symbol);

    List<Bond> findByIssuerContainingIgnoreCase(String issuer);

    List<Bond> findByBondType(Bond.BondType bondType);

    List<Bond> findByMaturityDateBefore(LocalDate date);

    List<Bond> findByMaturityDateAfter(LocalDate date);

    List<Bond> findByCreditRating(String creditRating);
}
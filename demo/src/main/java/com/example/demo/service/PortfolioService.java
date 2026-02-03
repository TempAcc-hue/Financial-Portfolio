package com.example.demo.service;

import com.example.demo.dto.AssetDTO;
import com.example.demo.dto.PortfolioSummaryDTO;
import com.example.demo.entity.AssetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for portfolio-level analytics and calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioService {

    private final AssetService assetService;

    /**
     * Get comprehensive portfolio summary with all analytics.
     */
    public PortfolioSummaryDTO getPortfolioSummary() {
        List<AssetDTO> allAssets = assetService.getAllAssets();

        if (allAssets.isEmpty()) {
            return PortfolioSummaryDTO.builder()
                    .totalValue(BigDecimal.ZERO)
                    .totalCostBasis(BigDecimal.ZERO)
                    .totalGainLoss(BigDecimal.ZERO)
                    .totalGainLossPercentage(BigDecimal.ZERO)
                    .totalAssets(0L)
                    .assetCountByType(Collections.emptyMap())
                    .allocationByType(Collections.emptyMap())
                    .valueByType(Collections.emptyMap())
                    .assets(allAssets)
                    .topGainers(Collections.emptyList())
                    .topLosers(Collections.emptyList())
                    .build();
        }

        // Calculate totals
        BigDecimal totalValue = allAssets.stream()
                .map(a -> a.getCurrentValue() != null ? a.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostBasis = allAssets.stream()
                .map(a -> a.getCostBasis() != null ? a.getCostBasis() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGainLoss = totalValue.subtract(totalCostBasis);

        BigDecimal totalGainLossPercentage = BigDecimal.ZERO;
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            totalGainLossPercentage = totalGainLoss
                    .divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Calculate value by type
        Map<String, BigDecimal> valueByType = allAssets.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getType().name(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                a -> a.getCurrentValue() != null ? a.getCurrentValue() : BigDecimal.ZERO,
                                BigDecimal::add)));

        // Calculate allocation percentages
        Map<String, BigDecimal> allocationByType = new HashMap<>();
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : valueByType.entrySet()) {
                BigDecimal percentage = entry.getValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                allocationByType.put(entry.getKey(), percentage);
            }
        }

        // Count assets by type
        Map<String, Long> assetCountByType = allAssets.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getType().name(),
                        Collectors.counting()));

        // Find top gainers (sorted by gainLossPercentage descending)
        List<AssetDTO> topGainers = allAssets.stream()
                .filter(a -> a.getGainLossPercentage() != null
                        && a.getGainLossPercentage().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(AssetDTO::getGainLossPercentage).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Find top losers (sorted by gainLossPercentage ascending)
        List<AssetDTO> topLosers = allAssets.stream()
                .filter(a -> a.getGainLossPercentage() != null
                        && a.getGainLossPercentage().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(AssetDTO::getGainLossPercentage))
                .limit(5)
                .collect(Collectors.toList());

        return PortfolioSummaryDTO.builder()
                .totalValue(totalValue)
                .totalCostBasis(totalCostBasis)
                .totalGainLoss(totalGainLoss)
                .totalGainLossPercentage(totalGainLossPercentage)
                .totalAssets((long) allAssets.size())
                .assetCountByType(assetCountByType)
                .allocationByType(allocationByType)
                .valueByType(valueByType)
                .assets(allAssets)
                .topGainers(topGainers)
                .topLosers(topLosers)
                .build();
    }

    /**
     * Get just the allocation data for pie charts.
     */
    public Map<String, BigDecimal> getAllocation() {
        PortfolioSummaryDTO summary = getPortfolioSummary();
        return summary.getAllocationByType();
    }

    /**
     * Get performance data for each asset type.
     */
    public Map<String, Map<String, BigDecimal>> getPerformanceByType() {
        List<AssetDTO> allAssets = assetService.getAllAssets();

        Map<String, Map<String, BigDecimal>> performanceByType = new HashMap<>();

        for (AssetType type : AssetType.values()) {
            List<AssetDTO> assetsOfType = allAssets.stream()
                    .filter(a -> a.getType() == type)
                    .collect(Collectors.toList());

            if (!assetsOfType.isEmpty()) {
                BigDecimal totalValue = assetsOfType.stream()
                        .map(a -> a.getCurrentValue() != null ? a.getCurrentValue() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCost = assetsOfType.stream()
                        .map(a -> a.getCostBasis() != null ? a.getCostBasis() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal gainLoss = totalValue.subtract(totalCost);

                Map<String, BigDecimal> typePerformance = new HashMap<>();
                typePerformance.put("value", totalValue);
                typePerformance.put("cost", totalCost);
                typePerformance.put("gainLoss", gainLoss);

                if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = gainLoss
                            .divide(totalCost, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    typePerformance.put("percentage", percentage);
                }

                performanceByType.put(type.name(), typePerformance);
            }
        }

        return performanceByType;
    }
}

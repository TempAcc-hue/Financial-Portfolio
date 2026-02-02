package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for portfolio summary and analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSummaryDTO {

    // Total values
    private BigDecimal totalValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalGainLoss;
    private BigDecimal totalGainLossPercentage;

    // Asset counts
    private long totalAssets;
    private Map<String, Long> assetCountByType;

    // Allocation breakdown (type -> percentage)
    private Map<String, BigDecimal> allocationByType;

    // Allocation by value (type -> total value)
    private Map<String, BigDecimal> valueByType;

    // All assets with enriched data
    private List<AssetDTO> assets;

    // Top performers
    private List<AssetDTO> topGainers;
    private List<AssetDTO> topLosers;
}

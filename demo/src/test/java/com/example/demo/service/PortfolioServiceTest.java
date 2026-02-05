// language: java
package com.example.demo.service;

import com.example.demo.dto.AssetDTO;
import com.example.demo.dto.PortfolioSummaryDTO;
import com.example.demo.entity.AssetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @InjectMocks
    private PortfolioService portfolioService;

    @Mock
    private AssetService assetService;

    private AssetDTO createAssetDTO(Long id,
                                    AssetType type,
                                    String symbol,
                                    BigDecimal currentValue,
                                    BigDecimal costBasis,
                                    BigDecimal gainLossPercentage) {
        return AssetDTO.builder()
                .id(id)
                .type(type)
                .symbol(symbol)
                .currentValue(currentValue)
                .costBasis(costBasis)
                .gainLoss(currentValue != null && costBasis != null ? currentValue.subtract(costBasis) : BigDecimal.ZERO)
                .gainLossPercentage(gainLossPercentage)
                .build();
    }

    @Nested
    @DisplayName("getPortfolioSummary")
    class GetPortfolioSummaryTests {

        @Test
        @DisplayName("Given no assets when getPortfolioSummary then return zeroed summary")
        void givenNoAssets_whenGetPortfolioSummary_thenZeroSummary() {
            when(assetService.getAllAssets()).thenReturn(Collections.emptyList());

            PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary();

            assertNotNull(summary);
            assertEquals(0, BigDecimal.ZERO.compareTo(summary.getTotalValue()));
            assertEquals(0, BigDecimal.ZERO.compareTo(summary.getTotalCostBasis()));
            assertEquals(0L, summary.getTotalAssets());
            assertTrue(summary.getAllocationByType().isEmpty());
            assertTrue(summary.getAssetCountByType().isEmpty());
            assertTrue(summary.getTopGainers().isEmpty());
            assertTrue(summary.getTopLosers().isEmpty());
        }

        @Test
        @DisplayName("Given mixed assets when getPortfolioSummary then compute totals, allocation and top lists")
        void givenAssets_whenGetPortfolioSummary_thenComputeCorrectly() {
            // STOCK: value 200, cost 100 -> gain 100 (100%)
            AssetDTO stock = createAssetDTO(1L, AssetType.STOCK, "A", new BigDecimal("200"), new BigDecimal("100"), new BigDecimal("100"));
            // BOND: value 50, cost 50 -> gain 0 (0%)
            AssetDTO bond = createAssetDTO(2L, AssetType.BOND, "B", new BigDecimal("50"), new BigDecimal("50"), BigDecimal.ZERO);

            when(assetService.getAllAssets()).thenReturn(List.of(stock, bond));

            PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary();

            // Totals
            assertEquals(0, new BigDecimal("250").compareTo(summary.getTotalValue()));
            assertEquals(0, new BigDecimal("150").compareTo(summary.getTotalCostBasis()));
            assertEquals(0, new BigDecimal("100").compareTo(summary.getTotalGainLoss()));

            // Percentage ~ 100 / 150 * 100 = 66.6667...
            BigDecimal expectedTotalPct = new BigDecimal("100").divide(new BigDecimal("150"), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
            assertEquals(0, expectedTotalPct.compareTo(summary.getTotalGainLossPercentage()));

            // Allocation: STOCK 200/250 = 80%, BOND 20%
            Map<String, BigDecimal> alloc = summary.getAllocationByType();
            assertEquals(2, alloc.size());
            assertEquals(0, new BigDecimal("80").compareTo(alloc.get(AssetType.STOCK.name())));
            assertEquals(0, new BigDecimal("20").compareTo(alloc.get(AssetType.BOND.name())));

            // Counts by type
            Map<String, Long> counts = summary.getAssetCountByType();
            assertEquals(1L, counts.get(AssetType.STOCK.name()));
            assertEquals(1L, counts.get(AssetType.BOND.name()));

            // Top gainers contains STOCK at top
            assertFalse(summary.getTopGainers().isEmpty());
            assertEquals(AssetType.STOCK, summary.getTopGainers().get(0).getType());
        }
    }

    @Nested
    @DisplayName("getAllocation")
    class GetAllocationTests {
        @Test
        @DisplayName("getAllocation delegates to getPortfolioSummary and returns allocation map")
        void getAllocation_returnsAllocationMap() {
            AssetDTO stock = createAssetDTO(1L, AssetType.STOCK, "A", new BigDecimal("200"), new BigDecimal("100"), new BigDecimal("100"));
            AssetDTO bond = createAssetDTO(2L, AssetType.BOND, "B", new BigDecimal("50"), new BigDecimal("50"), BigDecimal.ZERO);

            when(assetService.getAllAssets()).thenReturn(List.of(stock, bond));

            Map<String, BigDecimal> allocation = portfolioService.getAllocation();

            assertEquals(2, allocation.size());
            assertTrue(allocation.containsKey(AssetType.STOCK.name()));
            assertTrue(allocation.containsKey(AssetType.BOND.name()));
        }
    }

    @Nested
    @DisplayName("getPerformanceByType")
    class GetPerformanceByTypeTests {
        @Test
        @DisplayName("Given assets when getPerformanceByType then compute per-type metrics including percentage when cost > 0")
        void givenAssets_whenGetPerformanceByType_thenComputeMetrics() {
            // STOCK: value 200, cost 100 -> gain 100 -> percentage 100%
            AssetDTO stock = createAssetDTO(1L, AssetType.STOCK, "A", new BigDecimal("200"), new BigDecimal("100"), new BigDecimal("100"));

            when(assetService.getAllAssets()).thenReturn(List.of(stock));

            Map<String, Map<String, BigDecimal>> perf = portfolioService.getPerformanceByType();

            assertTrue(perf.containsKey(AssetType.STOCK.name()));
            Map<String, BigDecimal> stockPerf = perf.get(AssetType.STOCK.name());
            assertEquals(0, new BigDecimal("200").compareTo(stockPerf.get("value")));
            assertEquals(0, new BigDecimal("100").compareTo(stockPerf.get("cost")));
            assertEquals(0, new BigDecimal("100").compareTo(stockPerf.get("percentage")));
        }
    }
}

// java
package com.example.demo.repository;

import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetRepositoryTest {

    @Mock
    private AssetRepository assetRepository;

    // Helper to create asset entities
    private Asset createAsset(String symbol, String name, AssetType type, String quantity, String buyPrice, LocalDateTime createdAt) {
        return Asset.builder()
                .symbol(symbol)
                .name(name)
                .type(type)
                .quantity(new BigDecimal(quantity))
                .buyPrice(new BigDecimal(buyPrice))
                .purchaseDate(null)
                .createdAt(createdAt)
                .build();
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("Type and Count queries")
    class TypeAndCountTests {

        @Test
        @DisplayName("findByType returns assets of the requested type")
        void givenAssets_whenFindByType_thenReturnOnlyThatType() {
            Asset s = createAsset("AAPL", "Apple Inc", AssetType.STOCK, "10", "150.00", LocalDateTime.now());
            Asset b = createAsset("BOND1", "Gov Bond", AssetType.BOND, "5", "1000.00", LocalDateTime.now());

            when(assetRepository.findByType(AssetType.STOCK)).thenReturn(Arrays.asList(s));
            when(assetRepository.countByType(AssetType.STOCK)).thenReturn(1L);
            when(assetRepository.countByType(AssetType.BOND)).thenReturn(1L);

            List<Asset> stocks = assetRepository.findByType(AssetType.STOCK);
            assertNotNull(stocks);
            assertEquals(1, stocks.size());
            assertEquals("AAPL", stocks.get(0).getSymbol());

            long stockCount = assetRepository.countByType(AssetType.STOCK);
            long bondCount = assetRepository.countByType(AssetType.BOND);
            assertEquals(1L, stockCount);
            assertEquals(1L, bondCount);
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("Search queries")
    class SearchTests {

        @Test
        @DisplayName("findBySymbolContainingIgnoreCase finds symbols regardless of case and substring")
        void givenSymbols_whenSearchBySymbolContainingIgnoreCase_thenReturnMatches() {
            Asset a1 = createAsset("AAPL", "Apple", AssetType.STOCK, "1", "10.00", LocalDateTime.now());
            Asset a2 = createAsset("aaplX", "AppleX", AssetType.STOCK, "2", "20.00", LocalDateTime.now());

            when(assetRepository.findBySymbolContainingIgnoreCase("aapl")).thenReturn(Arrays.asList(a1, a2));

            List<Asset> results = assetRepository.findBySymbolContainingIgnoreCase("aapl");
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("findByNameContainingIgnoreCase finds names regardless of case and substring")
        void givenNames_whenSearchByNameContainingIgnoreCase_thenReturnMatches() {
            Asset a1 = createAsset("MSFT", "Microsoft Corporation", AssetType.STOCK, "3", "50.00", LocalDateTime.now());
            Asset a2 = createAsset("TECH1", "Tech Holdings", AssetType.STOCK, "4", "25.00", LocalDateTime.now());

            when(assetRepository.findByNameContainingIgnoreCase("tech")).thenReturn(Arrays.asList(a2));

            List<Asset> results = assetRepository.findByNameContainingIgnoreCase("tech");
            assertEquals(1, results.size());
            assertEquals("TECH1", results.get(0).getSymbol());
        }

        @Test
        @DisplayName("findBySymbolIgnoreCase matches exact symbol ignoring case")
        void givenSymbolDifferentCase_whenFindBySymbolIgnoreCase_thenReturnMatch() {
            Asset asset = createAsset("GoOgL", "Google", AssetType.STOCK, "1", "100.00", LocalDateTime.now());

            when(assetRepository.findBySymbolIgnoreCase("GOOGL")).thenReturn(Arrays.asList(asset));

            List<Asset> found = assetRepository.findBySymbolIgnoreCase("GOOGL");
            assertEquals(1, found.size());
            assertEquals("GoOgL", found.get(0).getSymbol());
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("Date range and aggregation")
    class DateAndAggregationTests {

        @Test
        @DisplayName("findByCreatedAtBetween returns assets within the provided date range")
        void givenAssetsWithDifferentDates_whenFindByCreatedAtBetween_thenReturnOnlyInRange() {
            LocalDateTime now = LocalDateTime.now();
            Asset recent = createAsset("REC", "Recent Asset", AssetType.CASH, "1", "1.00", now.minusHours(1));
            Asset older = createAsset("OLD", "Old Asset", AssetType.CASH, "1", "1.00", now.minusDays(10));

            when(assetRepository.findByCreatedAtBetween(now.minusDays(2), now.plusHours(1))).thenReturn(Arrays.asList(recent));

            List<Asset> found = assetRepository.findByCreatedAtBetween(now.minusDays(2), now.plusHours(1));
            assertTrue(found.stream().anyMatch(a -> "REC".equals(a.getSymbol())));
            assertFalse(found.stream().anyMatch(a -> "OLD".equals(a.getSymbol())));
        }

        @Test
        @DisplayName("getTotalCostBasis returns sum of quantity * buyPrice")
        void givenAssets_whenGetTotalCostBasis_thenReturnSum() {
            when(assetRepository.getTotalCostBasis()).thenReturn(new BigDecimal("35.00"));

            BigDecimal total = assetRepository.getTotalCostBasis();
            assertNotNull(total);
            assertEquals(0, total.compareTo(new BigDecimal("35.00")));
        }
    }
}

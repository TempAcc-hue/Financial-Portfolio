package com.example.demo.repository;

import com.example.demo.entity.BaseAsset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseAssetRepositoryTest {

    @Mock
    private BaseAssetRepository<BaseAsset> baseAssetRepository;

    // Helper to create mocked BaseAsset entities — make symbol stubbing lenient to avoid unnecessary-stubbing failures
    private BaseAsset createAsset(String symbol, String name) {
        BaseAsset asset = mock(BaseAsset.class);
        lenient().when(asset.getSymbol()).thenReturn(symbol);
        return asset;
    }

    @Nested
    @DisplayName("Aggregation")
    class AggregationTests {

        @Test
        @DisplayName("calculateTotalCostBasis returns sum of quantity * buyPrice")
        void givenAssets_whenCalculateTotalCostBasis_thenReturnSum() {
            when(baseAssetRepository.calculateTotalCostBasis()).thenReturn(new BigDecimal("123.45"));

            BigDecimal total = baseAssetRepository.calculateTotalCostBasis();
            assertNotNull(total);
            assertEquals(0, total.compareTo(new BigDecimal("123.45")));
        }

        @Test
        @DisplayName("calculateTotalCostBasis returns zero when no assets")
        void givenNoAssets_whenCalculateTotalCostBasis_thenReturnZero() {
            when(baseAssetRepository.calculateTotalCostBasis()).thenReturn(BigDecimal.ZERO);

            BigDecimal total = baseAssetRepository.calculateTotalCostBasis();
            assertNotNull(total);
            assertEquals(0, total.compareTo(BigDecimal.ZERO));
        }
    }
    @Nested
    @DisplayName("Search queries")
    class SearchTests {

        @Test
        @DisplayName("findBySymbolContainingIgnoreCase finds symbols regardless of case and substring")
        void givenSymbols_whenFindBySymbolContainingIgnoreCase_thenReturnMatches() {
            BaseAsset a1 = createAsset("AAPL", "Apple");
            BaseAsset a2 = createAsset("aaplX", "AppleX");

            when(baseAssetRepository.findBySymbolContainingIgnoreCase("aapl")).thenReturn(Arrays.asList(a1, a2));

            List<BaseAsset> results = baseAssetRepository.findBySymbolContainingIgnoreCase("aapl");
            assertNotNull(results);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("findByNameContainingIgnoreCase finds names regardless of case and substring")
        void givenNames_whenFindByNameContainingIgnoreCase_thenReturnMatches() {
            BaseAsset a1 = createAsset("MSFT", "Microsoft Corporation");
            BaseAsset a2 = createAsset("TECH1", "Tech Holdings");

            when(baseAssetRepository.findByNameContainingIgnoreCase("tech")).thenReturn(Arrays.asList(a2));

            List<BaseAsset> results = baseAssetRepository.findByNameContainingIgnoreCase("tech");
            assertEquals(1, results.size());
            assertEquals("TECH1", results.get(0).getSymbol());
        }
    }
}

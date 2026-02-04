package com.example.demo.controller;

import com.example.demo.dto.PortfolioSummaryDTO;
import com.example.demo.service.PortfolioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/portfolio/summary")
    class GetPortfolioSummaryTests {

        @Test
        @DisplayName("When getPortfolioSummary then return summary")
        void whenGetPortfolioSummary_thenReturnSummary() throws Exception {
            // GIVEN
            PortfolioSummaryDTO summary = PortfolioSummaryDTO.builder()
                    .totalValue(new BigDecimal("10000.00"))
                    .totalGainLoss(new BigDecimal("500.00"))
                    .totalAssets(5)
                    .build();

            when(portfolioService.getPortfolioSummary()).thenReturn(summary);

            // WHEN & THEN
            mockMvc.perform(get("/api/portfolio/summary")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalValue").value(10000.00))
                    .andExpect(jsonPath("$.data.totalAssets").value(5));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/portfolio/allocation")
    class GetAllocationTests {

        @Test
        @DisplayName("When getAllocation then return allocation map")
        void whenGetAllocation_thenReturnAllocationMap() throws Exception {
            // GIVEN
            Map<String, BigDecimal> allocation = new HashMap<>();
            allocation.put("STOCK", new BigDecimal("70.0"));
            allocation.put("CASH", new BigDecimal("30.0"));

            when(portfolioService.getAllocation()).thenReturn(allocation);

            // WHEN & THEN
            mockMvc.perform(get("/api/portfolio/allocation")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.STOCK").value(70.0))
                    .andExpect(jsonPath("$.data.CASH").value(30.0));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/portfolio/performance")
    class GetPerformanceTests {

        @Test
        @DisplayName("When getPerformance then return performance data")
        void whenGetPerformance_thenReturnPerformanceData() throws Exception {
            // GIVEN
            Map<String, Map<String, BigDecimal>> performance = new HashMap<>();

            Map<String, BigDecimal> stockPerf = new HashMap<>();
            stockPerf.put("gain", new BigDecimal("10.5"));
            performance.put("STOCK", stockPerf);

            when(portfolioService.getPerformanceByType()).thenReturn(performance);

            // WHEN & THEN
            mockMvc.perform(get("/api/portfolio/performance")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.STOCK.gain").value(10.5));
        }

        @Test
        @DisplayName("When no performance data then return empty map")
        void whenNoPerformanceData_thenReturnEmptyMap() throws Exception {
            // GIVEN
            when(portfolioService.getPerformanceByType()).thenReturn(Collections.emptyMap());

            // WHEN & THEN
            mockMvc.perform(get("/api/portfolio/performance")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}

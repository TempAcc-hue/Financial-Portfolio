package com.example.demo.controller;

import com.example.demo.dto.CandleStickData;
import com.example.demo.dto.MLPredictionResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.StockPredictionService;
import com.example.demo.service.YahooFinanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@Import(GlobalExceptionHandler.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private YahooFinanceService yahooFinanceService;

    @MockBean
    private StockPredictionService stockPredictionService;

    // Helper method to create a dummy CandleStickData
    private CandleStickData createCandleStickData(long time, double open, double high, double low, double close, long volume) {
        return new CandleStickData(time, open, high, low, close, volume);
    }

    // Helper method to create a dummy MLPredictionResponse
    private MLPredictionResponse createMLPredictionResponse(double predictedPrice, double rawScaledOutput) {
        MLPredictionResponse response = new MLPredictionResponse();
        response.setPredictedPrice(predictedPrice);
        response.setRawScaledOutput(rawScaledOutput);
        return response;
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/stocks/{ticker}/history")
    class GetStockHistoryTests {

        @Test
        @DisplayName("Given valid ticker when getStockHistory then return history data")
        void givenValidTicker_whenGetStockHistory_thenReturnHistoryData() throws Exception {
            // GIVEN
            String ticker = "AAPL";
            CandleStickData data1 = createCandleStickData(1704067200L, 185.50, 187.20, 184.80, 186.90, 50000000L);
            CandleStickData data2 = createCandleStickData(1704153600L, 186.90, 188.50, 186.20, 188.00, 52000000L);
            CandleStickData data3 = createCandleStickData(1704240000L, 188.00, 189.75, 187.50, 189.25, 48000000L);
            List<CandleStickData> mockHistory = Arrays.asList(data1, data2, data3);

            when(yahooFinanceService.getStockHistory(eq(ticker))).thenReturn(mockHistory);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/history", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].time").value(1704067200L))
                    .andExpect(jsonPath("$[0].open").value(185.50))
                    .andExpect(jsonPath("$[0].high").value(187.20))
                    .andExpect(jsonPath("$[0].low").value(184.80))
                    .andExpect(jsonPath("$[0].close").value(186.90))
                    .andExpect(jsonPath("$[0].volume").value(50000000L))
                    .andExpect(jsonPath("$[1].time").value(1704153600L))
                    .andExpect(jsonPath("$[2].time").value(1704240000L));
        }

        @Test
        @DisplayName("Given ticker with no history when getStockHistory then return empty list")
        void givenTickerWithNoHistory_whenGetStockHistory_thenReturnEmptyList() throws Exception {
            // GIVEN
            String ticker = "UNKNOWN";
            List<CandleStickData> emptyHistory = Collections.emptyList();

            when(yahooFinanceService.getStockHistory(eq(ticker))).thenReturn(emptyHistory);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/history", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Given popular stock ticker when getStockHistory then return comprehensive history")
        void givenPopularStockTicker_whenGetStockHistory_thenReturnComprehensiveHistory() throws Exception {
            // GIVEN
            String ticker = "TSLA";
            List<CandleStickData> mockHistory = Arrays.asList(
                    createCandleStickData(1704067200L, 250.00, 255.00, 248.00, 252.50, 100000000L),
                    createCandleStickData(1704153600L, 252.50, 258.00, 251.00, 256.75, 105000000L),
                    createCandleStickData(1704240000L, 256.75, 260.00, 254.50, 258.90, 98000000L),
                    createCandleStickData(1704326400L, 258.90, 262.50, 257.00, 261.25, 102000000L),
                    createCandleStickData(1704412800L, 261.25, 265.00, 259.50, 263.80, 110000000L)
            );

            when(yahooFinanceService.getStockHistory(eq(ticker))).thenReturn(mockHistory);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/history", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(5))
                    .andExpect(jsonPath("$[0].close").value(252.50))
                    .andExpect(jsonPath("$[4].close").value(263.80));
        }

        @Test
        @DisplayName("Given service returns null when getStockHistory then return empty response")
        void givenServiceReturnsNull_whenGetStockHistory_thenReturnEmptyResponse() throws Exception {
            // GIVEN
            String ticker = "NULL";

            when(yahooFinanceService.getStockHistory(eq(ticker))).thenReturn(null);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/history", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/stocks/{ticker}/predict")
    class GetStockPredictionTests {

        @Test
        @DisplayName("Given valid ticker when getStockPrediction then return prediction")
        void givenValidTicker_whenGetStockPrediction_thenReturnPrediction() throws Exception {
            // GIVEN
            String ticker = "AAPL";
            MLPredictionResponse mockPrediction = createMLPredictionResponse(195.50, 0.7823);

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(mockPrediction);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.predicted_price").value(195.50))
                    .andExpect(jsonPath("$.raw_scaled_output").value(0.7823));
        }

        @Test
        @DisplayName("Given different ticker when getStockPrediction then return specific prediction")
        void givenDifferentTicker_whenGetStockPrediction_thenReturnSpecificPrediction() throws Exception {
            // GIVEN
            String ticker = "GOOGL";
            MLPredictionResponse mockPrediction = createMLPredictionResponse(142.75, 0.6543);

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(mockPrediction);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.predicted_price").value(142.75))
                    .andExpect(jsonPath("$.raw_scaled_output").value(0.6543));
        }

        @Test
        @DisplayName("Given high value stock when getStockPrediction then return prediction with high price")
        void givenHighValueStock_whenGetStockPrediction_thenReturnPredictionWithHighPrice() throws Exception {
            // GIVEN
            String ticker = "NVDA";
            MLPredictionResponse mockPrediction = createMLPredictionResponse(875.25, 0.9124);

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(mockPrediction);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.predicted_price").value(875.25))
                    .andExpect(jsonPath("$.raw_scaled_output").value(0.9124));
        }

        @Test
        @DisplayName("Given low value stock when getStockPrediction then return prediction with low price")
        void givenLowValueStock_whenGetStockPrediction_thenReturnPredictionWithLowPrice() throws Exception {
            // GIVEN
            String ticker = "F";
            MLPredictionResponse mockPrediction = createMLPredictionResponse(12.35, 0.3421);

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(mockPrediction);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.predicted_price").value(12.35))
                    .andExpect(jsonPath("$.raw_scaled_output").value(0.3421));
        }

        @Test
        @DisplayName("Given service returns null when getStockPrediction then return null response")
        void givenServiceReturnsNull_whenGetStockPrediction_thenReturnNullResponse() throws Exception {
            // GIVEN
            String ticker = "NULL";

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(null);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Given cryptocurrency ticker when getStockPrediction then return prediction")
        void givenCryptocurrencyTicker_whenGetStockPrediction_thenReturnPrediction() throws Exception {
            // GIVEN
            String ticker = "BTC-USD";
            MLPredictionResponse mockPrediction = createMLPredictionResponse(48250.75, 0.8765);

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(mockPrediction);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.predicted_price").value(48250.75))
                    .andExpect(jsonPath("$.raw_scaled_output").value(0.8765));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Given ticker with special characters when getStockHistory then handle correctly")
        void givenTickerWithSpecialCharacters_whenGetStockHistory_thenHandleCorrectly() throws Exception {
            // GIVEN
            String ticker = "BRK.B";
            List<CandleStickData> mockHistory = Collections.singletonList(
                    createCandleStickData(1704067200L, 350.00, 355.00, 348.00, 352.50, 1000000L)
            );

            when(yahooFinanceService.getStockHistory(eq(ticker))).thenReturn(mockHistory);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/history", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].close").value(352.50));
        }

        @Test
        @DisplayName("Given lowercase ticker when getStockHistory then handle correctly")
        void givenLowercaseTicker_whenGetStockHistory_thenHandleCorrectly() throws Exception {
            // GIVEN
            String ticker = "aapl";
            List<CandleStickData> mockHistory = Collections.singletonList(
                    createCandleStickData(1704067200L, 185.50, 187.20, 184.80, 186.90, 50000000L)
            );

            when(yahooFinanceService.getStockHistory(eq(ticker))).thenReturn(mockHistory);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/history", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Given index ticker when getStockPrediction then return prediction")
        void givenIndexTicker_whenGetStockPrediction_thenReturnPrediction() throws Exception {
            // GIVEN
            String ticker = "^GSPC";
            MLPredictionResponse mockPrediction = createMLPredictionResponse(4850.25, 0.7891);

            when(stockPredictionService.getPrediction(eq(ticker))).thenReturn(mockPrediction);

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/stocks/{ticker}/predict", ticker)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.predicted_price").value(4850.25));
        }
    }
}

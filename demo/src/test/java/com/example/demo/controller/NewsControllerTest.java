package com.example.demo.controller;

import com.example.demo.dto.StockNews;
import com.example.demo.service.StockPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockPriceService stockPriceService;

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/news/market")
    class GetMarketNewsTests {

        @Test
        @DisplayName("Given no category (default) when getMarketNews then return general news")
        void givenNoCategory_whenGetMarketNews_thenReturnGeneralNews() throws Exception {
            // GIVEN
            StockNews newsItem = new StockNews(); // Assuming empty constructor or setter usage
            // Set properties if StockNews has setters/builder, e.g., newsItem.setTitle("Market Rally");
            List<StockNews> mockNews = Collections.singletonList(newsItem);

            when(stockPriceService.getMarketNews("general")).thenReturn(mockNews);

            // WHEN & THEN
            mockMvc.perform(get("/api/news/market")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("Given specific category when getMarketNews then return news for category")
        void givenSpecificCategory_whenGetMarketNews_thenReturnNewsForCategory() throws Exception {
            // GIVEN
            String category = "crypto";
            StockNews newsItem1 = new StockNews();
            StockNews newsItem2 = new StockNews();
            List<StockNews> mockNews = Arrays.asList(newsItem1, newsItem2);

            when(stockPriceService.getMarketNews(category)).thenReturn(mockNews);

            // WHEN & THEN
            mockMvc.perform(get("/api/news/market")
                            .param("category", category)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/news/company/{symbol}")
    class GetCompanyNewsTests {

        @Test
        @DisplayName("Given valid symbol and dates when getCompanyNews then return news")
        void givenValidSymbolAndDates_whenGetCompanyNews_thenReturnNews() throws Exception {
            // GIVEN
            String symbol = "AAPL";
            LocalDate fromDate = LocalDate.of(2023, 1, 1);
            LocalDate toDate = LocalDate.of(2023, 1, 31);

            StockNews newsItem = new StockNews();
            List<StockNews> mockNews = Collections.singletonList(newsItem);

            when(stockPriceService.isValidSymbol(symbol)).thenReturn(true);
            when(stockPriceService.getCompanyNews(eq(symbol), eq(fromDate), eq(toDate))).thenReturn(mockNews);

            // WHEN & THEN
            mockMvc.perform(get("/api/news/company/{symbol}", symbol)
                            .param("from", fromDate.toString())
                            .param("to", toDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("Given valid symbol without dates when getCompanyNews then return news with defaults")
        void givenValidSymbolNoDates_whenGetCompanyNews_thenReturnNews() throws Exception {
            // GIVEN
            String symbol = "TSLA";
            List<StockNews> mockNews = Collections.emptyList();

            when(stockPriceService.isValidSymbol(symbol)).thenReturn(true);
            // Controller passes null if params are missing
            when(stockPriceService.getCompanyNews(eq(symbol), any(), any())).thenReturn(mockNews);

            // WHEN & THEN
            mockMvc.perform(get("/api/news/company/{symbol}", symbol)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Given invalid symbol when getCompanyNews then return 400 Bad Request")
        void givenInvalidSymbol_whenGetCompanyNews_thenReturnBadRequest() throws Exception {
            // GIVEN
            String invalidSymbol = "INVALID";

            when(stockPriceService.isValidSymbol(invalidSymbol)).thenReturn(false);

            // WHEN & THEN
            mockMvc.perform(get("/api/news/company/{symbol}", invalidSymbol)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid or empty stock symbol provided"));
        }
    }
}
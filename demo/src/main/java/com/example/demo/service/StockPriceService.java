package com.example.demo.service;

import java.math.BigDecimal;
import com.example.demo.dto.StockNews; // Import the new DTO
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
/**
 * Service interface for fetching stock prices from external APIs.
 */
public interface StockPriceService {

    /**
     * Get the current price for a stock symbol.
     * 
     * @param symbol Stock ticker symbol (e.g., "AAPL")
     * @return Current price, or null if not available
     */
    BigDecimal getCurrentPrice(String symbol);

    /**
     * Get current prices for multiple symbols.
     * 
     * @param symbols Array of stock ticker symbols
     * @return Map of symbol to price
     */
    Map<String, BigDecimal> getCurrentPrices(String... symbols);

    /**
     * Check if a symbol is valid and tradeable.
     * 
     * @param symbol Stock ticker symbol
     * @return true if the symbol is valid
     */
    boolean isValidSymbol(String symbol);

    List<StockNews> getMarketNews(String category);
    List<StockNews> getCompanyNews(String symbol, LocalDate from, LocalDate to);
}

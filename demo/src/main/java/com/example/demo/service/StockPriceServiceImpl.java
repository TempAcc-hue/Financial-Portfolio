package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of StockPriceService using Yahoo Finance API.
 * Includes caching to reduce API calls.
 */
@Service
@Slf4j
public class StockPriceServiceImpl implements StockPriceService {

    // Simple cache with 5-minute expiry
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }

        String upperSymbol = symbol.toUpperCase().trim();

        // Check cache first
        CachedPrice cached = priceCache.get(upperSymbol);
        if (cached != null && !cached.isExpired()) {
            log.debug("Returning cached price for {}: {}", upperSymbol, cached.price);
            return cached.price;
        }

        try {
            Stock stock = YahooFinance.get(upperSymbol);
            if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                BigDecimal price = stock.getQuote().getPrice();
                priceCache.put(upperSymbol, new CachedPrice(price));
                log.info("Fetched price for {}: {}", upperSymbol, price);
                return price;
            }
        } catch (IOException e) {
            log.warn("Failed to fetch price for {}: {}", upperSymbol, e.getMessage());
        }

        return null;
    }

    @Override
    public Map<String, BigDecimal> getCurrentPrices(String... symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();

        if (symbols == null || symbols.length == 0) {
            return prices;
        }

        try {
            Map<String, Stock> stocks = YahooFinance.get(symbols);
            for (Map.Entry<String, Stock> entry : stocks.entrySet()) {
                Stock stock = entry.getValue();
                if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                    BigDecimal price = stock.getQuote().getPrice();
                    prices.put(entry.getKey(), price);
                    priceCache.put(entry.getKey(), new CachedPrice(price));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to fetch prices for batch request: {}", e.getMessage());
            // Fall back to individual requests
            for (String symbol : symbols) {
                BigDecimal price = getCurrentPrice(symbol);
                if (price != null) {
                    prices.put(symbol.toUpperCase(), price);
                }
            }
        }

        return prices;
    }

    @Override
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return false;
        }

        try {
            Stock stock = YahooFinance.get(symbol.toUpperCase().trim());
            return stock != null && stock.getName() != null;
        } catch (IOException e) {
            log.warn("Failed to validate symbol {}: {}", symbol, e.getMessage());
            return false;
        }
    }

    /**
     * Simple cache entry with expiration.
     */
    private static class CachedPrice {
        final BigDecimal price;
        final long timestamp;

        CachedPrice(BigDecimal price) {
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}

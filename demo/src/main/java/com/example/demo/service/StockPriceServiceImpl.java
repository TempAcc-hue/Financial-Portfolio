package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of StockPriceService using Finnhub API.
 * Provides real-time stock prices with caching to reduce API calls.
 * 
 * Free tier: 60 API calls/minute
 * API Docs: https://finnhub.io/docs/api
 */
@Service
@Slf4j
public class StockPriceServiceImpl implements StockPriceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.base-url}")
    private String baseUrl;

    // Simple cache with 5-minute expiry to reduce API calls
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    public StockPriceServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            log.debug("Symbol is null or blank, returning null");
            return null;
        }

        String upperSymbol = normalizeSymbol(symbol);

        // Check cache first
        CachedPrice cached = priceCache.get(upperSymbol);
        if (cached != null && !cached.isExpired()) {
            log.debug("Returning cached price for {}: {}", upperSymbol, cached.price);
            return cached.price;
        }

        try {
            // Build the API URL
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/quote")
                    .queryParam("symbol", upperSymbol)
                    .queryParam("token", apiKey)
                    .toUriString();

            log.debug("Fetching price from Finnhub for symbol: {}", upperSymbol);

            // Make the API call
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);

                // "c" is the current price in Finnhub's response
                JsonNode currentPriceNode = jsonNode.get("c");

                if (currentPriceNode != null && !currentPriceNode.isNull()) {
                    double priceValue = currentPriceNode.asDouble();

                    // Finnhub returns 0 for invalid symbols
                    if (priceValue > 0) {
                        BigDecimal price = BigDecimal.valueOf(priceValue);
                        priceCache.put(upperSymbol, new CachedPrice(price));
                        log.info("Fetched price for {}: ${}", upperSymbol, price);
                        return price;
                    } else {
                        log.warn("Finnhub returned 0 for symbol {} - symbol may be invalid", upperSymbol);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch price for {}: {}", upperSymbol, e.getMessage());
        }

        return null;
    }

    @Override
    public Map<String, BigDecimal> getCurrentPrices(String... symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();

        if (symbols == null || symbols.length == 0) {
            return prices;
        }

        // Finnhub doesn't have a batch endpoint on free tier, so we call individually
        for (String symbol : symbols) {
            BigDecimal price = getCurrentPrice(symbol);
            if (price != null) {
                prices.put(normalizeSymbol(symbol), price);
            }
        }

        return prices;
    }

    @Override
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return false;
        }

        // Try to get the price - if we get a valid price, the symbol is valid
        BigDecimal price = getCurrentPrice(symbol);
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Normalize symbol for API calls.
     * Handles crypto symbols and other special cases.
     */
    private String normalizeSymbol(String symbol) {
        String normalized = symbol.toUpperCase().trim();

        // Handle common crypto symbol formats
        // BTC-USD -> Use Coinbase format for Finnhub: COINBASE:BTC-USD doesn't work
        // For crypto, Finnhub needs exchange prefix, but for simplicity we'll
        // try the standard format first

        return normalized;
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

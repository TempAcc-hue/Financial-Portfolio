package com.example.demo.service;

import com.example.demo.dto.CandleStickData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class YahooFinanceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CandleStickData> getStockHistory(String ticker) {
        try {
            // Calculate timestamps for last 1 year
            long period2 = Instant.now().getEpochSecond();
            long period1 = Instant.now().minus(1826, ChronoUnit.DAYS).getEpochSecond();

            // Build URL
            String url = String.format(
                    "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d",
                    ticker, period1, period2
            );

            // Set headers to avoid rate limiting
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from Yahoo Finance");
            }

            // Parse response
            List<CandleStickData> candleSticks = parseYahooResponse(response.getBody());

            System.out.println("Fetched " + candleSticks.size() + " historical quotes");

            return candleSticks;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from Yahoo Finance: " + e.getMessage(), e);
        }
    }

    private List<CandleStickData> parseYahooResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode result = root.path("chart").path("result").get(0);

        if (result == null || result.isMissingNode()) {
            throw new RuntimeException("Invalid response from Yahoo Finance");
        }

        JsonNode timestamps = result.path("timestamp");
        JsonNode indicators = result.path("indicators").path("quote").get(0);

        JsonNode opens = indicators.path("open");
        JsonNode highs = indicators.path("high");
        JsonNode lows = indicators.path("low");
        JsonNode closes = indicators.path("close");
        JsonNode volumes = indicators.path("volume");

        List<CandleStickData> candleSticks = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++) {
            CandleStickData data = new CandleStickData();

            data.setTime(timestamps.get(i).asLong());
            data.setOpen(getDoubleValue(opens, i));
            data.setHigh(getDoubleValue(highs, i));
            data.setLow(getDoubleValue(lows, i));
            data.setClose(getDoubleValue(closes, i));
            data.setVolume(getLongValue(volumes, i));

            candleSticks.add(data);
        }

        return candleSticks;
    }

    private double getDoubleValue(JsonNode array, int index) {
        JsonNode node = array.get(index);
        return (node != null && !node.isNull()) ? node.asDouble() : 0.0;
    }

    private long getLongValue(JsonNode array, int index) {
        JsonNode node = array.get(index);
        return (node != null && !node.isNull()) ? node.asLong() : 0L;
    }
}
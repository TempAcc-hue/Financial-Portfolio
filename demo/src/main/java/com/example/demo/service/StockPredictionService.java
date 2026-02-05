package com.example.demo.service;

import com.example.demo.dto.MLPredictionRequest;
import com.example.demo.dto.MLPredictionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockPredictionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    // URL of your running Python Service
    private final String ML_SERVICE_URL = "http://localhost:5001/predict";

    // IMPORTANT: Must match the sequence length your model was trained on!
    private final int SEQUENCE_LENGTH = 60;

    public MLPredictionResponse getPrediction(String ticker) {
        try {
            // 1. Fetch History from Yahoo Finance API
            // Calculate timestamps for last 150 days to ensure we have enough data
            long period2 = Instant.now().getEpochSecond();
            long period1 = Instant.now().minus(150, ChronoUnit.DAYS).getEpochSecond();

            // Build URL
            String url = String.format(
                    "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d",
                    ticker, period1, period2);

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
                    String.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from Yahoo Finance");
            }

            // 2. Parse and format data for ML model
            List<List<Double>> formattedData = parseYahooResponseForML(response.getBody());

            if (formattedData.size() < SEQUENCE_LENGTH) {
                throw new RuntimeException("Not enough historical data to make a prediction. Got "
                        + formattedData.size() + " days, need " + SEQUENCE_LENGTH);
            }

            // Get the LAST 'SEQUENCE_LENGTH' candles (e.g., the most recent 60 days)
            List<List<Double>> recentData = formattedData.subList(formattedData.size() - SEQUENCE_LENGTH,
                    formattedData.size());

            // 3. Call Python Microservice
            MLPredictionRequest request = new MLPredictionRequest(recentData);
            MLPredictionResponse mlResponse = restTemplate.postForObject(ML_SERVICE_URL, request,
                    MLPredictionResponse.class);

            // 4. Scale the predicted price by 1.5
            if (mlResponse != null && mlResponse.getPredictedPrice() != null) {
                mlResponse.setPredictedPrice(mlResponse.getPredictedPrice() * 1.35);
            }

            return mlResponse;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error predicting stock price: " + e.getMessage());
        }
    }

    private List<List<Double>> parseYahooResponseForML(String jsonResponse) throws Exception {
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

        List<List<Double>> formattedData = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++) {
            List<Double> row = new ArrayList<>();
            // Format: [Open, High, Low, Close, Volume]
            row.add(getDoubleValue(opens, i));
            row.add(getDoubleValue(highs, i));
            row.add(getDoubleValue(lows, i));
            row.add(getDoubleValue(closes, i));
            row.add(getDoubleValue(volumes, i));

            formattedData.add(row);
        }

        return formattedData;
    }

    private double getDoubleValue(JsonNode array, int index) {
        JsonNode node = array.get(index);
        return (node != null && !node.isNull()) ? node.asDouble() : 0.0;
    }
}
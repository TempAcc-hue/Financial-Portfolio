package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.StockNews;
import com.example.demo.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {

    private final StockPriceService stockPriceService;

    /**
     * Get general market news.
     * Endpoint: GET /api/news/market?category=general
     * * @param category Optional category (general, forex, crypto, merger). Defaults to 'general' in service.
     */
    @GetMapping("/market")
    public ResponseEntity<ApiResponse<List<StockNews>>> getMarketNews(
            @RequestParam(required = false, defaultValue = "general") String category) {

        log.info("Request received for market news. Category: {}", category);
        List<StockNews> news = stockPriceService.getMarketNews(category);

        return ResponseEntity.ok(ApiResponse.success(news));
    }

    /**
     * Get company-specific news.
     * Endpoint: GET /api/news/company/AAPL?from=2023-01-01&to=2023-01-31
     * * @param symbol The stock symbol (e.g., AAPL, TSLA)
     * @param from   Optional start date (YYYY-MM-DD). Defaults to 30 days ago in service.
     * @param to     Optional end date (YYYY-MM-DD). Defaults to today in service.
     */
    @GetMapping("/company/{symbol}")
    public ResponseEntity<ApiResponse<List<StockNews>>> getCompanyNews(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        log.info("Request received for company news. Symbol: {}, From: {}, To: {}", symbol, from, to);

        if (!stockPriceService.isValidSymbol(symbol)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or empty stock symbol provided"));
        }

        List<StockNews> news = stockPriceService.getCompanyNews(symbol, from, to);
        return ResponseEntity.ok(ApiResponse.success(news));
    }
}
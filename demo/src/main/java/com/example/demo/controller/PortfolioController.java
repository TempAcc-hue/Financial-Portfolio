package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PortfolioSummaryDTO;
import com.example.demo.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST Controller for portfolio-level analytics and summary.
 */
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio analytics and summary endpoints")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/summary")
    @Operation(summary = "Get portfolio summary", description = "Retrieves comprehensive portfolio summary with totals, allocation, and top performers")
    public ResponseEntity<ApiResponse<PortfolioSummaryDTO>> getPortfolioSummary() {
        PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary();
        return ResponseEntity.ok(ApiResponse.success("Portfolio summary retrieved", summary));
    }

    @GetMapping("/allocation")
    @Operation(summary = "Get asset allocation", description = "Retrieves asset allocation percentages by type (for pie charts)")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getAllocation() {
        Map<String, BigDecimal> allocation = portfolioService.getAllocation();
        return ResponseEntity.ok(ApiResponse.success("Allocation data retrieved", allocation));
    }

    @GetMapping("/performance")
    @Operation(summary = "Get performance by type", description = "Retrieves performance metrics grouped by asset type")
    public ResponseEntity<ApiResponse<Map<String, Map<String, BigDecimal>>>> getPerformance() {
        Map<String, Map<String, BigDecimal>> performance = portfolioService.getPerformanceByType();
        return ResponseEntity.ok(ApiResponse.success("Performance data retrieved", performance));
    }
}

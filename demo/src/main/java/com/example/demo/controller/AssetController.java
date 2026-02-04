package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AssetDTO;
import com.example.demo.entity.AssetType;
import com.example.demo.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for Asset CRUD operations.
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Asset management endpoints")
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @Operation(summary = "Get all assets", description = "Retrieves all assets in the portfolio with current prices")
    public ResponseEntity<ApiResponse<List<AssetDTO>>> getAllAssets() {
        List<AssetDTO> assets = assetService.getAllAssets();
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + assets.size() + " assets", assets));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID", description = "Retrieves a specific asset by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Asset not found")
    })
    public ResponseEntity<ApiResponse<AssetDTO>> getAssetById(
            @Parameter(description = "Asset ID") @PathVariable Long id) {
        AssetDTO asset = assetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.success(asset));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get assets by type", description = "Retrieves all assets of a specific type")
    public ResponseEntity<ApiResponse<List<AssetDTO>>> getAssetsByType(
            @Parameter(description = "Asset type (STOCK, BOND, CASH, REAL_ESTATE, CRYPTO, ETF, MUTUAL_FUND)") @PathVariable AssetType type) {
        List<AssetDTO> assets = assetService.getAssetsByType(type);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + assets.size() + " " + type + " assets", assets));
    }

    @GetMapping("/search")
    @Operation(summary = "Search assets", description = "Search assets by symbol or name")
    public ResponseEntity<ApiResponse<List<AssetDTO>>> searchAssets(
            @Parameter(description = "Search query") @RequestParam String q) {
        List<AssetDTO> assets = assetService.searchAssets(q);
        return ResponseEntity.ok(ApiResponse.success("Found " + assets.size() + " matching assets", assets));
    }

    @PostMapping
    @Operation(summary = "Create asset", description = "Adds a new asset to the portfolio")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Asset created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<AssetDTO>> createAsset(
            @Valid @RequestBody AssetDTO assetDTO) {
        AssetDTO created = assetService.createAsset(assetDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset", description = "Updates an existing asset")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Asset not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<AssetDTO>> updateAsset(
            @Parameter(description = "Asset ID") @PathVariable Long id,
            @Valid @RequestBody AssetDTO assetDTO) {
        AssetDTO updated = assetService.updateAsset(id, assetDTO);
        return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete asset", description = "Removes an asset from the portfolio")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Asset not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @Parameter(description = "Asset ID") @PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", null));
    }

    /**
     * Upload CSV file containing assets and import them.
     * Expected columns (header required): symbol,name,type,quantity,buyPrice,purchaseDate
     * - type should match AssetType enum values (STOCK, BOND, ...)
     * - quantity and buyPrice are numeric
     * - purchaseDate (optional) in ISO format yyyy-MM-dd
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload CSV and import assets")
    public ResponseEntity<ApiResponse<List<AssetDTO>>> uploadAssets(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Empty file"));
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<AssetDTO> created = new ArrayList<>();

        try {
            if (filename.endsWith(".csv") || filename.endsWith(".txt")) {
                // simple CSV parse
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    String header = reader.readLine();
                    if (header == null) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("CSV has no header/rows"));
                    }
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] cols = line.split(",", -1);
                        AssetDTO dto = csvRowToDto(cols);
                        if (dto != null) {
                            created.add(assetService.createAsset(dto));
                        }
                    }
                }
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Unsupported file type. Use CSV (or .txt)"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to parse file: " + ex.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Imported " + created.size() + " assets", created));
    }

    // Minimal CSV row -> AssetDTO converter. Returns null for invalid rows.
    private AssetDTO csvRowToDto(String[] cols) {
        // Expected columns: symbol,name,type,quantity,buyPrice,purchaseDate
        if (cols == null || cols.length < 5) return null;
        try {
            String symbol = cols[0].trim();
            String name = cols[1].trim();
            String typeStr = cols[2].trim().toUpperCase();
            String qtyStr = cols[3].trim();
            String buyStr = cols[4].trim();
            String dateStr = cols.length > 5 ? cols[5].trim() : null;

            if (symbol.isEmpty() || name.isEmpty() || typeStr.isEmpty() || qtyStr.isEmpty() || buyStr.isEmpty()) {
                return null; // skip incomplete
            }

            AssetType type = AssetType.valueOf(typeStr);
            BigDecimal quantity = new BigDecimal(qtyStr);
            BigDecimal buyPrice = new BigDecimal(buyStr);
            LocalDate purchaseDate = null;
            if (dateStr != null && !dateStr.isEmpty()) {
                purchaseDate = LocalDate.parse(dateStr);
            }

            return AssetDTO.builder()
                    .symbol(symbol)
                    .name(name)
                    .type(type)
                    .quantity(quantity)
                    .buyPrice(buyPrice)
                    .purchaseDate(purchaseDate)
                    .build();
        } catch (Exception ex) {
            // invalid row, skip
            return null;
        }
    }

    @GetMapping("topGainersLoosers")
    @Operation(summary = "Get top stock gainers and loosers", description = "his endpoint returns the top 20 gainers, losers, and the most active traded tickers in the US market.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Asset not found")
    })
    public ResponseEntity<ApiResponse<JSONObject>> getTopGainersLoosers() {
        JSONObject asset = assetService.getTopGainersLoosers();
        return ResponseEntity.ok(ApiResponse.success(asset));
    }

}

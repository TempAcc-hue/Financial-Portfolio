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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

package com.example.demo.controller;

import com.example.demo.model.Asset;
import com.example.demo.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Portfolio Management", description = "Operations for managing portfolio assets")
public class AssetController {

    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /////////////////////////////////////////////////////////////

    @PostMapping("/assets")
    @Operation(summary = "Add a new asset", description = "Adds a Stock, Cash, or Real Estate asset to the portfolio")
    public ResponseEntity<Asset> addAsset(@RequestBody Asset asset) {
        Asset createdAsset = assetService.addAsset(asset);
        return new ResponseEntity<>(createdAsset, HttpStatus.CREATED);
    }

    /////////////////////////////////////////

    @GetMapping("/assets")
    @Operation(summary = "List all assets", description = "Retrieves a list of all assets (Stocks, Cash, Real Estate) currently in the portfolio")
    public ResponseEntity<List<Asset>> getAllAssets() {
        // Citation: - Browse a portfolio
        List<Asset> assets = assetService.getAllAssets();

        if (assets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(assets, HttpStatus.OK);
    }

    ///////////////////////////////////////////

    @DeleteMapping("/assets/{id}")
    @Operation(summary = "Delete an asset", description = "Removes an asset from the portfolio by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Asset successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Asset not found")
    })
    public ResponseEntity<Void> deleteAsset(
            @Parameter(description = "ID of the asset to delete") @PathVariable Long id) {

        boolean deleted = assetService.deleteAsset(id);

        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204: Success, no body returned
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404: ID didn't exist
        }
    }

}
package com.example.demo.service;

import com.example.demo.dto.AssetDTO;
import com.example.demo.entity.AssetType;
import org.json.JSONObject;

import java.util.List;

/**
 * Service interface for Asset CRUD operations.
 */
public interface AssetService {

    /**
     * Get all assets in the portfolio.
     */
    List<AssetDTO> getAllAssets();

    /**
     * Get an asset by its ID.
     */
    AssetDTO getAssetById(Long id);

    /**
     * Get assets filtered by type.
     */
    List<AssetDTO> getAssetsByType(AssetType type);

    /**
     * Search assets by symbol or name.
     */
    List<AssetDTO> searchAssets(String query);

    /**
     * Create a new asset.
     */
    AssetDTO createAsset(AssetDTO assetDTO);

    /**
     * Update an existing asset.
     */
    AssetDTO updateAsset(Long id, AssetDTO assetDTO);

    /**
     * Delete an asset by ID.
     */
    void deleteAsset(Long id);

    JSONObject getTopGainersLoosers();
}
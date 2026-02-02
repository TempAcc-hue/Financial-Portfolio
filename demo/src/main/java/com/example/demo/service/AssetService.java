package com.example.demo.service;

import com.example.demo.model.Asset;
import com.example.demo.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    @Autowired
    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public Asset addAsset(Asset asset) {
        // Best Practice: Validation or default setting before saving
        if (asset.getPurchaseDate() == null) {
            asset.setPurchaseDate(LocalDate.now());
        }

        return assetRepository.save(asset);
    }


    public List<Asset> getAllAssets() {
        // Citation: - API allows retrieving records
        return assetRepository.findAll();
    }

    public boolean deleteAsset(Long id) {
        if (assetRepository.existsById(id)) {
            assetRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
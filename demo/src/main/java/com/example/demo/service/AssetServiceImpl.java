package com.example.demo.service;

import com.example.demo.dto.AssetDTO;
import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AssetService with business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final StockPriceService stockPriceService;

    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(this::enrichAssetDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        return enrichAssetDTO(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> getAssetsByType(AssetType type) {
        return assetRepository.findByType(type).stream()
                .map(this::enrichAssetDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> searchAssets(String query) {
        if (query == null || query.isBlank()) {
            return getAllAssets();
        }

        // Search by symbol and name, combine results
        List<Asset> bySymbol = assetRepository.findBySymbolContainingIgnoreCase(query);
        List<Asset> byName = assetRepository.findByNameContainingIgnoreCase(query);

        // Combine and deduplicate
        java.util.Set<Asset> combined = new java.util.LinkedHashSet<>(bySymbol);
        combined.addAll(byName);

        return combined.stream()
                .map(this::enrichAssetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AssetDTO createAsset(AssetDTO assetDTO) {
        Asset asset = Asset.builder()
                .symbol(assetDTO.getSymbol().toUpperCase().trim())
                .name(assetDTO.getName().trim())
                .type(assetDTO.getType())
                .quantity(assetDTO.getQuantity())
                .buyPrice(assetDTO.getBuyPrice())
                .purchaseDate(assetDTO.getPurchaseDate())
                .build();

        Asset saved = assetRepository.save(asset);
        log.info("Created new asset: {} ({})", saved.getName(), saved.getSymbol());
        return enrichAssetDTO(saved);
    }

    @Override
    public AssetDTO updateAsset(Long id, AssetDTO assetDTO) {
        Asset existing = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        existing.setSymbol(assetDTO.getSymbol().toUpperCase().trim());
        existing.setName(assetDTO.getName().trim());
        existing.setType(assetDTO.getType());
        existing.setQuantity(assetDTO.getQuantity());
        existing.setBuyPrice(assetDTO.getBuyPrice());
        existing.setPurchaseDate(assetDTO.getPurchaseDate());

        Asset updated = assetRepository.save(existing);
        log.info("Updated asset: {} (ID: {})", updated.getName(), updated.getId());
        return enrichAssetDTO(updated);
    }

    @Override
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        assetRepository.delete(asset);
        log.info("Deleted asset: {} (ID: {})", asset.getName(), id);
    }

    /**
     * Convert Asset entity to DTO and enrich with current price data.
     */
    private AssetDTO enrichAssetDTO(Asset asset) {
        AssetDTO dto = AssetDTO.builder()
                .id(asset.getId())
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .type(asset.getType())
                .quantity(asset.getQuantity())
                .buyPrice(asset.getBuyPrice())
                .purchaseDate(asset.getPurchaseDate())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .costBasis(asset.getCostBasis())
                .build();

        // For STOCK, ETF, CRYPTO, and MUTUAL_FUND, try to get current price
        if (isTradeableAsset(asset.getType())) {
            BigDecimal currentPrice = stockPriceService.getCurrentPrice(asset.getSymbol());
            if (currentPrice != null) {
                dto.setCurrentPrice(currentPrice);
                dto.setCurrentValue(asset.getQuantity().multiply(currentPrice));

                BigDecimal costBasis = asset.getCostBasis();
                BigDecimal gainLoss = dto.getCurrentValue().subtract(costBasis);
                dto.setGainLoss(gainLoss);

                if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal gainLossPercentage = gainLoss
                            .divide(costBasis, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    dto.setGainLossPercentage(gainLossPercentage);
                }
            } else {
                // Fall back to buy price as current price
                dto.setCurrentPrice(asset.getBuyPrice());
                dto.setCurrentValue(asset.getCostBasis());
                dto.setGainLoss(BigDecimal.ZERO);
                dto.setGainLossPercentage(BigDecimal.ZERO);
            }
        } else {
            // For BOND, CASH, REAL_ESTATE - use buy price as current value
            dto.setCurrentPrice(asset.getBuyPrice());
            dto.setCurrentValue(asset.getCostBasis());
            dto.setGainLoss(BigDecimal.ZERO);
            dto.setGainLossPercentage(BigDecimal.ZERO);
        }

        return dto;
    }

    /**
     * Check if asset type supports live price fetching.
     */
    private boolean isTradeableAsset(AssetType type) {
        return type == AssetType.STOCK ||
                type == AssetType.ETF ||
                type == AssetType.CRYPTO ||
                type == AssetType.MUTUAL_FUND;
    }
}

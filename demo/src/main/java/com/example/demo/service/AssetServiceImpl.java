package com.example.demo.service;

import com.example.demo.dto.AssetDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of AssetService with support for multiple asset types.
 * Uses polymorphic queries across all asset repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetServiceImpl implements AssetService {

    private final StockRepository stockRepository;
    private final BondRepository bondRepository;
    private final EtfRepository etfRepository;
    private final MutualFundRepository mutualFundRepository;
    private final CryptoRepository cryptoRepository;
    private final RealEstateRepository realEstateRepository;
    private final CashRepository cashRepository;
    private final StockPriceService stockPriceService;

    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssets() {
        List<BaseAsset> allAssets = new ArrayList<>();

        allAssets.addAll(stockRepository.findAll());
        allAssets.addAll(bondRepository.findAll());
        allAssets.addAll(etfRepository.findAll());
        allAssets.addAll(mutualFundRepository.findAll());
        allAssets.addAll(cryptoRepository.findAll());
        allAssets.addAll(realEstateRepository.findAll());
        allAssets.addAll(cashRepository.findAll());

        return allAssets.stream()
                .map(this::enrichAssetDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        BaseAsset asset = findAssetById(id);
        return enrichAssetDTO(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> getAssetsByType(AssetType type) {
        List<? extends BaseAsset> assets = switch (type) {
            case STOCK -> stockRepository.findAll();
            case BOND -> bondRepository.findAll();
            case ETF -> etfRepository.findAll();
            case MUTUAL_FUND -> mutualFundRepository.findAll();
            case CRYPTO -> cryptoRepository.findAll();
            case REAL_ESTATE -> realEstateRepository.findAll();
            case CASH -> cashRepository.findAll();
        };

        return assets.stream()
                .map(this::enrichAssetDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> searchAssets(String query) {
        if (query == null || query.isBlank()) {
            return getAllAssets();
        }

        Set<BaseAsset> combined = new LinkedHashSet<>();

        // Search across all repositories
        combined.addAll(stockRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(stockRepository.findByNameContainingIgnoreCase(query));
        combined.addAll(bondRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(bondRepository.findByNameContainingIgnoreCase(query));
        combined.addAll(etfRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(etfRepository.findByNameContainingIgnoreCase(query));
        combined.addAll(mutualFundRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(mutualFundRepository.findByNameContainingIgnoreCase(query));
        combined.addAll(cryptoRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(cryptoRepository.findByNameContainingIgnoreCase(query));
        combined.addAll(realEstateRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(realEstateRepository.findByNameContainingIgnoreCase(query));
        combined.addAll(cashRepository.findBySymbolContainingIgnoreCase(query));
        combined.addAll(cashRepository.findByNameContainingIgnoreCase(query));

        return combined.stream()
                .map(this::enrichAssetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AssetDTO createAsset(AssetDTO assetDTO) {
        BaseAsset saved = switch (assetDTO.getType()) {
            case STOCK -> stockRepository.save(Stock.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
            case BOND -> bondRepository.save(Bond.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
            case ETF -> etfRepository.save(Etf.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
            case MUTUAL_FUND -> mutualFundRepository.save(MutualFund.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
            case CRYPTO -> cryptoRepository.save(Crypto.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
            case REAL_ESTATE -> realEstateRepository.save(RealEstate.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
            case CASH -> cashRepository.save(Cash.builder()
                    .symbol(assetDTO.getSymbol().toUpperCase().trim())
                    .name(assetDTO.getName().trim())
                    .quantity(assetDTO.getQuantity())
                    .buyPrice(assetDTO.getBuyPrice())
                    .purchaseDate(assetDTO.getPurchaseDate())
                    .build());
        };

        log.info("Created new {} asset: {} ({})", saved.getType(), saved.getName(), saved.getSymbol());
        return enrichAssetDTO(saved);
    }

    @Override
    public AssetDTO updateAsset(Long id, AssetDTO assetDTO) {
        BaseAsset existing = findAssetById(id);

        existing.setSymbol(assetDTO.getSymbol().toUpperCase().trim());
        existing.setName(assetDTO.getName().trim());
        existing.setQuantity(assetDTO.getQuantity());
        existing.setBuyPrice(assetDTO.getBuyPrice());
        existing.setPurchaseDate(assetDTO.getPurchaseDate());

        BaseAsset updated = saveAsset(existing);
        log.info("Updated {} asset: {} (ID: {})", updated.getType(), updated.getName(), updated.getId());
        return enrichAssetDTO(updated);
    }

    @Override
    public void deleteAsset(Long id) {
        BaseAsset asset = findAssetById(id);
        deleteAssetEntity(asset);
        log.info("Deleted {} asset: {} (ID: {})", asset.getType(), asset.getName(), id);
    }

    /**
     * Find asset by ID across all repositories.
     */
    private BaseAsset findAssetById(Long id) {
        return Stream.<Optional<? extends BaseAsset>>of(
                stockRepository.findById(id),
                bondRepository.findById(id),
                etfRepository.findById(id),
                mutualFundRepository.findById(id),
                cryptoRepository.findById(id),
                realEstateRepository.findById(id),
                cashRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
    }

    /**
     * Save asset to appropriate repository.
     */
    private BaseAsset saveAsset(BaseAsset asset) {
        if (asset instanceof Stock s) {
            return stockRepository.save(s);
        } else if (asset instanceof Bond b) {
            return bondRepository.save(b);
        } else if (asset instanceof Etf e) {
            return etfRepository.save(e);
        } else if (asset instanceof MutualFund m) {
            return mutualFundRepository.save(m);
        } else if (asset instanceof Crypto c) {
            return cryptoRepository.save(c);
        } else if (asset instanceof RealEstate r) {
            return realEstateRepository.save(r);
        } else if (asset instanceof Cash c) {
            return cashRepository.save(c);
        }
        throw new IllegalStateException("Unknown asset type: " + asset.getClass());
    }

    /**
     * Delete asset from appropriate repository.
     */
    private void deleteAssetEntity(BaseAsset asset) {
        if (asset instanceof Stock s) {
            stockRepository.delete(s);
        } else if (asset instanceof Bond b) {
            bondRepository.delete(b);
        } else if (asset instanceof Etf e) {
            etfRepository.delete(e);
        } else if (asset instanceof MutualFund m) {
            mutualFundRepository.delete(m);
        } else if (asset instanceof Crypto c) {
            cryptoRepository.delete(c);
        } else if (asset instanceof RealEstate r) {
            realEstateRepository.delete(r);
        } else if (asset instanceof Cash c) {
            cashRepository.delete(c);
        } else {
            throw new IllegalStateException("Unknown asset type: " + asset.getClass());
        }
    }

    /**
     * Convert BaseAsset entity to DTO and enrich with current price data.
     */
    private AssetDTO enrichAssetDTO(BaseAsset asset) {
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
                setDefaultPricing(dto, asset);
            }
        } else {
            setDefaultPricing(dto, asset);
        }

        return dto;
    }

    /**
     * Set default pricing when live price is not available.
     */
    private void setDefaultPricing(AssetDTO dto, BaseAsset asset) {
        dto.setCurrentPrice(asset.getBuyPrice());
        dto.setCurrentValue(asset.getCostBasis());
        dto.setGainLoss(BigDecimal.ZERO);
        dto.setGainLossPercentage(BigDecimal.ZERO);
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

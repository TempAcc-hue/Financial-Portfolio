package com.example.demo.dto;

import com.example.demo.entity.AssetType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Asset entity.
 * Used for API requests and responses with enriched data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDTO {

    private Long id;

    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Asset type is required")
    private AssetType type;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Buy price is required")
    @DecimalMin(value = "0.01", message = "Buy price must be greater than 0")
    private BigDecimal buyPrice;

    private LocalDate purchaseDate;

    // Read-only fields (enriched by service)
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal costBasis;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercentage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}

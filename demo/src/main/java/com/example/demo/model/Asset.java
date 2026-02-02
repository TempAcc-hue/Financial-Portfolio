package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data // Lombok for Getters, Setters, toString
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Financial Best Practice: Use Enums for fixed types to ensure data consistency
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    // For Stocks: "AAPL"; For Real Estate: "123 Main St"; For Cash: "USD"
    @Column(nullable = false)
    private String nameOrTicker;

    // Financial Best Practice: Use BigDecimal for money/financial precision
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    // Optional: Notes field (from your "Dump Excel sheet" idea)
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssetType getType() {
        return type;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    public String getNameOrTicker() {
        return nameOrTicker;
    }

    public void setNameOrTicker(String nameOrTicker) {
        this.nameOrTicker = nameOrTicker;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

// Create this Enum in the same package or a dedicated enums package
enum AssetType {
    STOCK,
    BOND,
    CASH,
    REAL_ESTATE,
    CRYPTO,
    ETF,
    MUTUAL_FUND,
    FUTURES,
    OPTIONS
}
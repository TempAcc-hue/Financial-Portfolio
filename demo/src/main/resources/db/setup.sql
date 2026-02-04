-- Portfolio Management Database Schema
-- Uses JOINED inheritance: base 'assets' table + type-specific tables with FK
-- Run this script to set up the database from scratch

CREATE DATABASE IF NOT EXISTS portfolio_db;

-- Select the database to use
USE portfolio_db;

-- ============================================
-- BASE TABLE: Contains common fields for all assets
-- ============================================
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_type VARCHAR(31) NOT NULL COMMENT 'Discriminator: STOCK, BOND, ETF, MUTUAL_FUND, CRYPTO, REAL_ESTATE, CASH',
    symbol VARCHAR(20) NOT NULL COMMENT 'Ticker symbol or asset code',
    name VARCHAR(100) NOT NULL COMMENT 'Full name of the asset',
    quantity DECIMAL(19, 4) NOT NULL COMMENT 'Number of units held',
    buy_price DECIMAL(19, 2) NOT NULL COMMENT 'Average purchase price per unit',
    purchase_date DATE COMMENT 'Date of purchase',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_asset_type (asset_type),
    INDEX idx_symbol (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- SUBCLASS TABLES: Only type-specific fields + FK to assets
-- ============================================

-- STOCKS table
CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT PRIMARY KEY,
    exchange VARCHAR(20) COMMENT 'NYSE, NASDAQ, etc.',
    sector VARCHAR(50) COMMENT 'Technology, Healthcare, etc.',
    dividend_yield DECIMAL(5, 2) COMMENT 'Annual dividend yield %',
    market_cap VARCHAR(20) COMMENT 'Large, Mid, Small',

    CONSTRAINT fk_stocks_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- BONDS table
CREATE TABLE IF NOT EXISTS bonds (
    id BIGINT PRIMARY KEY,
    coupon_rate DECIMAL(5, 2) COMMENT 'Annual coupon rate %',
    maturity_date DATE COMMENT 'Bond maturity date',
    issuer VARCHAR(100) COMMENT 'Government, Corporate name, etc.',
    bond_type VARCHAR(30) COMMENT 'GOVERNMENT, CORPORATE, MUNICIPAL, TREASURY, AGENCY',
    credit_rating VARCHAR(10) COMMENT 'AAA, AA, A, BBB, etc.',

    CONSTRAINT fk_bonds_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ETFS table
CREATE TABLE IF NOT EXISTS etfs (
    id BIGINT PRIMARY KEY,
    exchange VARCHAR(20) COMMENT 'NYSE, NASDAQ, etc.',
    expense_ratio DECIMAL(5, 4) COMMENT 'Annual expense ratio (e.g., 0.0003)',
    category VARCHAR(50) COMMENT 'Index, Sector, Bond, etc.',
    holdings_count INT COMMENT 'Number of holdings in ETF',
    dividend_yield DECIMAL(5, 2) COMMENT 'Annual dividend yield %',

    CONSTRAINT fk_etfs_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- MUTUAL_FUNDS table
CREATE TABLE IF NOT EXISTS mutual_funds (
    id BIGINT PRIMARY KEY,
    fund_family VARCHAR(50) COMMENT 'Vanguard, Fidelity, etc.',
    expense_ratio DECIMAL(5, 4) COMMENT 'Annual expense ratio',
    category VARCHAR(50) COMMENT 'Growth, Value, Blend, etc.',
    minimum_investment DECIMAL(19, 2) COMMENT 'Minimum investment amount',
    dividend_yield DECIMAL(5, 2) COMMENT 'Annual dividend yield %',

    CONSTRAINT fk_mutual_funds_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CRYPTOS table
CREATE TABLE IF NOT EXISTS cryptos (
    id BIGINT PRIMARY KEY,
    blockchain VARCHAR(30) COMMENT 'Bitcoin, Ethereum, Solana, etc.',
    wallet_address VARCHAR(100) COMMENT 'Wallet address',
    staking_enabled BIT(1) COMMENT 'Is staking enabled',
    staking_apy DECIMAL(5, 2) COMMENT 'Annual staking yield %',

    CONSTRAINT fk_cryptos_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- REAL_ESTATES table
CREATE TABLE IF NOT EXISTS real_estates (
    id BIGINT PRIMARY KEY,
    property_address VARCHAR(200) COMMENT 'Property address',
    property_type VARCHAR(30) COMMENT 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL, LAND, REIT',
    rental_income DECIMAL(19, 2) COMMENT 'Monthly rental income',
    square_footage INT COMMENT 'Property size in sq ft',
    year_built INT COMMENT 'Year property was built',

    CONSTRAINT fk_real_estates_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CASH_HOLDINGS table
CREATE TABLE IF NOT EXISTS cash_holdings (
    id BIGINT PRIMARY KEY,
    currency VARCHAR(3) COMMENT 'USD, EUR, GBP, etc.',
    account_type VARCHAR(30) COMMENT 'SAVINGS, CHECKING, MONEY_MARKET, CD, HIGH_YIELD_SAVINGS',
    interest_rate DECIMAL(5, 2) COMMENT 'Annual interest rate %',
    bank_name VARCHAR(50) COMMENT 'Bank name',

    CONSTRAINT fk_cash_holdings_assets FOREIGN KEY (id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- SAMPLE DATA
-- ============================================

-- Insert stocks
INSERT INTO assets (asset_type, symbol, name, quantity, buy_price, purchase_date) VALUES
('STOCK', 'AAPL', 'Apple Inc.', 50.0000, 150.00, '2024-01-15'),
('STOCK', 'GOOGL', 'Alphabet Inc.', 25.0000, 140.00, '2024-02-20'),
('STOCK', 'MSFT', 'Microsoft Corporation', 30.0000, 380.00, '2024-03-10'),
('STOCK', 'AMZN', 'Amazon.com Inc.', 20.0000, 175.00, '2024-01-05'),
('STOCK', 'TSLA', 'Tesla Inc.', 15.0000, 250.00, '2024-04-01');

INSERT INTO stocks (id, exchange, sector, market_cap)
SELECT id, 'NASDAQ', 'Technology', 'Large' FROM assets WHERE symbol IN ('AAPL', 'GOOGL', 'MSFT', 'AMZN');
INSERT INTO stocks (id, exchange, sector, market_cap)
SELECT id, 'NASDAQ', 'Automotive', 'Large' FROM assets WHERE symbol = 'TSLA';

-- Insert ETFs
INSERT INTO assets (asset_type, symbol, name, quantity, buy_price, purchase_date) VALUES
('ETF', 'SPY', 'SPDR S&P 500 ETF Trust', 40.0000, 480.00, '2024-02-15'),
('ETF', 'VTI', 'Vanguard Total Stock Market ETF', 35.0000, 245.00, '2024-03-01');

INSERT INTO etfs (id, exchange, expense_ratio, category)
SELECT id, 'NYSE', 0.0009, 'Index' FROM assets WHERE symbol = 'SPY';
INSERT INTO etfs (id, exchange, expense_ratio, category)
SELECT id, 'NYSE', 0.0003, 'Total Market' FROM assets WHERE symbol = 'VTI';

-- Insert Bonds
INSERT INTO assets (asset_type, symbol, name, quantity, buy_price, purchase_date) VALUES
('BOND', 'BND', 'Vanguard Total Bond Market ETF', 100.0000, 72.50, '2024-01-10');

INSERT INTO bonds (id, coupon_rate, issuer, bond_type)
SELECT id, 3.50, 'Vanguard', 'CORPORATE' FROM assets WHERE symbol = 'BND';

-- Insert Crypto
INSERT INTO assets (asset_type, symbol, name, quantity, buy_price, purchase_date) VALUES
('CRYPTO', 'BTC-USD', 'Bitcoin', 0.5000, 42000.00, '2024-01-20');

INSERT INTO cryptos (id, blockchain, staking_enabled)
SELECT id, 'Bitcoin', 0 FROM assets WHERE symbol = 'BTC-USD';

-- Insert Mutual Funds
INSERT INTO assets (asset_type, symbol, name, quantity, buy_price, purchase_date) VALUES
('MUTUAL_FUND', 'VFIAX', 'Vanguard 500 Index Fund', 25.0000, 420.00, '2024-02-28');

INSERT INTO mutual_funds (id, fund_family, expense_ratio, category)
SELECT id, 'Vanguard', 0.0004, 'Index' FROM assets WHERE symbol = 'VFIAX';

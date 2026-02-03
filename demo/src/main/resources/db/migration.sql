-- Migration script to create new asset tables
-- Run this after starting the application (Hibernate will auto-create tables)
-- This script migrates data from old 'assets' table to new type-specific tables

USE portfolio_db;

-- Migrate stocks
INSERT INTO stocks (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'STOCK';

-- Migrate bonds  
INSERT INTO bonds (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'BOND';

-- Migrate ETFs
INSERT INTO etfs (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'ETF';

-- Migrate mutual funds
INSERT INTO mutual_funds (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'MUTUAL_FUND';

-- Migrate crypto
INSERT INTO cryptos (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'CRYPTO';

-- Migrate real estate
INSERT INTO real_estates (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'REAL_ESTATE';

-- Migrate cash
INSERT INTO cash_holdings (id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at)
SELECT id, symbol, name, quantity, buy_price, purchase_date, created_at, updated_at
FROM assets WHERE type = 'CASH';

-- Optional: After verifying migration, you can drop the old table
-- DROP TABLE IF EXISTS assets;

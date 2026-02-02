-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS portfolio_db;

-- Select the database to use
USE portfolio_db;

-- Create the Asset table
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL COMMENT 'Stock Ticker (e.g., AAPL) or Asset Code',
    name VARCHAR(100) NOT NULL COMMENT 'Full name of the asset',
    
    -- Enum covering types mentioned in your requirements and standard financial assets
    -- Includes 'REAL_ESTATE' as per your handwritten notes [Image 1]
    type ENUM('STOCK', 'BOND', 'CASH', 'REAL_ESTATE', 'CRYPTO', 'ETF', 'MUTUAL_FUND') NOT NULL,
    
    -- Using DECIMAL(19, 4) ensures precision for fractional shares (common in Crypto/ETFs)
    quantity DECIMAL(19, 4) NOT NULL,
    
    -- Using DECIMAL(19, 2) is standard for currency. 
    -- Represents the cost basis (Average Buy Price per unit).
    buy_price DECIMAL(19, 2) NOT NULL,
    
    -- timestamps for auditing (Best Practice)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

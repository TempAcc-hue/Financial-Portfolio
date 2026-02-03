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
    type ENUM('STOCK', 'BOND', 'CASH', 'REAL_ESTATE', 'CRYPTO', 'ETF', 'MUTUAL_FUND') NOT NULL,
    
    -- Using DECIMAL(19, 4) ensures precision for fractional shares (common in Crypto/ETFs)
    quantity DECIMAL(19, 4) NOT NULL,
    
    -- Using DECIMAL(19, 2) is standard for currency. 
    -- Represents the cost basis (Average Buy Price per unit).
    buy_price DECIMAL(19, 2) NOT NULL,
    
    -- Date when the asset was purchased
    purchase_date DATE,
    
    -- timestamps for auditing (Best Practice)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO assets (symbol, name, type, quantity, buy_price, purchase_date) VALUES
('AAPL', 'Apple Inc.', 'STOCK', 50.0000, 150.00, '2024-01-15'),
('GOOGL', 'Alphabet Inc.', 'STOCK', 25.0000, 140.00, '2024-02-20'),
('MSFT', 'Microsoft Corporation', 'STOCK', 30.0000, 380.00, '2024-03-10'),
('AMZN', 'Amazon.com Inc.', 'STOCK', 20.0000, 175.00, '2024-01-05'),
('TSLA', 'Tesla Inc.', 'STOCK', 15.0000, 250.00, '2024-04-01'),
('BND', 'Vanguard Total Bond Market ETF', 'BOND', 100.0000, 72.50, '2024-01-10'),
('SPY', 'SPDR S&P 500 ETF Trust', 'ETF', 40.0000, 480.00, '2024-02-15'),
('VTI', 'Vanguard Total Stock Market ETF', 'ETF', 35.0000, 245.00, '2024-03-01'),
('BTC-USD', 'Bitcoin', 'CRYPTO', 0.5000, 42000.00, '2024-01-20'),
('VFIAX', 'Vanguard 500 Index Fund', 'MUTUAL_FUND', 25.0000, 420.00, '2024-02-28');

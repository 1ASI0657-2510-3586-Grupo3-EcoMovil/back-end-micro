-- Plans Service Database Initialization
-- MySQL initialization script

-- Use the database
USE ecomovil_plans_db;

-- Create Plan2 table
CREATE TABLE IF NOT EXISTS plan2s (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample plans
INSERT INTO plan2s (name, description, price) VALUES 
    ('Basic Plan', 'Basic plan with limited features for individual users', 9.99),
    ('Premium Plan', 'Premium plan with advanced features for small businesses', 29.99),
    ('Enterprise Plan', 'Enterprise plan with full features for large organizations', 99.99),
    ('Student Plan', 'Discounted plan for students and educational institutions', 4.99),
    ('Family Plan', 'Family plan for up to 5 family members', 19.99)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description),
    price = VALUES(price),
    updated_at = CURRENT_TIMESTAMP;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_plan2s_name ON plan2s(name);
CREATE INDEX IF NOT EXISTS idx_plan2s_price ON plan2s(price);

-- Add comments
ALTER TABLE plan2s COMMENT = 'Plans table for Plans service';

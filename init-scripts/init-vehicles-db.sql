-- Vehicles Service Database Initialization
-- MySQL initialization script

-- Use the database
USE ecomovil_vehicles_db;

-- Create vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    
    -- Details (embedded value object)
    type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    
    -- Review (embedded value object)
    review_value INT DEFAULT 0,
    
    -- Prices (embedded value object)
    price_rent DECIMAL(10,2) NOT NULL,
    price_sell DECIMAL(12,2),
    
    -- Additional fields
    is_available BOOLEAN DEFAULT TRUE,
    image_url TEXT,
    latitude FLOAT,
    longitude FLOAT,
    description TEXT,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Add indexes for better performance
    INDEX idx_vehicles_owner_id (owner_id),
    INDEX idx_vehicles_type (type),
    INDEX idx_vehicles_available (is_available),
    INDEX idx_vehicles_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample vehicles for testing
INSERT INTO vehicles (owner_id, type, name, year, review_value, price_rent, price_sell, is_available, image_url, latitude, longitude, description) VALUES 
    (1, 'sedan', 'Toyota Camry 2022', 2022, 5, 45.00, 28000.00, TRUE, 'https://example.com/camry.jpg', -12.0464, -77.0428, 'Comfortable sedan perfect for city and highway driving'),
    (1, 'suv', 'Honda CR-V 2023', 2023, 4, 65.00, 35000.00, TRUE, 'https://example.com/crv.jpg', -12.0464, -77.0428, 'Spacious SUV ideal for families and outdoor adventures'),
    (2, 'hatchback', 'Nissan Versa 2021', 2021, 4, 35.00, 18000.00, TRUE, 'https://example.com/versa.jpg', -12.0500, -77.0500, 'Economical hatchback with great fuel efficiency'),
    (2, 'pickup', 'Ford Ranger 2022', 2022, 5, 75.00, 42000.00, FALSE, 'https://example.com/ranger.jpg', -12.0600, -77.0600, 'Powerful pickup truck for work and recreation'),
    (3, 'coupe', 'BMW 320i 2023', 2023, 5, 85.00, 55000.00, TRUE, 'https://example.com/bmw320i.jpg', -12.0400, -77.0400, 'Luxury coupe with premium features and performance')
ON DUPLICATE KEY UPDATE 
    type = VALUES(type),
    name = VALUES(name),
    year = VALUES(year),
    review_value = VALUES(review_value),
    price_rent = VALUES(price_rent),
    price_sell = VALUES(price_sell),
    is_available = VALUES(is_available),
    image_url = VALUES(image_url),
    latitude = VALUES(latitude),
    longitude = VALUES(longitude),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;

-- Create additional indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_vehicles_price_rent ON vehicles (price_rent);
CREATE INDEX IF NOT EXISTS idx_vehicles_year ON vehicles (year);
CREATE INDEX IF NOT EXISTS idx_vehicles_review ON vehicles (review_value);

COMMIT;

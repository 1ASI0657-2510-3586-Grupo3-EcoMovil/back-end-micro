-- Reservations Service Database Initialization
-- MySQL initialization script

-- Use the database
USE ecomovil_reservations_db;

-- Create reservations table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    
    -- Reservation details
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    start_date VARCHAR(255),
    end_date VARCHAR(255),
    
    -- Pricing information
    total_price DECIMAL(12,2),
    
    -- Additional details
    reservation_type VARCHAR(50),
    notes TEXT,
    
    -- Audit fields (from AuditableAbstractAggregateRoot)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for better performance
    INDEX idx_user_id (user_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_status (status),
    INDEX idx_reservation_type (reservation_type)
);

-- Insert some sample reservations for testing
INSERT INTO reservations (user_id, vehicle_id, status, start_date, end_date, total_price, reservation_type, notes) VALUES
(1, 1, 'CONFIRMED', '2025-01-15 10:00:00', '2025-01-17 18:00:00', 150.00, 'rent', 'Weekend bike rental'),
(2, 2, 'PENDING', '2025-01-20 09:00:00', '2025-01-22 17:00:00', 320.00, 'rent', 'Business trip scooter rental'),
(1, 3, 'COMPLETED', '2025-01-10 08:00:00', '2025-01-12 20:00:00', 280.00, 'rent', 'Previous car rental - completed'),
(3, 1, 'CANCELLED', '2025-01-25 14:00:00', '2025-01-26 16:00:00', 75.00, 'rent', 'Cancelled due to weather');

-- Grant necessary permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ecomovil_reservations_db.* TO 'reservations_user'@'%';

FLUSH PRIVILEGES;

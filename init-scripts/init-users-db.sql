-- Users Service Database Initialization
-- MySQL initialization script

-- Use the database
USE ecomovil_users_db;

-- Create profiles table
CREATE TABLE IF NOT EXISTS profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    ruc_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Add constraints
    CONSTRAINT uk_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_profiles_ruc_number UNIQUE (ruc_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample profiles for testing
INSERT INTO profiles (user_id, plan_id, first_name, last_name, address, phone_number, ruc_number) VALUES 
    (1, 1, 'Admin', 'User', 'admin@ecomovil.com', '+51999999999', '12345678901'),
    (2, 2, 'John', 'Doe', 'john.doe@example.com', '+51987654321', '20123456789'),
    (3, 1, 'Jane', 'Smith', 'jane.smith@example.com', '+51876543210', '20987654321')
ON DUPLICATE KEY UPDATE 
    plan_id = VALUES(plan_id),
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    address = VALUES(address),
    phone_number = VALUES(phone_number),
    ruc_number = VALUES(ruc_number),
    updated_at = CURRENT_TIMESTAMP;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_plan_id ON profiles(plan_id);
CREATE INDEX IF NOT EXISTS idx_profiles_ruc_number ON profiles(ruc_number);
CREATE INDEX IF NOT EXISTS idx_profiles_name ON profiles(first_name, last_name);

-- Add comments
ALTER TABLE profiles COMMENT = 'User profiles table for Users service';

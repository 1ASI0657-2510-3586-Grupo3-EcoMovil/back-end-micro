
# Users Service Database Configuration
CREATE DATABASE IF NOT EXISTS ecomovil_users_db;
CREATE USER IF NOT EXISTS 'users_user'@'%' IDENTIFIED BY 'users_password';
GRANT ALL PRIVILEGES ON ecomovil_users_db.* TO 'users_user'@'%';
FLUSH PRIVILEGES;


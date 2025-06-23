# Vehicles Microservice - Complete Setup Guide

A Spring Boot microservice for managing vehicles in the Ecomovil platform, following Domain-Driven Design (DDD) principles and microservices architecture.

## ✅ Implementation Status

### Completed Features
- ✅ Complete DDD architecture with domain, application, infrastructure, and interface layers
- ✅ Vehicle entity with value objects (Details, Prices, Review)
- ✅ CQRS pattern with commands and queries
- ✅ Complete CRUD operations (Create, Read, Update, Delete)
- ✅ JWT-based authentication and authorization
- ✅ Role-based access control (USER, ADMIN)
- ✅ Integration with Users microservice for owner validation
- ✅ RESTful API with comprehensive endpoints
- ✅ Security configuration with JWT filters
- ✅ Docker support with individual and integrated docker-compose
- ✅ Database initialization scripts
- ✅ Unit tests and test configuration
- ✅ Swagger/OpenAPI documentation
- ✅ Health checks and monitoring endpoints

## Architecture Overview

```
src/main/java/upc/edu/ecomovil/microservices/vehicles/
├── domain/                     # Domain Layer (Business Logic)
│   ├── model/
│   │   ├── aggregates/        # Vehicle entity
│   │   ├── commands/          # Create/Update/Delete commands
│   │   ├── queries/           # Get vehicles queries  
│   │   └── valueobjects/      # Details, Prices, Review
│   └── services/              # Domain service interfaces
├── application/               # Application Layer
│   ├── internal/
│   │   ├── commandservices/   # Command handlers
│   │   ├── queryservices/     # Query handlers
│   │   └── outboundservices/  # External service integrations
├── infrastructure/            # Infrastructure Layer
│   ├── persistence/jpa/       # JPA repositories
│   └── security/              # JWT security, filters, config
├── interfaces/                # Interface Layer
│   └── rest/                  # REST controllers and DTOs
└── shared/                    # Shared domain models
```

## Quick Start

### 1. Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### 2. Run Individual Service
```bash
# Go to vehicles directory
cd vehicles/

# Run standalone with database
docker-compose up --build

# Service will be available at http://localhost:8082
```

### 3. Run Complete Microservices Stack
```bash
# From parent directory
cd ../
docker-compose up --build

# Vehicles service at http://localhost:8082
# IAM service at http://localhost:8080
# Users service at http://localhost:8081
```

### 4. API Documentation
- Swagger UI: http://localhost:8082/swagger-ui/
- Health Check: http://localhost:8082/actuator/health

## API Endpoints

### Authentication Required (JWT Token)

#### Vehicle Management
```bash
# Create vehicle (authenticated users)
POST /api/v1/vehicles
Content-Type: application/json
Authorization: Bearer <jwt-token>
{
  "type": "sedan",
  "name": "Toyota Camry 2023",
  "year": 2023,
  "review": 5,
  "priceRent": 50.0,
  "priceSell": 30000.0,
  "isAvailable": true,
  "imageUrl": "https://example.com/car.jpg",
  "lat": -12.0464,
  "lng": -77.0428,
  "description": "Comfortable sedan"
}

# Get my vehicles
GET /api/v1/vehicles/my-vehicles
Authorization: Bearer <jwt-token>

# Get vehicle by ID (owner or admin only)
GET /api/v1/vehicles/{id}
Authorization: Bearer <jwt-token>

# Update vehicle (owner or admin only)
PUT /api/v1/vehicles/{id}
Authorization: Bearer <jwt-token>

# Delete vehicle (owner or admin only)
DELETE /api/v1/vehicles/{id}
Authorization: Bearer <jwt-token>
```

#### Search & Filter
```bash
# Get vehicles by type
GET /api/v1/vehicles/type/{type}
Authorization: Bearer <jwt-token>

# Admin endpoints
GET /api/v1/vehicles/admin/all
GET /api/v1/vehicles/admin/owner/{ownerId}
Authorization: Bearer <jwt-token> (ROLE_ADMIN required)
```

### Public Endpoints
- `GET /actuator/health` - Health check
- `GET /swagger-ui/` - API documentation

## Configuration

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://vehicles-db:3306/ecomovil_vehicles_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password

# JWT Security
AUTHORIZATION_JWT_SECRET=mySecretKeyForJWTTokenValidation...

# External Services
USERS_SERVICE_BASE_URL=http://users-service:8081
IAM_SERVICE_BASE_URL=http://iam-service:8080

# Profile
SPRING_PROFILES_ACTIVE=docker
```

## Testing the API

### 1. Get JWT Token from IAM
```bash
# First authenticate with IAM service
curl -X POST http://localhost:8080/api/v1/authentication/sign-in \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'

# Extract the token from response
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

### 2. Create a Vehicle
```bash
curl -X POST http://localhost:8082/api/v1/vehicles \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "sedan",
    "name": "Honda Civic 2023",
    "year": 2023,
    "review": 4,
    "priceRent": 45.0,
    "priceSell": 28000.0,
    "isAvailable": true,
    "imageUrl": "https://example.com/civic.jpg",
    "lat": -12.0464,
    "lng": -77.0428,
    "description": "Efficient and reliable sedan"
  }'
```

### 3. Get Your Vehicles
```bash
curl -X GET http://localhost:8082/api/v1/vehicles/my-vehicles \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4. Search by Type
```bash
curl -X GET http://localhost:8082/api/v1/vehicles/type/sedan \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Database Schema

### Vehicles Table
```sql
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    
    -- Details (Value Object)
    type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    
    -- Review (Value Object)
    review_value INT DEFAULT 0,
    
    -- Prices (Value Object)
    price_rent DECIMAL(10,2) NOT NULL,
    price_sell DECIMAL(12,2),
    
    -- Additional Properties
    is_available BOOLEAN DEFAULT TRUE,
    image_url TEXT,
    latitude FLOAT,
    longitude FLOAT,
    description TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Security Model

### Role-Based Access Control
- **USER Role**: Can create, view, update, and delete their own vehicles
- **ADMIN Role**: Full access to all vehicles and admin endpoints

### JWT Integration
- Validates tokens issued by IAM microservice
- Extracts user ID and roles from JWT claims
- Automatically sets Spring Security context

### Authorization Rules
```java
// Users can manage their own vehicles
@PreAuthorize("hasRole('USER') and #vehicle.ownerId == authentication.principal.userId")

// Admins can access everything
@PreAuthorize("hasRole('ADMIN')")

// Vehicle access validation in controller
if (!isAdmin && !vehicle.getOwnerId().equals(userId)) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

## Integration with Other Services

### Users Microservice Integration
```java
// Validates vehicle owners before creation
var owner = externalUserService.fetchUserProfileById(command.ownerId());
if (owner.isEmpty()) {
    throw new IllegalArgumentException("Profile with ID does not exist");
}
```

### IAM Microservice Integration
- Uses shared JWT secret for token validation
- Extracts user authentication details from tokens
- Supports role-based authorization

## Development

### Local Development
```bash
# Run with H2 database
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Run tests
mvn test
```

### Adding New Features
1. **Domain First**: Start with domain model changes
2. **Commands/Queries**: Add CQRS objects
3. **Services**: Implement domain and application services
4. **REST API**: Create controllers and DTOs
5. **Tests**: Write comprehensive unit and integration tests

## Monitoring & Observability

### Health Endpoints
- `/actuator/health` - Service health
- `/actuator/info` - Service info
- `/actuator/metrics` - Metrics

### Logging
- Structured logging with SLF4J
- Different log levels for development and production
- Request/response logging for debugging

## Troubleshooting

### Common Issues
1. **Service won't start**: Check database connectivity and JWT secret
2. **Authentication errors**: Verify IAM service is running and JWT token is valid
3. **Vehicle creation fails**: Ensure Users service is accessible
4. **Permission denied**: Check user roles and vehicle ownership

### Debug Commands
```bash
# Check service health
curl http://localhost:8082/actuator/health

# View logs
docker logs vehicles-service

# Test database connection
docker exec -it vehicles-db mysql -u root -p ecomovil_vehicles_db
```

## Files Structure

### Key Implementation Files
- **Domain Layer**: `Vehicle.java`, `Details.java`, `Prices.java`, `Review.java`
- **Commands**: `CreateVehicleCommand.java`, `UpdateVehicleCommand.java`, `DeleteVehicleCommand.java`
- **Queries**: `GetAllVehiclesQuery.java`, `GetVehicleByIdQuery.java`, etc.
- **Services**: `VehicleCommandServiceImpl.java`, `VehicleQueryServiceImpl.java`
- **Security**: `JwtAuthenticationFilter.java`, `JwtUtils.java`, `WebSecurityConfig.java`
- **Controller**: `VehicleController.java`
- **Configuration**: `application.properties`, `application-docker.properties`
- **Docker**: `Dockerfile`, `docker-compose.yml`
- **Database**: `init-vehicles-db.sql`

This microservice is fully implemented and ready for integration with the complete Ecomovil platform.

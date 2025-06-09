# IAM Service - Setup Instructions

## Database Setup

La base de datos PostgreSQL ya está corriendo en Docker:
```bash
cd /Users/davidgallo/Documents/GitHub/microservices-ecomovil/carpetaoriginal/carpeta-microservicio
docker compose up -d iam-db
```

**Database Configuration:**
- Host: localhost
- Port: 5433
- Database: ecomovil_iam_db
- Username: iam_user
- Password: iam_password

## Running from IntelliJ IDEA

### 1. Import Project
- Open IntelliJ IDEA
- Import the IAM service project from: `/Users/davidgallo/Documents/GitHub/microservices-ecomovil/carpetaoriginal/carpeta-microservicio/iam`

### 2. Run Configuration
- Main Class: `upc.edu.ecomovil.microservices.iam.IAMServiceApplication`
- VM Options (if needed): `-Dspring.profiles.active=dev`
- Program Arguments: (leave empty)
- Working Directory: `/Users/davidgallo/Documents/GitHub/microservices-ecomovil/carpetaoriginal/carpeta-microservicio/iam`

### 3. Environment Variables (optional)
```
DB_URL=jdbc:postgresql://localhost:5433/ecomovil_iam_db
DB_USERNAME=iam_user
DB_PASSWORD=iam_password
JWT_SECRET=mySecretKey
SERVER_PORT=8080
```

## API Endpoints

### Authentication Endpoints
- **POST** `/api/v1/authentication/sign-up` - Register new user
- **POST** `/api/v1/authentication/sign-in` - Login user

### User Management Endpoints
- **GET** `/api/v1/users` - Get all users (Admin only)
- **GET** `/api/v1/users/{userId}` - Get user by ID
- **GET** `/api/v1/users/username/{username}` - Get user by username

### Swagger UI
- Access at: http://localhost:8080/swagger-ui.html

## Test Requests

### 1. Sign Up
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123",
    "roles": ["ROLE_ADMIN"]
  }'
```

### 2. Sign In
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-in \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

## Port Configuration

**IAM Service**: Puerto 8080 (servicio principal de autenticación)
**Plans Service**: Puerto 8081

## Architecture Components

✅ **Domain Layer**: User/Role aggregates, Commands, Queries, Value Objects
✅ **Application Layer**: Command/Query services implemented
✅ **Infrastructure Layer**: JPA repositories, JWT token service, BCrypt hashing, SNS events
✅ **Interface Layer**: REST controllers, Resources, Transformers
✅ **Security**: Spring Security + JWT authentication
✅ **Database**: PostgreSQL with JPA/Hibernate
✅ **AWS Integration**: SNS for event publishing (LocalStack ready)

## Next Steps

1. ✅ Database running (PostgreSQL on port 5433)
2. ✅ All layers implemented
3. ✅ REST controllers ready
4. ⏳ **Test in IntelliJ** - Ready to run!
5. ⏳ Create User Service (port 8082)
6. ⏳ Create Vehicle Service (port 8083)
7. ⏳ Create Reservations Service (port 8084)
8. ⏳ Create Forum Service (port 8085)

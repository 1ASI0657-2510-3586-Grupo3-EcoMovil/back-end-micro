# ===============================================
# EJEMPLO DE CONFIGURACIÓN COMPLETA PARA TESTING
# ===============================================

# Este archivo muestra la configuración completa necesaria para que
# el microservicio de Vehicles funcione correctamente con IAM y Users

# 1. VARIABLES DE ENTORNO PARA DOCKER-COMPOSE
# =============================================

# En el archivo docker-compose.yml principal, Vehicles service debe tener:
environment:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:mysql://vehicles-db:3306/ecomovil_vehicles_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  SPRING_DATASOURCE_USERNAME: vehicles_user
  SPRING_DATASOURCE_PASSWORD: vehicles_password
  SERVER_PORT: 8083
  # JWT Secret debe ser EXACTAMENTE igual en IAM, Users y Vehicles
  JWT_SECRET: mySecretKeyThatIsLongEnoughForJWTHMACAlgorithmRequirements1234567890
  # URL del servicio de Users
  SERVICES_USERS_URL: http://users-service:8082

# 2. PUERTOS ASIGNADOS
# ====================
# IAM Service:     8080
# Users Service:   8082
# Vehicles Service: 8083

# 3. ENDPOINTS DE VERIFICACIÓN
# ============================

# Health checks
GET http://localhost:8080/actuator/health  # IAM
GET http://localhost:8082/actuator/health  # Users  
GET http://localhost:8083/actuator/health  # Vehicles

# Swagger UIs
http://localhost:8080/swagger-ui.html  # IAM
http://localhost:8082/swagger-ui.html  # Users
http://localhost:8083/swagger-ui.html  # Vehicles

# 4. FLUJO DE TESTING COMPLETO
# ============================

# Paso 1: Autenticar con IAM
curl -X POST http://localhost:8080/api/v1/authentication/sign-in \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Paso 2: Extraer token de la respuesta
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Paso 3: Crear profile en Users (si no existe)
curl -X POST http://localhost:8082/api/v1/profiles \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "address": "test@example.com", 
    "phoneNumber": "+51999999999",
    "rucNumber": "12345678901",
    "planId": 1
  }'

# Paso 4: Test de integración JWT en Vehicles
curl -X GET http://localhost:8083/api/v1/vehicles/test-integration \
  -H "Authorization: Bearer $JWT_TOKEN"

# Paso 5: Test de comunicación con Users
curl -X GET http://localhost:8083/api/v1/vehicles/test-users-integration \
  -H "Authorization: Bearer $JWT_TOKEN"

# Paso 6: Crear vehículo (validación completa)
curl -X POST http://localhost:8083/api/v1/vehicles \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "sedan",
    "name": "Toyota Camry 2023",
    "year": 2023,
    "review": 5,
    "priceRent": 50.0,
    "priceSell": 30000.0,
    "isAvailable": true,
    "imageUrl": "https://example.com/camry.jpg",
    "lat": -12.0464,
    "lng": -77.0428,
    "description": "Comfortable sedan"
  }'

# 5. VALIDACIONES QUE SE EJECUTAN
# ===============================

# En VehicleCommandServiceImpl.handle(CreateVehicleCommand):
# 1. Extrae userId del JWT token
# 2. Llama a Users service para validar que el usuario existe
# 3. Verifica que no exista un vehículo duplicado (mismo nombre + año)
# 4. Si todas las validaciones pasan, crea el vehículo
# 5. Si alguna falla, lanza excepción con mensaje claro

# 6. RESPUESTAS ESPERADAS
# =======================

# JWT Integration Test (exitoso):
# "✅ JWT Integration Working!
#  Username: admin
#  User ID: 1
#  Authorities: [ROLE_USER]
#  Vehicles Service is ready to create vehicles!"

# Users Integration Test (exitoso):
# "✅ Users Service Integration Working!
#  Profile ID: 1
#  Name: Test User
#  Email: test@example.com
#  Plan ID: 1
#  Communication with Users service is successful!"

# Vehicle Creation (exitoso):
# {
#   "id": 1,
#   "ownerId": 1,
#   "type": "sedan",
#   "name": "Toyota Camry 2023",
#   "year": 2023,
#   ...
# }

# Vehicle Creation (usuario no existe):
# HTTP 400: "El perfil de usuario con el ID X no existe"

# Vehicle Creation (duplicado):
# HTTP 400: "Ya tienes un vehículo con el nombre 'Toyota Camry 2023' del año 2023"

# 7. COMANDOS DE DEBUGGING
# ========================

# Ver logs de Vehicles service
docker logs ecomovil-vehicles-service

# Ver logs específicos de JWT
docker logs ecomovil-vehicles-service | grep JWT

# Ver logs de validación de usuario
docker logs ecomovil-vehicles-service | grep "Validating owner"

# Ver logs de creación de vehículo
docker logs ecomovil-vehicles-service | grep "Creating vehicle"

# 8. TROUBLESHOOTING COMÚN
# ========================

# Error: "Connection refused" al llamar Users service
# - Verificar que users-service esté corriendo
# - Verificar configuración de URL en application-docker.properties

# Error: "JWT signature validation failed"
# - Verificar que JWT secret sea idéntico en IAM, Users y Vehicles

# Error: "Profile not found"
# - Verificar que el usuario tenga un profile creado en Users service
# - Verificar que userId en JWT corresponda al profile existente

# Error: Vehicle creation fails
# - Verificar logs para ver en qué validación falla
# - Verificar conectividad con Users service
# - Verificar datos de entrada en el request

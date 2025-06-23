# 🚀 VEHICLES MICROSERVICE - GUÍA DE VERIFICACIÓN

## ✅ Verificación de Integración IAM + Users + Vehicles

### 1. **Verificar Configuración de Puertos**
```
- IAM Service:     http://localhost:8080
- Users Service:   http://localhost:8082  
- Vehicles Service: http://localhost:8083
```

### 2. **Paso 1: Iniciar Servicios**
```bash
cd carpetaoriginal/carpeta-microservicio/
docker-compose up --build
```

### 3. **Paso 2: Verificar Health Checks**
```bash
# IAM Service
curl http://localhost:8080/actuator/health

# Users Service  
curl http://localhost:8082/actuator/health

# Vehicles Service
curl http://localhost:8083/actuator/health
```

### 4. **Paso 3: Autenticación con IAM**
```bash
# Crear usuario (si no existe)
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "roles": ["ROLE_USER"]
  }'

# Autenticar y obtener JWT
curl -X POST http://localhost:8080/api/v1/authentication/sign-in \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser", 
    "password": "password123"
  }'
```

**Esperado**: Respuesta con JWT token
```json
{
  "id": 1,
  "username": "testuser",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "roles": ["ROLE_USER"]
}
```

### 5. **Paso 4: Crear Profile en Users**
```bash
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

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
```

**Esperado**: Profile creado exitosamente

### 6. **Paso 5: Test de Integración JWT en Vehicles**
```bash
curl -X GET http://localhost:8083/api/v1/vehicles/test-integration \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Esperado**: 
```
✅ JWT Integration Working!
Username: testuser
User ID: 1
Authorities: [ROLE_USER]
Vehicles Service is ready to create vehicles!
```

### 7. **Paso 6: Crear Vehículo (Validación Completa)**
```bash
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
    "description": "Comfortable sedan for city driving"
  }'
```

**Esperado**: Vehículo creado exitosamente
```json
{
  "id": 1,
  "ownerId": 1,
  "type": "sedan",
  "name": "Toyota Camry 2023",
  "year": 2023,
  "reviewValue": 5,
  "priceRent": 50.0,
  "priceSell": 30000.0,
  "isAvailable": true,
  "imageUrl": "https://example.com/camry.jpg",
  "lat": -12.0464,
  "lng": -77.0428,
  "description": "Comfortable sedan for city driving"
}
```

### 8. **Paso 7: Verificar Mis Vehículos**
```bash
curl -X GET http://localhost:8083/api/v1/vehicles/my-vehicles \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 9. **Paso 8: Probar Validaciones**

#### **Usuario Inexistente (Simulado con JWT modificado)**
Si usas un JWT con userId que no existe en Users service, debería fallar:
```
❌ Error 400: "El perfil de usuario con el ID X no existe"
```

#### **Vehículo Duplicado**
Intenta crear el mismo vehículo (mismo nombre y año):
```bash
curl -X POST http://localhost:8083/api/v1/vehicles \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "sedan",
    "name": "Toyota Camry 2023",
    "year": 2023,
    "priceRent": 55.0
  }'
```

**Esperado**: 
```
❌ Error 400: "Ya tienes un vehículo con el nombre 'Toyota Camry 2023' del año 2023"
```

## 🔍 **Verificación de Logs**

### Logs de Integración Exitosa:
```bash
# En Vehicles Service
docker logs ecomovil-vehicles-service | grep -E "(JWT|Creating vehicle|Validating owner)"
```

**Logs esperados**:
```
INFO  - JWT Token validated successfully for user: testuser, userId: 1
INFO  - Creating vehicle for userId: 1 with type: sedan  
INFO  - Validating owner profile with ID: 1
INFO  - Owner profile validation successful for ID: 1
INFO  - Vehicle created successfully with ID: 1
```

## 🚨 **Troubleshooting**

### **Error: Connection refused Users Service**
```bash
# Verificar que Users service esté corriendo
docker ps | grep users-service
curl http://localhost:8082/actuator/health
```

### **Error: JWT signature does not match**
- Verificar que todas las configuraciones JWT usen la misma clave secreta:
```
authorization.jwt.secret=mySecretKeyThatIsLongEnoughForJWTHMACAlgorithmRequirements1234567890
```

### **Error: Profile not found**
- Asegurar que el usuario tenga un profile creado en Users service
- Verificar que el userId en JWT corresponda al userId en Users

## ✅ **Integración Completada Exitosamente**

Si todos los pasos funcionan:

1. ✅ **JWT de IAM** se valida correctamente en Vehicles
2. ✅ **Usuario en JWT** se extrae y valida contra Users service  
3. ✅ **Vehículo se crea** solo si usuario existe
4. ✅ **Duplicados se previenen** correctamente
5. ✅ **Autorización** funciona por roles (USER/ADMIN)

**El microservicio de Vehicles está completamente integrado con IAM y Users siguiendo el mismo patrón que Users-Plans.** 🎉

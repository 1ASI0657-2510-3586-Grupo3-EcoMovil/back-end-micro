# Validación de Dependencias Entre Microservicios

## Patrón de Validación Implementado

### Users Microservice → Plans Microservice
Cuando se crea un **Profile** en el microservicio de Users:
1. Se valida que el **Plan** exista en el microservicio de Plans
2. Se usa `ExternalPlanService` para verificar la existencia
3. Si el plan no existe, se lanza una excepción

```java
// En ProfileCommandServiceImpl
if (command.planId() != null) {
    var plan = externalPlanService.fetchPlanById(command.planId());
    if (plan.isEmpty()) {
        throw new IllegalArgumentException("El plan con el ID " + command.planId() + " no existe");
    }
}
```

### Vehicles Microservice → Users Microservice
Cuando se crea un **Vehicle** en el microservicio de Vehicles:
1. Se valida que el **User Profile** exista en el microservicio de Users
2. Se usa `ExternalUserService` para verificar la existencia
3. Si el usuario no existe, se lanza una excepción
4. **PLUS**: Se previene la creación de vehículos duplicados (mismo nombre y año por propietario)

```java
// En VehicleCommandServiceImpl
if (command.ownerId() != null) {
    var owner = externalUserService.fetchUserProfileById(command.ownerId());
    if (owner.isEmpty()) {
        throw new IllegalArgumentException("El perfil de usuario con el ID " + command.ownerId() + " no existe");
    }
}

// Validación adicional para evitar duplicados
vehicleRepository.findByOwnerIdAndDetailsNameAndDetailsYear(
    command.ownerId(), command.name(), command.year()).ifPresent(
    existingVehicle -> {
        throw new IllegalArgumentException("Ya tienes un vehículo con el nombre '" + command.name() + 
                "' del año " + command.year() + ". Cada vehículo debe tener un nombre único por año.");
    });
```

## Flujo de Validación Completo

### Escenario: Usuario crea un vehículo

1. **Autenticación**: Usuario se autentica con IAM service
   ```bash
   POST /api/v1/authentication/sign-in
   Response: { "token": "eyJhbGciOiJIUzI1..." }
   ```

2. **Creación de vehículo**: Usuario envía request al Vehicles service
   ```bash
   POST /api/v1/vehicles
   Authorization: Bearer eyJhbGciOiJIUzI1...
   {
     "type": "sedan",
     "name": "Toyota Camry 2023",
     "year": 2023,
     "priceRent": 50.0,
     "isAvailable": true
   }
   ```

3. **Validaciones en Vehicles Service**:
   
   a) **Extracción de userId del JWT**:
   ```java
   Long userId = ((JwtUserDetails) userDetails).getUserId();
   ```
   
   b) **Validación de usuario existe**:
   ```java
   var owner = externalUserService.fetchUserProfileById(userId);
   if (owner.isEmpty()) {
       throw new IllegalArgumentException("El perfil de usuario no existe");
   }
   ```
   
   c) **Validación de vehículo no duplicado**:
   ```java
   vehicleRepository.findByOwnerIdAndDetailsNameAndDetailsYear(userId, name, year)
       .ifPresent(existing -> throw new IllegalArgumentException("Vehículo duplicado"));
   ```

4. **Resultado**: 
   - ✅ Usuario válido + vehículo único → Vehículo creado
   - ❌ Usuario inexistente → Error 400 "El perfil de usuario no existe"
   - ❌ Vehículo duplicado → Error 400 "Ya tienes un vehículo con ese nombre y año"

## Beneficios del Patrón

### 1. **Integridad Referencial**
- Los vehículos solo pueden ser creados por usuarios que existen
- Previene referencias a usuarios inexistentes (orphaned records)

### 2. **Validación Distribuida**
- Cada microservicio mantiene su responsabilidad
- Users service valida usuarios
- Vehicles service valida vehículos pero consulta a Users

### 3. **Consistencia de Datos**
- Previene estados inconsistentes en el sistema
- Validaciones en tiempo real

### 4. **Manejo de Errores Consistente**
- Mensajes de error claros y en español
- Códigos de estado HTTP apropiados
- Logging detallado para debugging

## Configuración Inter-Servicios

### application.properties (Vehicles Service)
```properties
# URL del servicio de usuarios
services.users.url=${USERS_SERVICE_URL:http://localhost:8081}

# Para Docker
services.users.url=${SERVICES_USERS_URL:http://users-service:8081}
```

### Autenticación JWT Compartida
```properties
# Misma clave secreta en todos los servicios
authorization.jwt.secret=mySecretKeyForJWTTokenValidation...
```

## Testing

El patrón incluye tests unitarios que validan:

1. **Usuario válido** → Vehículo creado exitosamente
2. **Usuario inexistente** → Excepción lanzada
3. **Vehículo duplicado** → Excepción lanzada
4. **Owner ID nulo** → Excepción lanzada

```java
@Test
void testCreateVehicle_UserNotFound_ThrowsException() {
    when(externalUserService.fetchUserProfileById(999L))
        .thenReturn(Optional.empty());
    
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> vehicleCommandService.handle(command)
    );
    
    assertEquals("El perfil de usuario con el ID 999 no existe", 
                exception.getMessage());
}
```

Este patrón asegura que el microservicio de Vehicles mantenga la integridad de datos al igual que el microservicio de Users lo hace con Plans, creando un sistema distribuido pero consistente.

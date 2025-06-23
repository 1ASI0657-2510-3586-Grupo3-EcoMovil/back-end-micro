# Vehicles Service - Microservicio de Vehículos

Este es el microservicio de gestión de vehículos de la plataforma Ecomovil.

## Arquitectura

El servicio está diseñado siguiendo los principios de Domain-Driven Design (DDD) y Clean Architecture:

```
src/main/java/upc/edu/ecomovil/microservices/vehicles/
├── VehiclesServiceApplication.java
├── application/
│   └── internal/
│       ├── commandservice/
│       │   └── VehicleCommandServiceImpl.java
│       └── queryservices/
│           └── VehicleQueryServiceImpl.java
├── domain/
│   ├── model/
│   │   ├── aggregates/
│   │   │   └── Vehicle.java
│   │   ├── commands/
│   │   │   └── CreateVehicleCommand.java
│   │   └── queries/
│   │       ├── GetAllVehiclesQuery.java
│   │       └── GetVehicleByIdQuery.java
│   └── services/
│       ├── VehicleCommandService.java
│       └── VehicleQueryService.java
├── infrastructure/
│   └── persistence/
│       └── jpa/
│           └── repositories/
│               └── VehicleRepository.java
├── interfaces/
│   └── rest/
│       ├── VehicleController.java
│       ├── resources/
│       │   ├── CreateVehicleResource.java
│       │   └── VehicleResource.java
│       └── transform/
│           ├── CreateVehicleCommandFromResourceAssembler.java
│           └── VehicleResourceFromEntityAssembler.java
└── shared/
    └── domain/
        └── model/
            └── aggregates/
                └── AuditableAbstractAggregateRoot.java
```

## Funcionalidades

- **Gestión de Vehículos**: CRUD completo de vehículos
- **Consultas**: Búsqueda por ID, obtener todos los vehículos
- **Validaciones**: Validación de datos de entrada
- **Auditoría**: Timestamps automáticos de creación y modificación
- **Seguridad**: Autenticación JWT integrada
- **Documentación**: Swagger UI automática

## Configuración

### Variables de Entorno
- `SPRING_DATASOURCE_URL`: URL de la base de datos
- `SPRING_DATASOURCE_USERNAME`: Usuario de la base de datos  
- `SPRING_DATASOURCE_PASSWORD`: Contraseña de la base de datos
- `JWT_SECRET`: Clave secreta para JWT
- `SERVICES_USERS_URL`: URL del servicio de usuarios

### Puertos
- **Desarrollo**: 8083
- **Docker**: 8083

## Ejecutar localmente

```bash
# Compilar
./mvnw clean compile

# Ejecutar
./mvnw spring-boot:run

# Ejecutar tests
./mvnw test
```

## Docker

```bash
# Construir imagen
docker build -t ecomovil-vehicles-service .

# Ejecutar contenedor
docker run -p 8083:8083 ecomovil-vehicles-service
```

## API Documentation

Una vez que el servicio esté ejecutándose, la documentación Swagger estará disponible en:
- http://localhost:8083/swagger-ui.html

## Health Check

Endpoint de salud disponible en:
- http://localhost:8083/actuator/health

## Base de datos

El servicio utiliza MySQL como base de datos:
- **Puerto local**: 3308
- **Puerto Docker**: 3306 (en contenedor vehicles-db)
- **Base de datos**: ecomovil_vehicles_db
- **Usuario**: vehicles_user

## Endpoints principales

- `GET /api/v1/vehicles` - Obtener todos los vehículos
- `GET /api/v1/vehicles/{id}` - Obtener vehículo por ID
- `POST /api/v1/vehicles` - Crear nuevo vehículo
- `PUT /api/v1/vehicles/{id}` - Actualizar vehículo
- `DELETE /api/v1/vehicles/{id}` - Eliminar vehículo

## Dependencias con otros servicios

- **Users Service**: Para validar propietarios de vehículos
- **IAM Service**: Para autenticación JWT

## Próximos pasos

1. ✅ Estructura base creada
2. 🔄 Implementar entidades de dominio
3. 🔄 Crear servicios de aplicación
4. 🔄 Implementar controladores REST
5. 🔄 Agregar validaciones
6. 🔄 Configurar comunicación con Users Service
7. 🔄 Actualizar docker-compose.yml

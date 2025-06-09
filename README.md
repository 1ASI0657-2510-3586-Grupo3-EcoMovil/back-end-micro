# Ecomovil Microservices

Sistema de microservicios para la plataforma Ecomovil con arquitectura distribuida usando Spring Boot, Docker y JWT authentication.

## 🏗️ Arquitectura

El sistema está compuesto por 3 microservicios:

- **IAM Service** (Puerto 8080) - Gestión de identidad y autenticación
- **Plans Service** (Puerto 8081) - Gestión de planes de suscripción  
- **Users Service** (Puerto 8082) - Gestión de perfiles de usuario

## 🚀 Inicio Rápido

### Prerrequisitos
- Docker Desktop instalado y corriendo
- Git

### Ejecutar el proyecto completo
```bash
# Clonar el repositorio
git clone <repository-url>
cd microservices-ecomovil/carpetaoriginal/carpeta-microservicio

# Ejecutar todos los servicios con Docker Compose
docker compose up --build
```

### URLs de acceso
Una vez que todos los servicios estén corriendo:

- **IAM Service**: http://localhost:8080/swagger-ui/index.html
- **Plans Service**: http://localhost:8081/swagger-ui/index.html  
- **Users Service**: http://localhost:8082/swagger-ui/index.html

## 📋 Guía de uso

### 1. Autenticación (IAM Service)
```bash
# Crear un usuario
POST http://localhost:8080/api/v1/authentication/sign-up
{
  "username": "daniela",
  "password": "password123",
  "roles": ["ROLE_ADMIN"]
}

# Iniciar sesión y obtener JWT token
POST http://localhost:8080/api/v1/authentication/sign-in
{
  "username": "daniela", 
  "password": "password123"
}
```

### 2. Gestionar planes (Plans Service)
```bash
# Crear un plan (requiere JWT token)
POST http://localhost:8081/api/v1/plans
Authorization: Bearer <jwt-token>
{
  "name": "Plan Básico",
  "description": "Plan básico mensual"
}
```

### 3. Gestionar perfiles (Users Service)
```bash
# Crear un perfil (requiere JWT token)
POST http://localhost:8082/api/v1/profiles
Authorization: Bearer <jwt-token>
{
  "firstName": "Daniela",
  "lastName": "García",
  "email": "daniela@example.com",
  "phoneNumber": "123456789",
  "ruc": "12345678912",
  "planId": 1
}
```

## 🔧 Desarrollo

### Estructura del proyecto
```
carpeta-microservicio/
├── docker-compose.yml          # Orquestación completa
├── iam/                        # Servicio de autenticación
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── plans/untitled/             # Servicio de planes
│   ├── Dockerfile
│   ├── pom.xml  
│   └── src/
├── users/                      # Servicio de usuarios
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── init-scripts/               # Scripts de inicialización de BD
```

### Comandos útiles

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f iam-service

# Reiniciar un servicio específico
docker compose restart users-service

# Detener todos los servicios
docker compose down

# Limpiar volúmenes y reiniciar desde cero
docker compose down -v && docker compose up --build
```

### Base de datos
- **IAM**: PostgreSQL (puerto 5433)
- **Plans**: MySQL (puerto 3306) 
- **Users**: MySQL (puerto 3307)

Las bases de datos se inicializan automáticamente con Docker Compose.

## 🔐 Autenticación JWT

Todos los endpoints (excepto sign-up/sign-in) requieren autenticación JWT:

1. Obtén un token del endpoint `/api/v1/authentication/sign-in`
2. Incluye el token en el header: `Authorization: Bearer <token>`
3. En Swagger UI, usa el botón "Authorize" para configurar el token

## ✅ Health Checks

Todos los servicios incluyen health checks:
- http://localhost:8080/actuator/health
- http://localhost:8081/actuator/health  
- http://localhost:8082/actuator/health

## 🐛 Solución de problemas

### Los servicios no inician
```bash
# Verificar que Docker Desktop esté corriendo
docker --version

# Limpiar contenedores e imágenes anteriores
docker compose down -v
docker system prune -f
docker compose up --build
```

### Error de conexión a base de datos
```bash
# Verificar que las bases de datos estén corriendo
docker compose ps

# Revisar logs de las bases de datos
docker compose logs iam-db
docker compose logs plans-db  
docker compose logs users-db
```

### Error de autenticación entre servicios
- Verificar que todos los servicios usen el mismo JWT secret
- Confirmar que los servicios puedan comunicarse por la red Docker

## 📚 Documentación adicional

- **OpenAPI/Swagger**: Disponible en `/swagger-ui/index.html` de cada servicio
- **Actuator**: Endpoints de monitoreo en `/actuator/` 
- **Logs**: Los logs se almacenan en Docker logs y se pueden ver con `docker compose logs`

## 🤝 Contribución

1. Crear una rama feature
2. Hacer los cambios necesarios
3. Probar con `docker compose up --build`
4. Crear pull request

---

**Equipo de desarrollo Ecomovil** 🚗💚

# Estrategia de Bases de Datos - Microservicios Ecomovil

## Base de Datos por Servicio

### 1. Plans Service
- **BD**: `ecomovil_plans_db`
- **Tipo**: MySQL/PostgreSQL
- **Tablas**: plans, plan_features, plan_pricing
- **Puerto**: 3306 (MySQL) / 5432 (PostgreSQL)

### 2. User Service  
- **BD**: `ecomovil_users_db`
- **Tipo**: MySQL/PostgreSQL
- **Tablas**: users, user_profiles, user_preferences
- **Puerto**: 3307 (MySQL) / 5433 (PostgreSQL)

### 3. Vehicle Service
- **BD**: `ecomovil_vehicles_db` 
- **Tipo**: MySQL/PostgreSQL
- **Tablas**: vehicles, vehicle_types, vehicle_status
- **Puerto**: 3308 (MySQL) / 5434 (PostgreSQL)

### 4. Reservations Service
- **BD**: `ecomovil_reservations_db`
- **Tipo**: MySQL/PostgreSQL  
- **Tablas**: reservations, reservation_status, payments
- **Puerto**: 3309 (MySQL) / 5435 (PostgreSQL)

### 5. Forum Service
- **BD**: `ecomovil_forum_db`
- **Tipo**: MongoDB (mejor para contenido dinámico)
- **Colecciones**: posts, comments, categories, users_activity
- **Puerto**: 27017

### 6. IAM Service
- **BD**: `ecomovil_iam_db`
- **Tipo**: PostgreSQL (mejor para seguridad)
- **Tablas**: users, roles, permissions, tokens, audit_logs
- **Puerto**: 5436

## Configuración AWS RDS

### Opción 1: Instancias Separadas (Recomendado para Producción)
```
- RDS Instance 1: Plans + Users (MySQL Multi-AZ)
- RDS Instance 2: Vehicles + Reservations (MySQL Multi-AZ) 
- RDS Instance 3: IAM (PostgreSQL Multi-AZ)
- DocumentDB: Forum (MongoDB compatible)
```

### Opción 2: Multi-Database en pocas instancias (Desarrollo/Staging)
```
- RDS MySQL: plans_db, users_db, vehicles_db, reservations_db
- RDS PostgreSQL: iam_db
- DocumentDB: forum_db
```

## Comunicación Entre Servicios

### Datos Compartidos
- **NO compartir tablas directamente**
- **SÍ usar eventos/mensajes** (AWS SNS/SQS)
- **SÍ usar APIs REST** entre servicios
- **SÍ mantener copias desnormalizadas** cuando sea necesario

### Ejemplo: Usuario hace reserva
1. Reservation Service consulta User Service (API REST)
2. Reservation Service consulta Vehicle Service (API REST)  
3. Reservation Service crea reserva en su BD
4. Reservation Service publica evento "ReservationCreated" (SNS)
5. User Service y Vehicle Service escuchan evento y actualizan sus datos

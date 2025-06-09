#!/bin/bash

echo "🧹 Limpiando contenedores existentes..."
docker rm -f ecomovil-plans-db ecomovil-plans-service 2>/dev/null || true

echo "🚀 Iniciando servicios con Docker Compose..."
cd /Users/davidgallo/Documents/GitHub/microservices-ecomovil/carpetaoriginal/carpeta-microservicio

# Ejecutar solo la base de datos primero
docker compose up plans-db -d

echo "⏳ Esperando que MySQL esté listo..."
sleep 20

# Ejecutar el servicio
docker compose up plans-service -d

echo "✅ Servicios iniciados!"
echo ""
echo "📊 URLs disponibles:"
echo "🔗 Swagger UI: http://localhost:8081/swagger-ui.html"
echo "🔗 Health Check: http://localhost:8081/actuator/health"
echo "🔗 API Docs: http://localhost:8081/api-docs"
echo "🔗 Plans API: http://localhost:8081/api/v1/plans"
echo ""
echo "📋 Verificar estado:"
echo "docker compose ps"
echo ""
echo "📝 Ver logs:"
echo "docker compose logs plans-service -f"

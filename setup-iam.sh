#!/bin/bash

# Ecomovil IAM Service - Setup Script
echo "🚀 Configurando servicio IAM de Ecomovil..."

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker no está corriendo. Por favor inicia Docker Desktop."
    exit 1
fi

# Cambiar al directorio del proyecto
cd /Users/davidgallo/Documents/GitHub/microservices-ecomovil/carpetaoriginal/carpeta-microservicio

# Iniciar base de datos PostgreSQL para IAM
echo "📦 Iniciando base de datos PostgreSQL para IAM..."
docker compose up -d iam-db

# Esperar a que la base de datos esté lista
echo "⏳ Esperando a que PostgreSQL esté listo..."
sleep 10

# Verificar que la base de datos esté corriendo
if docker compose ps iam-db | grep -q "Up"; then
    echo "✅ Base de datos IAM corriendo en puerto 5433"
else
    echo "❌ Error iniciando la base de datos IAM"
    exit 1
fi

# Mostrar información de conexión
echo ""
echo "📋 Información de la base de datos:"
echo "   Host: localhost"
echo "   Puerto: 5433"
echo "   Base de datos: ecomovil_iam_db"
echo "   Usuario: iam_user"
echo "   Contraseña: iam_password"
echo ""

echo "🎯 Para ejecutar el servicio IAM:"
echo "1. Abre IntelliJ IDEA"
echo "2. Importa el proyecto desde: $(pwd)/iam"
echo "3. Ejecuta la clase principal: IAMServiceApplication"
echo "4. El servicio estará disponible en: http://localhost:8080"
echo "5. Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""

echo "✅ Setup completado! El servicio IAM está listo para ejecutar en IntelliJ."

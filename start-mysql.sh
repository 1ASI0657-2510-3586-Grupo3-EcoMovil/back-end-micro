#!/bin/bash

echo "🐳 MySQL para Plans Service - Puerto 3306"
echo "==========================================="

# Verificar si el contenedor ya existe
if [ "$(docker ps -q -f name=ecomovil-plans-db)" ]; then
    echo "✅ MySQL ya está corriendo en puerto 3306"
    echo "🔗 Conexión: localhost:3306"
    echo "📊 Base de datos: ecomovil_plans_db"
    echo "👤 Usuario: plans_user"
    echo "🔑 Contraseña: plans_password"
else
    # Verificar si el contenedor existe pero está detenido
    if [ "$(docker ps -aq -f status=exited -f name=ecomovil-plans-db)" ]; then
        echo "🔄 Iniciando contenedor existente..."
        docker start ecomovil-plans-db
    else
        echo "🆕 Creando nuevo contenedor MySQL..."
        docker run -d \
            --name ecomovil-plans-db \
            -e MYSQL_ROOT_PASSWORD=rootpassword \
            -e MYSQL_DATABASE=ecomovil_plans_db \
            -e MYSQL_USER=plans_user \
            -e MYSQL_PASSWORD=plans_password \
            -p 3306:3306 \
            mysql:8.0
    fi
    
    echo "⏳ Esperando que MySQL esté listo..."
    sleep 10
    
    echo "✅ MySQL corriendo en puerto 3306"
fi

echo ""
echo "📋 Comandos útiles:"
echo "  Detener: docker stop ecomovil-plans-db"
echo "  Iniciar: docker start ecomovil-plans-db"
echo "  Logs:    docker logs ecomovil-plans-db -f"
echo "  Acceso:  docker exec -it ecomovil-plans-db mysql -u plans_user -p"
echo ""
echo "🚀 Ahora ejecuta PlansServiceApplication.java en IntelliJ"
echo "🔗 Swagger: http://localhost:8081/swagger-ui.html"

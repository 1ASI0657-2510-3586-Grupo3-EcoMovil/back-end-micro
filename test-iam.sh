#!/bin/bash

# Test script para el servicio IAM
IAM_BASE_URL="http://localhost:8080/api/v1"

echo "🧪 Testing IAM Service..."
echo "Base URL: $IAM_BASE_URL"
echo ""

# Test 1: Sign Up Admin User
echo "1️⃣ Registrando usuario administrador..."
SIGNUP_RESPONSE=$(curl -s -X POST "$IAM_BASE_URL/authentication/sign-up" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "roles": ["ROLE_ADMIN"]
  }')

echo "Response: $SIGNUP_RESPONSE"
echo ""

# Test 2: Sign Up Regular User
echo "2️⃣ Registrando usuario regular..."
curl -s -X POST "$IAM_BASE_URL/authentication/sign-up" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123",
    "roles": ["ROLE_USER"]
  }' | jq '.' || echo "Response: $?"
echo ""

# Test 3: Sign In Admin
echo "3️⃣ Iniciando sesión como admin..."
SIGNIN_RESPONSE=$(curl -s -X POST "$IAM_BASE_URL/authentication/sign-in" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "Response: $SIGNIN_RESPONSE"

# Extract token (if jq is available)
if command -v jq &> /dev/null; then
    TOKEN=$(echo $SIGNIN_RESPONSE | jq -r '.token')
    echo "Token extraído: $TOKEN"
    echo ""
    
    # Test 4: Get All Users (Admin only)
    echo "4️⃣ Obteniendo todos los usuarios (requiere admin)..."
    curl -s -X GET "$IAM_BASE_URL/users" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" | jq '.'
    echo ""
    
    # Test 5: Get User by Username
    echo "5️⃣ Obteniendo usuario por username..."
    curl -s -X GET "$IAM_BASE_URL/users/username/admin" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" | jq '.'
else
    echo "💡 Instala 'jq' para mejor formato de respuestas: brew install jq"
fi

echo ""
echo "✅ Tests completados!"
echo "💡 Para más tests, visita Swagger UI: http://localhost:8080/swagger-ui.html"

#!/bin/bash

# Test crear perfil con plan inexistente usando token de raulito
curl -X POST http://localhost:8082/api/v1/profiles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyYXVsaXRvIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJ1c2VySWQiOjE4LCJpYXQiOjE3NDk0ODg1NDgsImV4cCI6MTc1MDA5MzM0OH0.JuNG8Dj1pdQhjeQXgXD1UOjO303sv64RCttfbWEZDXxnH9aJBIZ3lQPYVeDQLaM-yl10t_A3739xGxTmEmPKDA" \
  -d '{
    "firstName": "Raul", 
    "lastName": "Testeo", 
    "email": "raul@test.com", 
    "phone": "987654321", 
    "documentNumber": "98765432101", 
    "planId": 999
  }'

# EcoMovil — Backend Microservices

Spring Boot microservices for EcoMovil, a marketplace for renting/selling eco-friendly vehicles (bikes, scooters, skates) for students in Lima, Peru.

## Services

| Service | Port | Responsibility |
|---|---|---|
| `iam` | 8080 | Authentication, JWT issuance |
| `users` | 8082 | User profiles |
| `plans` | 8081 | Publication plans |
| `vehicles` | 8083 | Vehicle listings, image upload, sales chatbot (Bedrock) |
| `reservations` | 8084 | Rental/sale reservations |

## Local development

```bash
docker compose up --build
```

Each service has its own `Dockerfile` and `application.properties` (local), `application-docker.properties` (compose), and `application-aws.properties` (production — values injected by ECS from Secrets Manager, never hardcoded).

## Production deployment

- **Infrastructure**: AWS (ECS Fargate, ALB, CloudFront, RDS) managed via Terraform in `../../infrastructure/terraform/`.
- **CI/CD**: GitHub Actions (`.github/workflows/deploy.yml`) builds an ARM64 image and redeploys on push to `main`, only for the service whose directory changed.
- Full operational details (security groups, secrets, AWS SDK version pinning, the Bedrock chatbot architecture) are documented in `../../CLAUDE.md`.

To bring the AWS stack up or down:
```bash
cd ../../infrastructure/terraform
terraform apply    # bring everything up
terraform destroy  # tear down to save cost
```

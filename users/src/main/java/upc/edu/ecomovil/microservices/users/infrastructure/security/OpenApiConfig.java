package upc.edu.ecomovil.microservices.users.infrastructure.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Swagger Documentation
 * Configures JWT Bearer authentication for API documentation
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Users Microservice API")
                                                .description("API for managing user profiles with JWT authentication")
                                                .version("1.0.0"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("JWT token for authentication")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}

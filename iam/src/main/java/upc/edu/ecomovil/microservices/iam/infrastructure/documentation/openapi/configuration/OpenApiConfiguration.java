package upc.edu.ecomovil.microservices.iam.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for IAM Service
 * <p>
 * This configuration sets up Swagger UI with Bearer token authentication
 * support.
 * Users will be able to see the "Authorize" button (lock icon) to input JWT
 * tokens.
 * </p>
 */
@Configuration
public class OpenApiConfiguration {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI iamServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecomovil IAM Service API")
                        .description("Identity and Access Management microservice for Ecomovil platform. " +
                                "Handles user authentication, authorization, and role-based access control.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ecomovil Development Team")
                                .email("dev@ecomovil.com")
                                .url("https://github.com/ecomovil"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token **_only_** (without 'Bearer ' prefix)")));
    }
}

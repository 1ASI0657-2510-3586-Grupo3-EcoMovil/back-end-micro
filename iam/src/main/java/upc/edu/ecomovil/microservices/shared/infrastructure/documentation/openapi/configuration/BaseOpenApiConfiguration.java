package upc.edu.ecomovil.microservices.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;

/**
 * Base OpenAPI Configuration for Ecomovil Microservices
 * <p>
 * This is a base configuration that can be extended by other microservices
 * to provide consistent Swagger UI with Bearer token authentication support.
 * </p>
 */
public abstract class BaseOpenApiConfiguration {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Creates a base OpenAPI configuration with JWT Bearer authentication.
     * 
     * @param title       the API title
     * @param description the API description
     * @param version     the API version
     * @return the configured OpenAPI instance
     */
    protected OpenAPI createBaseOpenAPI(String title, String description, String version) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
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

package upc.edu.ecomovil.microservices.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * IAM Service Application
 * <p>
 * This is the main application class for the Identity and Access Management
 * (IAM) microservice.
 * This microservice handles authentication, authorization, user management, and
 * role-based access control.
 * </p>
 * 
 * @author Ecomovil Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class IAMServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IAMServiceApplication.class, args);
    }
}

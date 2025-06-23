package upc.edu.ecomovil.microservices.vehicles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Vehicles Service Application
 * <p>
 * This is the main application class for the Vehicles microservice.
 * This microservice handles vehicle management including creation,
 * updates, queries, and vehicle-related operations.
 * </p>
 *
 * @author Ecomovil Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class VehiclesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehiclesServiceApplication.class, args);
    }
}

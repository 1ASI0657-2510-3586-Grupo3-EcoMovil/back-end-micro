package upc.edu.ecomovil.microservices.vehicles;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the Vehicles Service Application
 */
@SpringBootTest
@ActiveProfiles("test")
class VehiclesServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads correctly
        // with all beans properly configured
    }

}

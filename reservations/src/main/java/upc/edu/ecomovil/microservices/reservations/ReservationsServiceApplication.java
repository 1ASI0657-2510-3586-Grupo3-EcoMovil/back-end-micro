package upc.edu.ecomovil.microservices.reservations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ReservationsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationsServiceApplication.class, args);
    }

}

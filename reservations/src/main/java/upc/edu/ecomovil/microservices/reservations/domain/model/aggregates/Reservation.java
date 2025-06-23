package upc.edu.ecomovil.microservices.reservations.domain.model.aggregates;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import upc.edu.ecomovil.microservices.reservations.domain.model.commands.CreateReservationCommand;
import upc.edu.ecomovil.microservices.reservations.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation extends AuditableAbstractAggregateRoot<Reservation> {

    private String status; // pending, accepted, rejected, cancelled, deleted
    private Long vehicleId; // Reference to vehicle in vehicles microservice
    private Long userId; // Reference to user profile in users microservice

    // Additional reservation details
    private String startDate;
    private String endDate;
    private Double totalPrice;
    private String reservationType; // rent, sell
    private String notes;

    public Reservation() {
    }

    public Reservation(String status, Long vehicleId, Long userId, String startDate, String endDate,
            Double totalPrice, String reservationType, String notes) {
        this.status = status;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.reservationType = reservationType;
        this.notes = notes;
    }

    public Reservation(CreateReservationCommand command) {
        this.status = command.status();
        this.vehicleId = command.vehicleId();
        this.userId = command.userId();
        this.startDate = command.startDate();
        this.endDate = command.endDate();
        this.totalPrice = command.totalPrice();
        this.reservationType = command.reservationType();
        this.notes = command.notes();
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateDetails(String startDate, String endDate, Double totalPrice, String notes) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.notes = notes;
    }
}

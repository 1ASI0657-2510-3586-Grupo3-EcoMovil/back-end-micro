package upc.edu.ecomovil.microservices.reservations.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.reservations.application.internal.outboundservices.acl.ExternalUserService;
import upc.edu.ecomovil.microservices.reservations.application.internal.outboundservices.acl.ExternalVehicleService;
import upc.edu.ecomovil.microservices.reservations.domain.model.aggregates.Reservation;
import upc.edu.ecomovil.microservices.reservations.domain.model.commands.CreateReservationCommand;
import upc.edu.ecomovil.microservices.reservations.domain.model.commands.UpdateReservationStatusCommand;
import upc.edu.ecomovil.microservices.reservations.domain.services.ReservationCommandService;
import upc.edu.ecomovil.microservices.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;

import java.util.Optional;

@Service
@Slf4j
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ExternalUserService externalUserService;
    private final ExternalVehicleService externalVehicleService;

    public ReservationCommandServiceImpl(ReservationRepository reservationRepository,
            ExternalUserService externalUserService,
            ExternalVehicleService externalVehicleService) {
        this.reservationRepository = reservationRepository;
        this.externalUserService = externalUserService;
        this.externalVehicleService = externalVehicleService;
    }

    @Override
    public Optional<Reservation> handle(CreateReservationCommand command) {
        try {
            log.info("Creating reservation for user {} and vehicle {}", command.userId(), command.vehicleId());

            // Validate user exists
            var userProfile = externalUserService.fetchUserProfileById(command.userId());
            if (userProfile.isEmpty()) {
                log.warn("User with ID {} not found", command.userId());
                return Optional.empty();
            }

            // Validate vehicle exists
            var vehicle = externalVehicleService.fetchVehicleById(command.vehicleId());
            if (vehicle.isEmpty()) {
                log.warn("Vehicle with ID {} not found", command.vehicleId());
                return Optional.empty();
            }

            var reservation = new Reservation(command);
            var savedReservation = reservationRepository.save(reservation);

            log.info("Reservation created successfully with ID: {}", savedReservation.getId());
            return Optional.of(savedReservation);

        } catch (Exception e) {
            log.error("Error creating reservation: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> handle(UpdateReservationStatusCommand command) {
        try {
            log.info("Updating reservation {} to status {}", command.reservationId(), command.status());

            var reservationOptional = reservationRepository.findById(command.reservationId());
            if (reservationOptional.isEmpty()) {
                log.warn("Reservation with ID {} not found", command.reservationId());
                return Optional.empty();
            }

            var reservation = reservationOptional.get();
            reservation.updateStatus(command.status());
            var savedReservation = reservationRepository.save(reservation);

            log.info("Reservation {} status updated to {}", command.reservationId(), command.status());
            return Optional.of(savedReservation);

        } catch (Exception e) {
            log.error("Error updating reservation status: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

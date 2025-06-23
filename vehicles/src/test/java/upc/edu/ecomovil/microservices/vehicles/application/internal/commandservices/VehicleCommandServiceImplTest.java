package upc.edu.ecomovil.microservices.vehicles.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import upc.edu.ecomovil.microservices.vehicles.application.internal.outboundservices.acl.ExternalUserService;
import upc.edu.ecomovil.microservices.vehicles.application.internal.outboundservices.acl.ExternalUserService.UserProfileDto;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.persistence.jpa.repositories.VehicleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleCommandServiceImpl
 * Tests the business logic including user validation
 */
class VehicleCommandServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ExternalUserService externalUserService;

    @InjectMocks
    private VehicleCommandServiceImpl vehicleCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVehicle_Success() {
        // Given
        CreateVehicleCommand command = new CreateVehicleCommand(
                "sedan", "Toyota Camry", 2023, 5, 50.0, 30000.0,
                true, "https://example.com/car.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        UserProfileDto userProfile = new UserProfileDto(1L, "John", "Doe",
                "john@example.com", "+51999999999", "12345678901", 1L);

        Vehicle savedVehicle = new Vehicle(command);

        // Mock external user service to return valid user
        when(externalUserService.fetchUserProfileById(1L)).thenReturn(Optional.of(userProfile));
        // Mock repository to return no duplicate vehicle
        when(vehicleRepository.findByOwnerIdAndDetailsNameAndDetailsYear(1L, "Toyota Camry", 2023))
                .thenReturn(Optional.empty());
        // Mock repository save
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        // When
        Optional<Vehicle> result = vehicleCommandService.handle(command);

        // Then
        assertTrue(result.isPresent());
        assertEquals("sedan", result.get().getType());
        assertEquals("Toyota Camry", result.get().getName());
        assertEquals(2023, result.get().getYear());
        assertEquals(1L, result.get().getOwnerId());

        // Verify interactions
        verify(externalUserService).fetchUserProfileById(1L);
        verify(vehicleRepository).findByOwnerIdAndDetailsNameAndDetailsYear(1L, "Toyota Camry", 2023);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void testCreateVehicle_UserNotFound_ThrowsException() {
        // Given
        CreateVehicleCommand command = new CreateVehicleCommand(
                "sedan", "Toyota Camry", 2023, 5, 50.0, 30000.0,
                true, "https://example.com/car.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 999L);

        // Mock external user service to return empty (user not found)
        when(externalUserService.fetchUserProfileById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleCommandService.handle(command));

        assertEquals("El perfil de usuario con el ID 999 no existe", exception.getMessage());

        // Verify interactions
        verify(externalUserService).fetchUserProfileById(999L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testCreateVehicle_DuplicateVehicle_ThrowsException() {
        // Given
        CreateVehicleCommand command = new CreateVehicleCommand(
                "sedan", "Toyota Camry", 2023, 5, 50.0, 30000.0,
                true, "https://example.com/car.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        UserProfileDto userProfile = new UserProfileDto(1L, "John", "Doe",
                "john@example.com", "+51999999999", "12345678901", 1L);

        Vehicle existingVehicle = new Vehicle(command);

        // Mock external user service to return valid user
        when(externalUserService.fetchUserProfileById(1L)).thenReturn(Optional.of(userProfile));
        // Mock repository to return existing duplicate vehicle
        when(vehicleRepository.findByOwnerIdAndDetailsNameAndDetailsYear(1L, "Toyota Camry", 2023))
                .thenReturn(Optional.of(existingVehicle));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleCommandService.handle(command));

        assertTrue(exception.getMessage().contains("Ya tienes un vehículo con el nombre 'Toyota Camry' del año 2023"));

        // Verify interactions
        verify(externalUserService).fetchUserProfileById(1L);
        verify(vehicleRepository).findByOwnerIdAndDetailsNameAndDetailsYear(1L, "Toyota Camry", 2023);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testCreateVehicle_NullOwnerId_ThrowsException() {
        // Given
        CreateVehicleCommand command = new CreateVehicleCommand(
                "sedan", "Toyota Camry", 2023, 5, 50.0, 30000.0,
                true, "https://example.com/car.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleCommandService.handle(command));

        assertEquals("El ID del propietario es requerido para crear un vehículo", exception.getMessage());

        // Verify no external calls were made
        verify(externalUserService, never()).fetchUserProfileById(anyLong());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}

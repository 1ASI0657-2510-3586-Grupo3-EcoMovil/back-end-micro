package upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Vehicle entity
 */
class VehicleTest {

    @Test
    void testVehicleCreationWithCommand() {
        // Given
        CreateVehicleCommand command = new CreateVehicleCommand(
                "sedan",
                "Toyota Camry",
                2022,
                5,
                50.0,
                25000.0,
                true,
                "https://example.com/image.jpg",
                -12.0464f,
                -77.0428f,
                "Comfortable sedan for city driving",
                1L);

        // When
        Vehicle vehicle = new Vehicle(command);

        // Then
        assertNotNull(vehicle);
        assertEquals("sedan", vehicle.getType());
        assertEquals("Toyota Camry", vehicle.getName());
        assertEquals(2022, vehicle.getYear());
        assertEquals(5, vehicle.getReviewValue());
        assertEquals(50.0, vehicle.getPriceRent());
        assertEquals(25000.0, vehicle.getPriceSell());
        assertTrue(vehicle.getIsAvailable());
        assertEquals("https://example.com/image.jpg", vehicle.getImageUrl());
        assertEquals(-12.0464f, vehicle.getLat());
        assertEquals(-77.0428f, vehicle.getLng());
        assertEquals("Comfortable sedan for city driving", vehicle.getDescription());
        assertEquals(1L, vehicle.getOwnerId());
    }

    @Test
    void testVehicleDetailsUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
                "sedan", "Toyota Camry", 2022, 5, 50.0, 25000.0,
                true, "https://example.com/image.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        // When
        vehicle.updateDetails("suv", "Toyota RAV4", 2023);

        // Then
        assertEquals("suv", vehicle.getType());
        assertEquals("Toyota RAV4", vehicle.getName());
        assertEquals(2023, vehicle.getYear());
    }

    @Test
    void testVehiclePricesUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
                "sedan", "Toyota Camry", 2022, 5, 50.0, 25000.0,
                true, "https://example.com/image.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        // When
        vehicle.updatePrices(75.0, 30000.0);

        // Then
        assertEquals(75.0, vehicle.getPriceRent());
        assertEquals(30000.0, vehicle.getPriceSell());
    }

    @Test
    void testVehicleAvailabilityUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
                "sedan", "Toyota Camry", 2022, 5, 50.0, 25000.0,
                true, "https://example.com/image.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        // When
        vehicle.updateAvailability(false);

        // Then
        assertFalse(vehicle.getIsAvailable());
    }

    @Test
    void testVehicleLocationUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
                "sedan", "Toyota Camry", 2022, 5, 50.0, 25000.0,
                true, "https://example.com/image.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        // When
        vehicle.updateLocation(-12.1000f, -77.1000f);

        // Then
        assertEquals(-12.1000f, vehicle.getLat());
        assertEquals(-77.1000f, vehicle.getLng());
    }

    @Test
    void testVehicleDescriptionUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
                "sedan", "Toyota Camry", 2022, 5, 50.0, 25000.0,
                true, "https://example.com/image.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        // When
        vehicle.updateDescription("Updated description for the vehicle");

        // Then
        assertEquals("Updated description for the vehicle", vehicle.getDescription());
    }

    @Test
    void testVehicleReviewUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
                "sedan", "Toyota Camry", 2022, 5, 50.0, 25000.0,
                true, "https://example.com/image.jpg", -12.0464f, -77.0428f,
                "Comfortable sedan", 1L);

        // When
        vehicle.updateReview(4);

        // Then
        assertEquals(4, vehicle.getReviewValue());
    }
}

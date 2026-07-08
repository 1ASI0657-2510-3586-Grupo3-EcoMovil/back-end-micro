package upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects.Details;
import upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects.Prices;
import upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects.Review;
import upc.edu.ecomovil.microservices.vehicles.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Instant;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle> {

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Embedded
    private Details details;

    @Embedded
    private Review review;

    @Embedded
    private Prices prices;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "latitude")
    private Float lat;

    @Column(name = "longitude")
    private Float lng;

    @Column(name = "description", length = 1000)
    private String description;

    // IoT fields — populated by the ESP32 via Lambda bridge
    @Column(name = "iot_device_id")
    private String iotDeviceId;

    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Column(name = "fall_detected")
    private Boolean fallDetected = false;

    @Column(name = "panic_active")
    private Boolean panicActive = false;

    @Column(name = "speed_kmh")
    private Float speedKmh = 0f;

    @Column(name = "last_iot_update")
    private Instant lastIotUpdate;

    // Geofence fields
    @Column(name = "geofence_center_lat")
    private Float geofenceCenterLat;

    @Column(name = "geofence_center_lng")
    private Float geofenceCenterLng;

    @Column(name = "geofence_radius_m")
    private Integer geofenceRadiusM;

    @Column(name = "geofence_breached")
    private Boolean geofenceBreached = false;

    public Vehicle(String type, String name, Integer year, Integer review, Double priceRent, Double priceSell,
            Boolean isAvailable, String imageUrl, Float lat, Float lng, String description, Long ownerId) {
        this.details = new Details(type, name, year);
        this.review = new Review(review);
        this.prices = new Prices(priceRent, priceSell);
        this.isAvailable = isAvailable;
        this.imageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.ownerId = ownerId;
    }

    public Vehicle(CreateVehicleCommand command) {
        this.details = new Details(command.type(), command.name(), command.year());
        this.review = new Review(command.review());
        this.prices = new Prices(command.priceRent(), command.priceSell());
        this.isAvailable = command.isAvailable();
        this.imageUrl = command.imageUrl();
        this.lat = command.lat();
        this.lng = command.lng();
        this.description = command.description();
        this.ownerId = command.ownerId();
    }

    // Business methods
    public void updateDetails(String type, String name, Integer year) {
        this.details = new Details(type, name, year);
    }

    public void updateReview(Integer review) {
        this.review = new Review(review);
    }

    public void updatePrices(Double priceRent, Double priceSell) {
        this.prices = new Prices(priceRent, priceSell);
    }

    public void updateAvailability(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateLocation(Float lat, Float lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateIoTTelemetry(String iotDeviceId, Float lat, Float lng,
                                   Boolean fallDetected, Boolean isLocked,
                                   Float speedKmh, Boolean panicActive) {
        this.iotDeviceId   = iotDeviceId;
        if (lat != null && lng != null) {
            this.lat = lat;
            this.lng = lng;
            this.geofenceBreached = computeGeofenceBreach(lat, lng);
        }
        this.fallDetected  = fallDetected;
        this.isLocked      = isLocked;
        this.speedKmh      = speedKmh != null ? speedKmh : 0f;
        if (Boolean.TRUE.equals(panicActive)) this.panicActive = true;
        this.lastIotUpdate = Instant.now();
    }

    public void setGeofence(Float centerLat, Float centerLng, Integer radiusM) {
        this.geofenceCenterLat = centerLat;
        this.geofenceCenterLng = centerLng;
        this.geofenceRadiusM   = radiusM;
        if (this.lat != null && this.lng != null) {
            this.geofenceBreached = computeGeofenceBreach(this.lat, this.lng);
        }
    }

    public void resetAlerts() {
        this.fallDetected   = false;
        this.panicActive    = false;
        this.geofenceBreached = false;
    }

    public void setLocked(Boolean locked) {
        this.isLocked      = locked;
        if (!Boolean.TRUE.equals(locked)) resetAlerts();
        this.lastIotUpdate = Instant.now();
    }

    private boolean computeGeofenceBreach(float lat, float lng) {
        if (geofenceCenterLat == null || geofenceCenterLng == null || geofenceRadiusM == null) return false;
        double R = 6371000.0;
        double dLat = Math.toRadians(lat - geofenceCenterLat);
        double dLng = Math.toRadians(lng - geofenceCenterLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(geofenceCenterLat)) * Math.cos(Math.toRadians(lat))
                   * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double dist = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return dist > geofenceRadiusM;
    }

    // Getters for embedded value objects
    public String getType() {
        return details != null ? details.getType() : null;
    }

    public String getName() {
        return details != null ? details.getName() : null;
    }

    public Integer getYear() {
        return details != null ? details.getYear() : null;
    }

    public Integer getReviewValue() {
        return review != null ? review.getReview() : null;
    }

    public Double getPriceRent() {
        return prices != null ? prices.getPriceRent() : null;
    }

    public Double getPriceSell() {
        return prices != null ? prices.getPriceSell() : null;
    }
}

package upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects.Details;
import upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects.Prices;
import upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects.Review;
import upc.edu.ecomovil.microservices.vehicles.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

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

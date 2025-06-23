package upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Prices(Double priceRent, Double priceSell) {
    public Prices() {
        this(null, null);
    }

    public Prices {
        if (priceRent == null) {
            throw new IllegalArgumentException("Rent price cannot be null");
        }
        if (priceSell == null) {
            throw new IllegalArgumentException("Sell price cannot be null");
        }
        if (priceRent < 0) {
            throw new IllegalArgumentException("Rent price cannot be negative");
        }
        if (priceSell < 0) {
            throw new IllegalArgumentException("Sell price cannot be negative");
        }
    }

    public Double getPriceRent() {
        return priceRent;
    }

    public Double getPriceSell() {
        return priceSell;
    }
}

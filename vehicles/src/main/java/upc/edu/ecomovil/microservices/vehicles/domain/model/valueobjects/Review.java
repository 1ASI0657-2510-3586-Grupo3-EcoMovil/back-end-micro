package upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Review(Integer review) {
    public Review() {
        this(null);
    }

    public Review {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        if (review < 0 || review > 5) {
            throw new IllegalArgumentException("Review must be between 0 and 5");
        }
    }

    public Integer getReview() {
        return review;
    }
}

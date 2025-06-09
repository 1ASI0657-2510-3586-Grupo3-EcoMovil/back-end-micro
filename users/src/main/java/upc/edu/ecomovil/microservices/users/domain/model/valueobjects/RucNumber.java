package upc.edu.ecomovil.microservices.users.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record RucNumber(String rucNumber) {
    public RucNumber() { this(null); }
    public RucNumber {
        if (rucNumber == null || rucNumber.isBlank()) {
            throw new IllegalArgumentException("RUC number cannot be null or blank");
        }
        if (rucNumber.length() != 11) {
            throw new IllegalArgumentException("RUC number must have 11 digits");
        }
    }
    public String getRucNumber() {
        return rucNumber;
    }
}

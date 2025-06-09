package upc.edu.ecomovil.microservices.users.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PhoneNumber(String phoneNumber) {
    public PhoneNumber() { this(null); }

    public PhoneNumber {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (phoneNumber.length() != 9) {
            throw new IllegalArgumentException("Phone number must have 9 digits");
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

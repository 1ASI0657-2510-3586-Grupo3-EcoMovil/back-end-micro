package upc.edu.ecomovil.microservices.vehicles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Details(String type, String name, Integer year) {
    public Details() {
        this(null, null, null);
    }

    public Details {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (year == null) {
            throw new IllegalArgumentException("Year cannot be null");
        }
        if (year < 1900 || year > 2030) {
            throw new IllegalArgumentException("Year must be between 1900 and 2030");
        }
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Integer getYear() {
        return year;
    }
}

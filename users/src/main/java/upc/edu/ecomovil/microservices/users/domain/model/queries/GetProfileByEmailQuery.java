package upc.edu.ecomovil.microservices.users.domain.model.queries;

import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.EmailAddress;

public record GetProfileByEmailQuery(EmailAddress email) {
}

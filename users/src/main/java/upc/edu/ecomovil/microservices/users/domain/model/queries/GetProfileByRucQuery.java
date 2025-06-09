package upc.edu.ecomovil.microservices.users.domain.model.queries;

import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.RucNumber;

public record GetProfileByRucQuery(RucNumber ruc) {
}

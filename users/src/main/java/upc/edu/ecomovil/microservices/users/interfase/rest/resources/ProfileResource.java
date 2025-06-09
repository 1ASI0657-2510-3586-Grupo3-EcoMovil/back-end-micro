package upc.edu.ecomovil.microservices.users.interfase.rest.resources;

public record ProfileResource(Long id, String fullName, String email, String phoneNumber, String ruc, Long planId) {
}

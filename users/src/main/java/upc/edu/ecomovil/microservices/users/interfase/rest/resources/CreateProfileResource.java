package upc.edu.ecomovil.microservices.users.interfase.rest.resources;

public record CreateProfileResource(String firstName, String lastName, String email, String phoneNumber, String ruc, Long planId) {
}

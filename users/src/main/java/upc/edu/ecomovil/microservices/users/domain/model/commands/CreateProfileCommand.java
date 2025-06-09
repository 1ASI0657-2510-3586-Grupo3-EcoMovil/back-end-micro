package upc.edu.ecomovil.microservices.users.domain.model.commands;

public record CreateProfileCommand(Long userId, String firstName, String lastName, String email, String phoneNumber,
        String rucNumber, Long planId) {

}

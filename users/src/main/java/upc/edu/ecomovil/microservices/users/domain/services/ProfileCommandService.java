package upc.edu.ecomovil.microservices.users.domain.services;

import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.domain.model.commands.CreateProfileCommand;

import java.util.Optional;

public interface ProfileCommandService {
    Optional<Profile> handle(CreateProfileCommand command);
}

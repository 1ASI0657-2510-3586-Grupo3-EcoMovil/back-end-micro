package upc.edu.ecomovil.microservices.users.interfase.rest.transform;

import upc.edu.ecomovil.microservices.users.domain.model.commands.CreateProfileCommand;
import upc.edu.ecomovil.microservices.users.interfase.rest.resources.CreateProfileResource;

public class CreateProfileCommandFromResourceAssembler {
    public static CreateProfileCommand toCommandFromResource(CreateProfileResource resource, Long userId) {
        return new CreateProfileCommand(userId, resource.firstName(), resource.lastName(), resource.email(),
                resource.phoneNumber(), resource.ruc(), resource.planId());
    }
}

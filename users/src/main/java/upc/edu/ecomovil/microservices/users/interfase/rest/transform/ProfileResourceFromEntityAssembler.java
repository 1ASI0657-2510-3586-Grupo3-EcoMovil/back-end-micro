package upc.edu.ecomovil.microservices.users.interfase.rest.transform;

import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.interfase.rest.resources.ProfileResource;

public class ProfileResourceFromEntityAssembler {
    public static ProfileResource toResourceFromEntity(Profile entity) {
        return new ProfileResource(entity.getId(), entity.getFullName(), entity.getEmail(), entity.getPhoneNumber(),
                entity.getRuc(), entity.getPlanId());
    }
}

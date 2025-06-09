package upc.edu.ecomovil.microservices.iam.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.UserResource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembler to transform User to UserResource.
 */
public class UserResourceFromEntityAssembler {

    /**
     * Transform a User to a UserResource.
     * 
     * @param entity the User entity
     * @return the UserResource
     */
    public static UserResource toResourceFromEntity(User entity) {
        List<String> roleNames = entity.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new UserResource(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                roleNames);
    }

    /**
     * Transform a list of Users to a list of UserResources.
     * 
     * @param entities the list of User entities
     * @return the list of UserResources
     */
    public static List<UserResource> toResourceFromEntityList(List<User> entities) {
        return entities.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
    }
}

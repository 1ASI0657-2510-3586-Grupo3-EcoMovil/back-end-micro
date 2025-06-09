package upc.edu.ecomovil.microservices.iam.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.AuthenticatedUserResource;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Assembler to transform User with token to AuthenticatedUserResource.
 */
public class AuthenticatedUserResourceFromEntityAssembler {

    /**
     * Transform a User with authentication token to an AuthenticatedUserResource.
     * 
     * @param userTokenPair the pair containing user and token
     * @return the AuthenticatedUserResource
     */
    public static AuthenticatedUserResource toResourceFromEntity(ImmutablePair<User, String> userTokenPair) {
        return new AuthenticatedUserResource(
                userTokenPair.getLeft().getId(),
                userTokenPair.getLeft().getUsername(),
                userTokenPair.getRight());
    }
}

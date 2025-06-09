package upc.edu.ecomovil.microservices.iam.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.iam.domain.model.commands.SignInCommand;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.SignInResource;

/**
 * Assembler to transform SignInResource to SignInCommand.
 */
public class SignInCommandFromResourceAssembler {

    /**
     * Transform a SignInResource to a SignInCommand.
     * 
     * @param resource the SignInResource
     * @return the SignInCommand
     */
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}

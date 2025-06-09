package upc.edu.ecomovil.microservices.iam.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.iam.domain.model.commands.SignUpCommand;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.SignUpResource;

/**
 * Assembler to transform SignUpResource to SignUpCommand.
 */
public class SignUpCommandFromResourceAssembler {

    /**
     * Transform a SignUpResource to a SignUpCommand.
     * 
     * @param resource the SignUpResource
     * @return the SignUpCommand
     */
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(resource.username(), resource.password(), resource.email(), resource.roles());
    }
}

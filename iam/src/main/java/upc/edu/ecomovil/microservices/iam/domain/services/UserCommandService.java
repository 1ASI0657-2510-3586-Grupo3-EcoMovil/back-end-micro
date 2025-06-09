package upc.edu.ecomovil.microservices.iam.domain.services;

import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.microservices.iam.domain.model.commands.SignInCommand;
import upc.edu.ecomovil.microservices.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

/**
 * User command service interface.
 * <p>
 * This interface defines the operations for managing users in the IAM domain.
 * </p>
 */
public interface UserCommandService {

    /**
     * Handle the sign-up command.
     * 
     * @param command the sign-up command
     * @return the created user
     */
    Optional<User> handle(SignUpCommand command);

    /**
     * Handle the sign-in command.
     * 
     * @param command the sign-in command
     * @return a pair containing the user and the generated token
     */
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
}

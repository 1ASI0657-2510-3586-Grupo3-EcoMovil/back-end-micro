package upc.edu.ecomovil.microservices.iam.domain.model.commands;

import java.util.List;

/**
 * Command to sign up a new user.
 * 
 * @param username the username
 * @param password the password
 * @param email    the email address
 * @param roles    the roles for the user
 */
public record SignUpCommand(String username, String password, String email, List<String> roles) {

    public SignUpCommand {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
    }
}

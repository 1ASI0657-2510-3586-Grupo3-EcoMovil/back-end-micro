package upc.edu.ecomovil.microservices.iam.domain.model.commands;

/**
 * Command to sign in a user.
 * 
 * @param username the username
 * @param password the password
 */
public record SignInCommand(String username, String password) {

    public SignInCommand {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }
}

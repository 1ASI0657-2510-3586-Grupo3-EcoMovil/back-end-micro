package upc.edu.ecomovil.microservices.iam.interfaces.rest.resources;

import java.util.List;

/**
 * Sign up resource.
 * 
 * @param username the username
 * @param password the password
 * @param email    the email address
 * @param roles    the roles for the user
 */
public record SignUpResource(String username, String password, String email, List<String> roles) {
}

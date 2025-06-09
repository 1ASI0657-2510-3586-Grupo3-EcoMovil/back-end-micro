package upc.edu.ecomovil.microservices.iam.interfaces.rest.resources;

/**
 * Sign in resource.
 * 
 * @param username the username
 * @param password the password
 */
public record SignInResource(String username, String password) {
}

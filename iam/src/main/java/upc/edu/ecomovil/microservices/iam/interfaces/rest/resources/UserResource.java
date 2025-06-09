package upc.edu.ecomovil.microservices.iam.interfaces.rest.resources;

import java.util.List;

/**
 * User resource.
 * 
 * @param id       the user ID
 * @param username the username
 * @param email    the email address
 * @param roles    the user roles
 */
public record UserResource(Long id, String username, String email, List<String> roles) {
}

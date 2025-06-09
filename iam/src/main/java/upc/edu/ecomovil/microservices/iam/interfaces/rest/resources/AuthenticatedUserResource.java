package upc.edu.ecomovil.microservices.iam.interfaces.rest.resources;

/**
 * Authenticated user resource.
 * 
 * @param id       the user ID
 * @param username the username
 * @param token    the authentication token
 */
public record AuthenticatedUserResource(Long id, String username, String token) {
}

package upc.edu.ecomovil.microservices.iam.domain.model.queries;

/**
 * Query to get a user by username.
 * 
 * @param username the username
 */
public record GetUserByUsernameQuery(String username) {

    public GetUserByUsernameQuery {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
    }
}

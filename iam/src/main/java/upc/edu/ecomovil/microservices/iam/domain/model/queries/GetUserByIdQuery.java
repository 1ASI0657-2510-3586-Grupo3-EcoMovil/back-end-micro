package upc.edu.ecomovil.microservices.iam.domain.model.queries;

/**
 * Query to get a user by id.
 * 
 * @param userId the user id
 */
public record GetUserByIdQuery(Long userId) {

    public GetUserByIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
    }
}

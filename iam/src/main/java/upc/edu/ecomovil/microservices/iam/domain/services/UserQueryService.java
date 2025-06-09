package upc.edu.ecomovil.microservices.iam.domain.services;

import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetAllUsersQuery;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetUserByIdQuery;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

/**
 * User query service interface.
 * <p>
 * This interface defines the read operations for users in the IAM domain.
 * </p>
 */
public interface UserQueryService {

    /**
     * Handle the get all users query.
     * 
     * @param query the query
     * @return the list of users
     */
    List<User> handle(GetAllUsersQuery query);

    /**
     * Handle the get user by id query.
     * 
     * @param query the query
     * @return the user if found
     */
    Optional<User> handle(GetUserByIdQuery query);

    /**
     * Handle the get user by username query.
     * 
     * @param query the query
     * @return the user if found
     */
    Optional<User> handle(GetUserByUsernameQuery query);
}

package upc.edu.ecomovil.microservices.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetAllUsersQuery;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetUserByIdQuery;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetUserByUsernameQuery;
import upc.edu.ecomovil.microservices.iam.domain.services.UserQueryService;
import upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * User query service implementation.
 * <p>
 * This class implements the UserQueryService interface and provides
 * the implementation for user queries.
 * </p>
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handle the get all users query.
     * 
     * @param query the query
     * @return the list of all users
     */
    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    /**
     * Handle the get user by id query.
     * 
     * @param query the query containing the user id
     * @return an optional containing the user if found
     */
    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    /**
     * Handle the get user by username query.
     * 
     * @param query the query containing the username
     * @return an optional containing the user if found
     */
    @Override
    public Optional<User> handle(GetUserByUsernameQuery query) {
        return userRepository.findByUsername(query.username());
    }
}

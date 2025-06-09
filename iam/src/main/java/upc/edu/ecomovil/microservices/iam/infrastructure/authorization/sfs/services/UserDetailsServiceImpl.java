package upc.edu.ecomovil.microservices.iam.infrastructure.authorization.sfs.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories.UserRepository;

/**
 * User details service implementation.
 * <p>
 * This class implements the UserDetailsService interface and provides
 * user details for Spring Security authentication.
 * </p>
 */
@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user details by username.
     * 
     * @param username the username
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserDetailsImpl.build(user);
    }
}

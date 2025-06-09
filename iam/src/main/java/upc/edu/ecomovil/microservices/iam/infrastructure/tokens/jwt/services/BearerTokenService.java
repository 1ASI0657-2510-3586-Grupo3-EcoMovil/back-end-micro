package upc.edu.ecomovil.microservices.iam.infrastructure.tokens.jwt.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.tokens.TokenService;

/**
 * Bearer token service interface.
 * <p>
 * This interface extends the TokenService interface and provides
 * additional methods for handling Bearer tokens in HTTP requests.
 * </p>
 */
public interface BearerTokenService extends TokenService {

    /**
     * Extract the Bearer token from an HTTP request.
     * 
     * @param request the HTTP request
     * @return the JWT token or null if not found
     */
    String getBearerTokenFrom(HttpServletRequest request);

    /**
     * Generate a token from an authentication object.
     * 
     * @param authentication the authentication object
     * @return the generated token
     */
    String generateToken(Authentication authentication);

    /**
     * Generate a token from username and roles.
     * 
     * @param username the username
     * @param roles    the user roles
     * @return the generated token
     */
    String generateToken(String username, java.util.List<String> roles);

    /**
     * Generate a token from username, roles, and userId.
     * 
     * @param username the username
     * @param roles    the user roles
     * @param userId   the user ID
     * @return the generated token
     */
    String generateToken(String username, java.util.List<String> roles, Long userId);

    /**
     * Extract roles from a JWT token.
     * 
     * @param token the JWT token
     * @return the list of roles
     */
    java.util.List<String> getRolesFromToken(String token);
}

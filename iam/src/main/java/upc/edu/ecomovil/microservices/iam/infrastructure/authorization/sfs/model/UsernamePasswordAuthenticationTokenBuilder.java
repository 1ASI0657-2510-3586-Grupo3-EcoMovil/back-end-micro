package upc.edu.ecomovil.microservices.iam.infrastructure.authorization.sfs.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/**
 * Username password authentication token builder.
 * <p>
 * This class provides utility methods for building authentication tokens
 * used by Spring Security.
 * </p>
 */
public class UsernamePasswordAuthenticationTokenBuilder {

    /**
     * Build a UsernamePasswordAuthenticationToken from user details and HTTP
     * request.
     * 
     * @param principal the user details
     * @param request   the HTTP request
     * @return the authentication token
     */
    public static UsernamePasswordAuthenticationToken build(UserDetails principal, HttpServletRequest request) {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }
}

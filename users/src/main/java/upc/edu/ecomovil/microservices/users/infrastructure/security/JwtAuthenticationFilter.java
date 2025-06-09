package upc.edu.ecomovil.microservices.users.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for Users Microservice
 * 
 * This filter intercepts HTTP requests and validates JWT tokens from the
 * Authorization header.
 * If a valid token is found, it extracts user information and roles to set up
 * Spring Security context with a proper UserDetails object.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        logger.debug("Processing request for URL: {}", request.getRequestURL());
        logger.debug("Authorization header: {}", request.getHeader("Authorization"));

        try {
            String jwt = parseJwt(request);
            logger.debug("Extracted JWT token: {}", jwt != null ? jwt.substring(0, Math.min(jwt.length(), 20)) + "..." : "null");

            if (jwt != null) {
                boolean isValid = jwtUtils.validateToken(jwt);
                logger.debug("Token validation result: {}", isValid);
                
                if (isValid) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    Long userId = jwtUtils.getUserIdFromToken(jwt);
                    List<String> roles = jwtUtils.getRolesFromToken(jwt);

                    logger.info("JWT Token validated successfully for user: {}, userId: {}, roles: {}", username, userId,
                            roles);

                    // Convert roles to Spring Security authorities (handle null roles)
                    List<SimpleGrantedAuthority> authorities = (roles != null)
                            ? roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                            : List.of(); // Empty list if no roles

                    logger.info("Converted authorities: {}", authorities);

                    // Create UserDetails object for @AuthenticationPrincipal with userId
                    JwtUserDetails userDetails = new JwtUserDetails(username, userId, authorities);

                    // Create authentication token with UserDetails as principal
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in Security Context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Authentication set for user: {}", username);
                } else {
                    logger.warn("Token is invalid");
                }
            } else {
                logger.debug("No JWT token found in request");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer <token>"
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}

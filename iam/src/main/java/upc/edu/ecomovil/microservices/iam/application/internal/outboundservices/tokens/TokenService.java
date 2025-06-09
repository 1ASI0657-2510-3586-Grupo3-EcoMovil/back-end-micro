package upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.tokens;

/**
 * Token service interface.
 * <p>
 * This interface defines the contract for token operations in the IAM context.
 * </p>
 */
public interface TokenService {

    /**
     * Generate a token for a given username.
     * 
     * @param username the username
     * @return the generated token
     */
    String generateToken(String username);

    /**
     * Extract the username from a token.
     * 
     * @param token the token
     * @return the username
     */
    String getUsernameFromToken(String token);

    /**
     * Validate a token.
     * 
     * @param token the token
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}

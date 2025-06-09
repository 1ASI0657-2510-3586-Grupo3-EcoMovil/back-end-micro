package upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.hashing;

/**
 * Hashing service interface.
 * <p>
 * This interface defines the contract for hashing operations in the IAM
 * context.
 * </p>
 */
public interface HashingService {

    /**
     * Encode a raw password.
     * 
     * @param rawPassword the raw password
     * @return the encoded password
     */
    String encode(CharSequence rawPassword);

    /**
     * Check if a raw password matches an encoded password.
     * 
     * @param rawPassword     the raw password
     * @param encodedPassword the encoded password
     * @return true if the passwords match, false otherwise
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

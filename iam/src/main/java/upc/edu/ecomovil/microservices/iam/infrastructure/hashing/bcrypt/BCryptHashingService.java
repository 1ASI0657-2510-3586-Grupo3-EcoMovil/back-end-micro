package upc.edu.ecomovil.microservices.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.hashing.HashingService;

/**
 * BCrypt hashing service implementation.
 * <p>
 * This class implements the HashingService interface and provides
 * password hashing using the BCrypt algorithm.
 * </p>
 */
@Service
public class BCryptHashingService implements HashingService, PasswordEncoder {

    private final BCryptPasswordEncoder encoder;

    public BCryptHashingService() {
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * Encode a raw password using BCrypt.
     * 
     * @param rawPassword the raw password
     * @return the encoded password
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Check if a raw password matches an encoded password.
     * 
     * @param rawPassword     the raw password
     * @param encodedPassword the encoded password
     * @return true if the passwords match, false otherwise
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}

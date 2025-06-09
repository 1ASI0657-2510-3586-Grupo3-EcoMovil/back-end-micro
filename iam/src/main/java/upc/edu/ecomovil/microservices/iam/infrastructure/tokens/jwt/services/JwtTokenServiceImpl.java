package upc.edu.ecomovil.microservices.iam.infrastructure.tokens.jwt.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.tokens.TokenService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT token service implementation.
 * <p>
 * This class implements the TokenService interface and provides
 * JWT token generation and validation functionality.
 * </p>
 */
@Service
public class JwtTokenServiceImpl implements TokenService, BearerTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenServiceImpl.class);
    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = 7;

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;

    /**
     * Generate a JWT token from a username.
     * 
     * @param username the username
     * @return the JWT token
     */
    @Override
    public String generateToken(String username) {
        return buildTokenWithDefaultParameters(username);
    }

    /**
     * Generate a JWT token from a username with roles.
     * 
     * @param username the username
     * @param roles    the user roles
     * @return the JWT token
     */
    public String generateToken(String username, java.util.List<String> roles) {
        return buildTokenWithRoles(username, roles);
    }

    /**
     * Generate a JWT token from an authentication object.
     * 
     * @param authentication the authentication object
     * @return the JWT token
     */
    public String generateToken(Authentication authentication) {
        return buildTokenWithDefaultParameters(authentication.getName());
    }

    /**
     * Generate a JWT token from a username with roles and userId.
     * 
     * @param username the username
     * @param roles    the user roles
     * @param userId   the user ID
     * @return the JWT token
     */
    public String generateToken(String username, java.util.List<String> roles, Long userId) {
        return buildTokenWithUserIdAndRoles(username, roles, userId);
    }

    /**
     * Extract the username from a JWT token.
     * 
     * @param token the token
     * @return the username
     */
    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the roles from a JWT token.
     * 
     * @param token the token
     * @return the list of roles
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getRolesFromToken(String token) {
        return extractClaim(token, claims -> (java.util.List<String>) claims.get("roles"));
    }

    /**
     * Validate a JWT token.
     * 
     * @param token the token
     * @return true if the token is valid, false otherwise
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            LOGGER.info("Token is valid");
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JSON Web Token Signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JSON Web Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JSON Web Token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JSON Web Token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JSON Web Token claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract the Bearer token from an HTTP request.
     * 
     * @param request the HTTP request
     * @return the JWT token or null if not found
     */
    public String getBearerTokenFrom(HttpServletRequest request) {
        String parameter = getAuthorizationParameterFrom(request);
        if (isTokenPresentIn(parameter) && isBearerTokenIn(parameter)) {
            return extractTokenFrom(parameter);
        }
        return null;
    }

    /**
     * Build a JWT token with default parameters.
     * 
     * @param username the username
     * @return the JWT token
     */
    private String buildTokenWithDefaultParameters(String username) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Build a JWT token with roles.
     * 
     * @param username the username
     * @param roles    the user roles
     * @return the JWT token
     */
    private String buildTokenWithRoles(String username, java.util.List<String> roles) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("authorities", roles) // Spring Security compatible
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Build a JWT token with userId and roles.
     * 
     * @param username the username
     * @param roles    the user roles
     * @param userId   the user ID
     * @return the JWT token
     */
    private String buildTokenWithUserIdAndRoles(String username, java.util.List<String> roles, Long userId) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("authorities", roles) // Spring Security compatible
                .claim("userId", userId)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Extract a claim from a token.
     * 
     * @param token           the token
     * @param claimsResolvers the claims resolver
     * @param <T>             the type of the claim
     * @return the claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Extract all claims from a token.
     * 
     * @param token the token
     * @return the claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Get the signing key.
     * 
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenPresentIn(String authorizationParameter) {
        return StringUtils.hasText(authorizationParameter);
    }

    private boolean isBearerTokenIn(String authorizationParameter) {
        return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
    }

    private String extractTokenFrom(String authorizationHeaderParameter) {
        return authorizationHeaderParameter.substring(TOKEN_BEGIN_INDEX);
    }

    private String getAuthorizationParameterFrom(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
    }
}

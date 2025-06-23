package upc.edu.ecomovil.microservices.reservations.application.internal.outboundservices.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class ExternalUserService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUserService.class);

    @Value("${external.users.service.url}")
    private String usersServiceUrl;

    private final RestTemplate restTemplate;

    public ExternalUserService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch a user profile by user ID from the Users microservice.
     * 
     * @param userId the user ID from the JWT token
     * @return the profile DTO if found, empty otherwise
     */
    public Optional<UserProfileDto> fetchUserProfileById(Long userId) {
        try {
            logger.info("Fetching profile with user ID: {} from Users service at URL: {}", userId, usersServiceUrl);

            // Get JWT token from current request
            String jwtToken = getJwtTokenFromRequest();
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null) {
                headers.set("Authorization", "Bearer " + jwtToken);
                logger.debug("Added JWT token to request headers");
            } else {
                logger.warn("No JWT token found in request");
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = usersServiceUrl + "/api/v1/profiles/user/" + userId;
            logger.debug("Making request to: {}", url);

            ResponseEntity<UserProfileDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserProfileDto.class);

            logger.debug("Received response with status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully fetched profile with user ID: {}", userId);
                return Optional.of(response.getBody());
            } else {
                logger.warn("Profile with user ID {} not found", userId);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error fetching profile with user ID {} from users service at {}: {}",
                    userId, usersServiceUrl, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Extract JWT token from current HTTP request.
     * 
     * @return JWT token if found, null otherwise
     */
    private String getJwtTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract JWT token from request: {}", e.getMessage());
        }
        return null;
    }

    // DTO for User Profile data from Users microservice
    public static class UserProfileDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String ruc;
        private Long planId;

        // Default constructor for Jackson
        public UserProfileDto() {
        }

        // Constructor
        public UserProfileDto(Long id, String firstName, String lastName, String email,
                String phoneNumber, String ruc, Long planId) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.ruc = ruc;
            this.planId = planId;
        }

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getRuc() {
            return ruc;
        }

        public void setRuc(String ruc) {
            this.ruc = ruc;
        }

        public Long getPlanId() {
            return planId;
        }

        public void setPlanId(Long planId) {
            this.planId = planId;
        }
    }
}

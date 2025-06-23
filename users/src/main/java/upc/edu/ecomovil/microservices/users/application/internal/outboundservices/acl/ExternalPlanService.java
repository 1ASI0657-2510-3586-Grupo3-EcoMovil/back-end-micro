package upc.edu.ecomovil.microservices.users.application.internal.outboundservices.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class ExternalPlanService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalPlanService.class);

    @Value("${services.plans.url}")
    private String plansServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<PlanDto> fetchPlanById(Long id) {
        logger.error("=== ExternalPlanService.fetchPlanById called with ID: {} ===", id);
        logger.error("Plans service URL configured as: {}", plansServiceUrl);
        logger.info("Fetching plan with ID: {} from URL: {}", id, plansServiceUrl);

        try {
            String url = plansServiceUrl + "/api/v1/plans/id/" + id;
            logger.debug("Making HTTP GET request to: {}", url);

            // Get JWT token from current HTTP request
            HttpHeaders headers = new HttpHeaders();

            // Try to get the token from the current web request context
            String authToken = getCurrentJwtToken();
            if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
                logger.debug("Added Authorization header with JWT token");
            } else {
                logger.warn("No JWT token found in current request context");
            }

            // Create HTTP entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the request with authentication headers
            ResponseEntity<PlanDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, PlanDto.class);
            PlanDto plan = response.getBody();

            if (plan != null) {
                logger.info("Successfully fetched plan with ID: {} - Name: {}", id, plan.getName());
                return Optional.of(plan);
            } else {
                logger.warn("Plan with ID: {} not found - received null response", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            // Return empty if plan is not found or service is unavailable
            logger.error("Error fetching plan with ID {}: {}", id, e.getMessage());
            logger.debug("Full exception stack trace:", e);
            return Optional.empty();
        }
    }

    // DTO for Plan data from Plans microservice
    public static class PlanDto {
        private Long id;
        private String name;
        private String description;

        // Default constructor for Jackson
        public PlanDto() {
        }

        // Constructor for fallback
        public PlanDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        // getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Get the current JWT token from the web request context
     */
    private String getCurrentJwtToken() {
        try {
            // Get the current HTTP request from RequestContextHolder
            org.springframework.web.context.request.RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes();

            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes)
                        .getRequest();

                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7); // Remove "Bearer " prefix
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract JWT token from request context: {}", e.getMessage());
        }
        return null;
    }
}
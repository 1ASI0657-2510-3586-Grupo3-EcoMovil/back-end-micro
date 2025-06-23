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
public class ExternalVehicleService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalVehicleService.class);

    @Value("${external.vehicles.service.url}")
    private String vehiclesServiceUrl;

    private final RestTemplate restTemplate;

    public ExternalVehicleService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch a vehicle by vehicle ID from the Vehicles microservice.
     * 
     * @param vehicleId the vehicle ID
     * @return the vehicle DTO if found, empty otherwise
     */
    public Optional<VehicleDto> fetchVehicleById(Long vehicleId) {
        try {
            logger.info("Fetching vehicle with ID: {} from Vehicles service at URL: {}", vehicleId, vehiclesServiceUrl);

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

            String url = vehiclesServiceUrl + "/api/v1/vehicles/public/" + vehicleId;
            logger.debug("Making request to: {}", url);

            ResponseEntity<VehicleDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    VehicleDto.class);

            logger.debug("Received response with status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully fetched vehicle with ID: {}", vehicleId);
                return Optional.of(response.getBody());
            } else {
                logger.warn("Vehicle with ID {} not found", vehicleId);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error fetching vehicle with ID {} from vehicles service at {}: {}",
                    vehicleId, vehiclesServiceUrl, e.getMessage(), e);
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

    // DTO for Vehicle data from Vehicles microservice
    public static class VehicleDto {
        private Long id;
        private String type;
        private String name;
        private Integer year;
        private Integer review;
        private Double priceRent;
        private Double priceSell;
        private Boolean isAvailable;
        private String imageUrl;
        private Double lat;
        private Double lng;
        private String description;
        private Long ownerId;

        // Default constructor for Jackson
        public VehicleDto() {
        }

        // Constructor
        public VehicleDto(Long id, String type, String name, Integer year, Integer review,
                Double priceRent, Double priceSell, Boolean isAvailable, String imageUrl,
                Double lat, Double lng, String description, Long ownerId) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.year = year;
            this.review = review;
            this.priceRent = priceRent;
            this.priceSell = priceSell;
            this.isAvailable = isAvailable;
            this.imageUrl = imageUrl;
            this.lat = lat;
            this.lng = lng;
            this.description = description;
            this.ownerId = ownerId;
        }

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getReview() {
            return review;
        }

        public void setReview(Integer review) {
            this.review = review;
        }

        public Double getPriceRent() {
            return priceRent;
        }

        public void setPriceRent(Double priceRent) {
            this.priceRent = priceRent;
        }

        public Double getPriceSell() {
            return priceSell;
        }

        public void setPriceSell(Double priceSell) {
            this.priceSell = priceSell;
        }

        public Boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(Boolean available) {
            isAvailable = available;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }
    }
}

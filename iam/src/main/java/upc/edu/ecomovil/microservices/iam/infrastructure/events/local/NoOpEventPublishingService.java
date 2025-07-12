package upc.edu.ecomovil.microservices.iam.infrastructure.events.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.events.EventPublishingService;

/**
 * No-op event publishing service implementation.
 * <p>
 * This implementation logs events but doesn't publish them to any external system.
 * Useful for development and testing when external event publishing is not needed.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "app.events.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEventPublishingService implements EventPublishingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpEventPublishingService.class);

    /**
     * Log a user registered event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    @Override
    public void publishUserRegisteredEvent(Long userId, String username) {
        LOGGER.info("EVENT: User registered - userId: {}, username: {}", userId, username);
    }

    /**
     * Log a user authenticated event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    @Override
    public void publishUserAuthenticatedEvent(Long userId, String username) {
        LOGGER.info("EVENT: User authenticated - userId: {}, username: {}", userId, username);
    }

    /**
     * Log a user deactivated event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    @Override
    public void publishUserDeactivatedEvent(Long userId, String username) {
        LOGGER.info("EVENT: User deactivated - userId: {}, username: {}", userId, username);
    }
}

package upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.events;

/**
 * Event publishing service interface.
 * <p>
 * This interface defines the contract for publishing domain events to external
 * systems.
 * </p>
 */
public interface EventPublishingService {

    /**
     * Publish a user registered event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    void publishUserRegisteredEvent(Long userId, String username);

    /**
     * Publish a user authenticated event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    void publishUserAuthenticatedEvent(Long userId, String username);

    /**
     * Publish a user deactivated event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    void publishUserDeactivatedEvent(Long userId, String username);
}

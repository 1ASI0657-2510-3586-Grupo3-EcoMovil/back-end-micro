package upc.edu.ecomovil.microservices.iam.infrastructure.events.sns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.events.EventPublishingService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SNS event publishing service implementation.
 * <p>
 * This class implements the EventPublishingService interface and provides
 * event publishing functionality using AWS SNS.
 * </p>
 * 
 * TEMPORARILY DISABLED - Use NoOpEventPublishingService instead
 */
// @Service
// @ConditionalOnProperty(name = "app.events.enabled", havingValue = "true")
public class SnsEventPublishingService implements EventPublishingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnsEventPublishingService.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.user-events-topic}")
    private String userEventsTopicArn;

    @Value("${aws.sns.auth-events-topic}")
    private String authEventsTopicArn;

    public SnsEventPublishingService(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish a user registered event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    @Override
    public void publishUserRegisteredEvent(Long userId, String username) {
        try {
            var eventData = Map.of(
                    "eventType", "USER_REGISTERED",
                    "userId", userId,
                    "username", username,
                    "timestamp", LocalDateTime.now().toString(),
                    "source", "iam-service");

            var message = objectMapper.writeValueAsString(eventData);

            var publishRequest = PublishRequest.builder()
                    .topicArn(userEventsTopicArn)
                    .message(message)
                    .subject("User Registered")
                    .build();

            var result = snsClient.publish(publishRequest);
            LOGGER.info("Published user registered event for user: {} with message ID: {}",
                    username, result.messageId());

        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing user registered event: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error publishing user registered event: {}", e.getMessage());
        }
    }

    /**
     * Publish a user authenticated event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    @Override
    public void publishUserAuthenticatedEvent(Long userId, String username) {
        try {
            var eventData = Map.of(
                    "eventType", "USER_AUTHENTICATED",
                    "userId", userId,
                    "username", username,
                    "timestamp", LocalDateTime.now().toString(),
                    "source", "iam-service");

            var message = objectMapper.writeValueAsString(eventData);

            var publishRequest = PublishRequest.builder()
                    .topicArn(authEventsTopicArn)
                    .message(message)
                    .subject("User Authenticated")
                    .build();

            var result = snsClient.publish(publishRequest);
            LOGGER.info("Published user authenticated event for user: {} with message ID: {}",
                    username, result.messageId());

        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing user authenticated event: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error publishing user authenticated event: {}", e.getMessage());
        }
    }

    /**
     * Publish a user deactivated event.
     * 
     * @param userId   the user ID
     * @param username the username
     */
    @Override
    public void publishUserDeactivatedEvent(Long userId, String username) {
        try {
            var eventData = Map.of(
                    "eventType", "USER_DEACTIVATED",
                    "userId", userId,
                    "username", username,
                    "timestamp", LocalDateTime.now().toString(),
                    "source", "iam-service");

            var message = objectMapper.writeValueAsString(eventData);

            var publishRequest = PublishRequest.builder()
                    .topicArn(userEventsTopicArn)
                    .message(message)
                    .subject("User Deactivated")
                    .build();

            var result = snsClient.publish(publishRequest);
            LOGGER.info("Published user deactivated event for user: {} with message ID: {}",
                    username, result.messageId());

        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing user deactivated event: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error publishing user deactivated event: {}", e.getMessage());
        }
    }
}

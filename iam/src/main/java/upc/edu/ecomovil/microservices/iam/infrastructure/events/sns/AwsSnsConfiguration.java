package upc.edu.ecomovil.microservices.iam.infrastructure.events.sns;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

/**
 * AWS SNS configuration.
 * <p>
 * This class provides configuration for AWS SNS client and related beans.
 * </p>
 */
@Configuration
public class AwsSnsConfiguration {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint.override:}")
    private String endpointOverride;

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    /**
     * Create an SNS client bean.
     * 
     * @return the SNS client
     */
    @Bean
    public SnsClient snsClient() {
        var credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));

        var clientBuilder = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        // For local development using LocalStack
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            clientBuilder.endpointOverride(URI.create(endpointOverride));
        }

        return clientBuilder.build();
    }

    /**
     * Create an ObjectMapper bean for JSON serialization.
     * 
     * @return the ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

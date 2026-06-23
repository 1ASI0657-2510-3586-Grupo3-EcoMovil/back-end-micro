package upc.edu.ecomovil.microservices.vehicles.infrastructure.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient;
import software.amazon.awssdk.services.iotdataplane.model.PublishRequest;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class IoTCoreService {

    private final IotDataPlaneClient iotClient;

    public IoTCoreService(@Value("${aws.iot.endpoint}") String iotEndpoint) {
        this.iotClient = IotDataPlaneClient.builder()
                .endpointOverride(URI.create("https://" + iotEndpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        log.info("IoTCoreService initialised with endpoint: {}", iotEndpoint);
    }

    /**
     * Sends a LOCK or UNLOCK command to a specific device.
     * Topic: ecomovil/commands/{deviceId}
     */
    public void sendCommand(String deviceId, String command) {
        String topic   = "ecomovil/commands/" + deviceId;
        String payload = "{\"command\":\"" + command + "\"}";
        publish(topic, payload);
        log.info("IoT command sent: topic={} payload={}", topic, payload);
    }

    private void publish(String topic, String payload) {
        try {
            iotClient.publish(PublishRequest.builder()
                    .topic(topic)
                    .qos(1)
                    .payload(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                    .build());
        } catch (Exception e) {
            log.error("Failed to publish to IoT topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("IoT publish failed: " + e.getMessage(), e);
        }
    }
}

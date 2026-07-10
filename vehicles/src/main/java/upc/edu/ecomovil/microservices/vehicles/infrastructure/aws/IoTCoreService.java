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

    /**
     * Sends geofence parameters to the device so it can run Haversine locally (edge).
     * The ESP32 will beep immediately when it detects a breach without waiting for a server round-trip.
     * Topic: ecomovil/commands/{deviceId}
     */
    public void sendGeofenceCommand(String deviceId, Double lat, Double lng, Double radiusM) {
        String topic = "ecomovil/commands/" + deviceId;
        String payload = String.format(
                "{\"command\":\"GEOFENCE\",\"enabled\":true,\"lat\":%f,\"lng\":%f,\"radius_m\":%f}",
                lat, lng, radiusM);
        publish(topic, payload);
        log.info("IoT geofence pushed to device {}: lat={} lng={} radius={}m", deviceId, lat, lng, radiusM);
    }

    /**
     * Disables the geofence on the device (e.g. when owner clears the geofence).
     */
    public void clearGeofenceCommand(String deviceId) {
        String topic   = "ecomovil/commands/" + deviceId;
        String payload = "{\"command\":\"GEOFENCE\",\"enabled\":false,\"lat\":0,\"lng\":0,\"radius_m\":0}";
        publish(topic, payload);
        log.info("IoT geofence cleared on device {}", deviceId);
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

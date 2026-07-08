package upc.edu.ecomovil.microservices.vehicles.infrastructure.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

@Service
public class TelemetryHistoryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryHistoryService.class);
    private static final String TABLE = "ecomovil-telemetry-history";

    private final DynamoDbClient dynamo;

    public TelemetryHistoryService(@Value("${aws.region:us-east-1}") String region) {
        this.dynamo = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Returns the last {@code limit} telemetry records for a vehicle, newest first.
     * DynamoDB sorts by ts (epoch ms) ascending; we reverse in-memory for the API.
     */
    public List<Map<String, AttributeValue>> getHistory(long vehicleId, int limit) {
        try {
            var request = QueryRequest.builder()
                    .tableName(TABLE)
                    .keyConditionExpression("vehicle_id = :vid")
                    .expressionAttributeValues(Map.of(
                            ":vid", AttributeValue.fromN(String.valueOf(vehicleId))
                    ))
                    .scanIndexForward(false) // newest first
                    .limit(limit)
                    .build();

            var response = dynamo.query(request);
            log.debug("DynamoDB history for vehicle {}: {} items", vehicleId, response.count());
            return response.items();
        } catch (Exception e) {
            log.error("DynamoDB query failed for vehicle {}: {}", vehicleId, e.getMessage());
            return List.of();
        }
    }
}

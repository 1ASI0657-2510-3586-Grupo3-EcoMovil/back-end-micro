package upc.edu.ecomovil.microservices.vehicles.infrastructure.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;

import java.util.List;
import java.util.Map;

/**
 * Sales chatbot backed by Amazon Nova Micro (cheapest Bedrock text model)
 * with a Bedrock Guardrail attached. The model never sees the full catalog —
 * it only gets a pre-filtered candidate list computed in Java, so it can't
 * hallucinate vehicles or prices that don't exist.
 */
@Service
@Slf4j
public class BedrockChatService {

    private static final String MODEL_ID = "amazon.nova-micro-v1:0";
    private final BedrockRuntimeClient bedrockClient;
    private final String guardrailId;
    private final String guardrailVersion;

    public BedrockChatService(@Value("${aws.bedrock.guardrail-secret:/ecomovil/bedrock-guardrail}") String secretName) {
        this.bedrockClient = BedrockRuntimeClient.builder().region(Region.US_EAST_1).build();

        String id = "";
        String version = "";
        try (SecretsManagerClient sm = SecretsManagerClient.builder().region(Region.US_EAST_1).build()) {
            String json = sm.getSecretValue(GetSecretValueRequest.builder().secretId(secretName).build()).secretString();
            Map<?, ?> parsed = new ObjectMapper().readValue(json, Map.class);
            id = String.valueOf(parsed.get("guardrailId"));
            version = String.valueOf(parsed.get("guardrailVersion"));
        } catch (Exception e) {
            // ponytail: chatbot still works without a guardrail (just less safe);
            // don't fail service startup over it.
            log.warn("Could not load Bedrock guardrail config, running without it: {}", e.getMessage());
        }
        this.guardrailId = id;
        this.guardrailVersion = version;
    }

    public String chat(String userMessage, List<Vehicle> candidates) {
        String catalog = candidates.isEmpty() ? "No hay vehiculos disponibles ahora."
                : candidates.stream()
                        .map(v -> String.format("- id=%d, %s %s (%d), venta S/%.0f, renta S/%.0f/dia",
                                v.getId(), v.getType(), v.getName(), v.getYear(), v.getPriceSell(), v.getPriceRent()))
                        .reduce("", (a, b) -> a + b + "\n");

        String system = "Eres el asistente de ventas de EcoMovil, un marketplace de vehiculos ecologicos "
                + "(bicicletas, scooters, monopatines, rollskaters) para estudiantes en Lima. "
                + "Responde SOLO sobre los vehiculos de esta lista, nunca inventes otros: \n" + catalog
                + "\nSe breve (maximo 60 palabras), en español, y sugiere como maximo 1 vehiculo de la lista por id.";

        var request = ConverseRequest.builder()
                .modelId(MODEL_ID)
                .system(SystemContentBlock.builder().text(system).build())
                .messages(Message.builder()
                        .role(ConversationRole.USER)
                        .content(ContentBlock.builder().text(userMessage).build())
                        .build())
                .inferenceConfig(InferenceConfiguration.builder().maxTokens(200).temperature(0.4f).build());

        if (!guardrailId.isBlank()) {
            request.guardrailConfig(GuardrailConfiguration.builder()
                    .guardrailIdentifier(guardrailId)
                    .guardrailVersion(guardrailVersion)
                    .trace(GuardrailTrace.ENABLED)
                    .build());
        }

        try {
            var response = bedrockClient.converse(request.build());
            if (response.stopReason() == StopReason.GUARDRAIL_INTERVENED) {
                return "Lo siento, no puedo ayudar con eso. Pregúntame sobre vehículos disponibles en EcoMovil.";
            }
            return response.output().message().content().get(0).text();
        } catch (Exception e) {
            log.error("Bedrock chat failed: {}", e.getMessage(), e);
            return "No pude procesar tu mensaje, intenta de nuevo en un momento.";
        }
    }
}

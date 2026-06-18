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

    public record ChatTurn(String role, String text) {
    }

    public String chat(String userMessage, List<Vehicle> candidates, String userName, List<ChatTurn> history) {
        String catalog = candidates.isEmpty() ? "No hay vehiculos disponibles ahora."
                : candidates.stream()
                        .map(v -> String.format("- id=%d, %s %s (%d), venta S/%.0f, renta S/%.0f/dia",
                                v.getId(), v.getType(), v.getName(), v.getYear(), v.getPriceSell(), v.getPriceRent()))
                        .reduce("", (a, b) -> a + b + "\n");

        String greeting = (userName == null || userName.isBlank()) ? "" : " El usuario se llama " + userName + ".";

        String system = "Eres el asistente de ventas de EcoMovil, un marketplace de vehiculos ecologicos "
                + "(bicicletas, scooters, monopatines, rollskaters) para estudiantes en Lima." + greeting
                + " Responde SOLO sobre los vehiculos de esta lista (ya filtrada por presupuesto si el usuario "
                + "dio uno), nunca inventes otros: \n" + catalog
                + "\nREGLA 1: si el usuario SOLO saluda (ej. \"hola\") sin decir nada mas, saludalo (por su "
                + "nombre si lo tienes) y pregunta que tipo de vehiculo busca y cuanto quiere gastar. No "
                + "sugieras nada en ese caso.\n"
                + "REGLA 2: en cualquier otro caso, es decir si el usuario menciona un tipo de vehiculo, un "
                + "presupuesto, o pide una recomendacion, responde INMEDIATAMENTE sugiriendo 1 vehiculo "
                + "especifico de la lista (indica su id), el que mejor se ajuste a lo que pidio. No pidas mas "
                + "detalles, la lista ya esta filtrada para que cualquier opcion sea valida.\n"
                + "Se breve (maximo 60 palabras), en español.";

        // ponytail: no DB-backed session — frontend resends the last few turns each call.
        var messages = new java.util.ArrayList<Message>();
        if (history != null) {
            for (ChatTurn t : history) {
                messages.add(Message.builder()
                        .role("assistant".equals(t.role()) ? ConversationRole.ASSISTANT : ConversationRole.USER)
                        .content(ContentBlock.builder().text(t.text()).build())
                        .build());
            }
        }
        messages.add(Message.builder()
                .role(ConversationRole.USER)
                .content(ContentBlock.builder().text(userMessage).build())
                .build());

        var request = ConverseRequest.builder()
                .modelId(MODEL_ID)
                .system(SystemContentBlock.builder().text(system).build())
                .messages(messages)
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

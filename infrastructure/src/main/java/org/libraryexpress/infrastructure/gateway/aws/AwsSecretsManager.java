package org.libraryexpress.infrastructure.gateway.aws;

import static org.libraryexpress.infrastructure.exception.InfrastructureErrorType.BAD_GATEWAY;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.config.ConfigRegistry;
import org.libraryexpress.infrastructure.config.JacksonConfig;
import org.libraryexpress.infrastructure.exception.InfrastructureException;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

public final class AwsSecretsManager implements SecretProvider {

    private static final CustomLogger LOG = CustomLoggerFactory.getLogger(AwsSecretsManager.class);

    private SecretsManagerClient client;

    @Override
    public Optional<String> get(String id) {
        try (SecretsManagerClient smClient = client()) {
            String jsonResponse =
                    smClient.getSecretValue(request -> request.secretId(id)).secretString();
            return Optional.ofNullable(extractValue(jsonResponse));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Could not read the super admin secret. {}", e.getCause(), e.getMessage());
            throw new InfrastructureException(BAD_GATEWAY, "Could not read the super admin secret" + e.getMessage());
        }
    }

    private synchronized SecretsManagerClient client() {
        if (client == null) {
            client = SecretsManagerClient.builder()
                    .httpClientBuilder(UrlConnectionHttpClient.builder())
                    .build();
        }
        return client;
    }

    private String extractValue(String secretJson) throws Exception {
        JsonNode node = JacksonConfig.mapper().readTree(secretJson);
        return node.get(ConfigRegistry.getAwsSecretName()).asText();
    }
}

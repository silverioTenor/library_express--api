package org.libraryexpress.infrastructure.gateway.aws;

import static org.libraryexpress.infrastructure.exception.InfrastructureErrorType.BAD_GATEWAY;

import java.util.Optional;
import org.libraryexpress.infrastructure.exception.InfrastructureException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

public final class AwsSecretsManager implements SecretProvider {

    private SecretsManagerClient client;

    @Override
    public Optional<String> get(String id) {
        try (SecretsManagerClient smClient = client()) {
            String value =
                    smClient.getSecretValue(request -> request.secretId(id)).secretString();
            return Optional.ofNullable(value);
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (SdkClientException e) {
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
}

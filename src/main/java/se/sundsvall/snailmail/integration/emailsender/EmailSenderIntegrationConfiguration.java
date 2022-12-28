package se.sundsvall.snailmail.integration.emailsender;

import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import java.util.concurrent.TimeUnit;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(EmailSenderIntegrationProperties.class)
public class EmailSenderIntegrationConfiguration {

    private final EmailSenderIntegrationProperties properties;

    public EmailSenderIntegrationConfiguration(EmailSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean
    FeignBuilderCustomizer customizer() {
        return FeignMultiCustomizer.create()
                .withErrorDecoder(errorDecoder())
                .withRequestOptions(requestOptions())
                .withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
                .composeCustomizersToOne();
    }

    Request.Options requestOptions() {
        return new Request.Options(
                properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                true);

    }

    ClientRegistration clientRegistration() {
        return ClientRegistration
                .withRegistrationId(EmailSenderIntegration.INTEGRATION_NAME)
                .tokenUri(properties.getTokenUri())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .authorizationGrantType(new AuthorizationGrantType(properties.getGrantType()))
                .build();
    }

    ErrorDecoder errorDecoder() {
        return new ProblemErrorDecoder(EmailSenderIntegration.INTEGRATION_NAME);
    }
}

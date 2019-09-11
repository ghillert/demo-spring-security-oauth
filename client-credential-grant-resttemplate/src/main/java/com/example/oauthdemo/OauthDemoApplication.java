package com.example.oauthdemo;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

@Configuration
public class OauthDemoApplication {

	public static void main(String[] args) {

		final ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(OauthDemoApplication.class);

		final ClientRegistrationRepository  clientRegistrations = ctx.getBean(ClientRegistrationRepository.class);

		final ClientRegistration clientRegistration = clientRegistrations.findByRegistrationId("default");

		final OAuth2ClientCredentialsGrantRequest grantRequest = new OAuth2ClientCredentialsGrantRequest(clientRegistration);

		final OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient =
				ctx.getBean(OAuth2AccessTokenResponseClient.class);

		final OAuth2AccessTokenResponse res = clientCredentialsTokenResponseClient.getTokenResponse(grantRequest);

		System.out.println(res.getAccessToken().getTokenValue());

	}

	@Bean
	public InMemoryClientRegistrationRepository clientRegistrationRepository() {
		final ClientRegistration clientRegistration = ClientRegistration
				.withRegistrationId("default")
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.tokenUri("http://localhost:9999/oauth/token")
				.clientId("myclient")
				.clientSecret("mysecret")
				.scope("access")
				.build();
		return new InMemoryClientRegistrationRepository(clientRegistration);
	}

	@Bean
	OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient() {
		return new DefaultClientCredentialsTokenResponseClient();
	}
}

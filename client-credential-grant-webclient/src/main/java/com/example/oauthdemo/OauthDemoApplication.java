package com.example.oauthdemo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OauthDemoApplication {

	public static void main(String[] args) {
		final ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(OauthDemoApplication.class);

		final WebClient client = ctx.getBean(WebClient.class);

		ReactiveClientRegistrationRepository clientRegistrations = ctx.getBean(ReactiveClientRegistrationRepository.class);

		ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient =
				ctx.getBean(ReactiveOAuth2AccessTokenResponseClient.class);

		final OAuth2ClientCredentialsGrantRequest grantRequest = new OAuth2ClientCredentialsGrantRequest(clientRegistrations.findByRegistrationId("default").block());

		final OAuth2AccessTokenResponse res = clientCredentialsTokenResponseClient.getTokenResponse(grantRequest).block();

		System.out.println(res.getAccessToken().getTokenValue());

		client.get()
			.uri("http://localhost:9393/about")
			.retrieve().bodyToMono(String.class)
			.map(string
			        -> "Retrieved using Client Credentials Grant Type: " + string)
			.subscribe(System.out::println);
	}

	// REACTIVE

	/**
	 * See ReactiveOAuth2ClientAutoConfiguration
	 *
	 * @return
	 */
	@Bean
	public InMemoryReactiveClientRegistrationRepository clientRegistrationRepository() {
		final ClientRegistration clientRegistration = ClientRegistration
				.withRegistrationId("default")
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.tokenUri("http://localhost:9999/oauth/token")
				.clientId("myclient")
				.clientSecret("mysecret")
				.scope("access")
				.build();
		return new InMemoryReactiveClientRegistrationRepository(clientRegistration);
	}


	@Bean
	ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient() {
		return new WebClientReactiveClientCredentialsTokenResponseClient();
	}


	@Bean
	WebClient webClient(
			ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient,
			ReactiveClientRegistrationRepository clientRegistrations) {
		ClientRegistration cr = clientRegistrations.findByRegistrationId("bael").block();
		//String scopes = StringUtils.collectionToCommaDelimitedString(cr.getScopes());

		ServerOAuth2AuthorizedClientRepository repo = new UnAuthenticatedServerOAuth2AuthorizedClientRepository();


		final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
	      new ServerOAuth2AuthorizedClientExchangeFilterFunction(
	        clientRegistrations,
	        repo);
		oauth.setClientCredentialsTokenResponseClient(clientCredentialsTokenResponseClient);
	    oauth.setDefaultClientRegistrationId("bael");
	    return WebClient.builder()
	      .filter(oauth)
	      .build();
	}

}

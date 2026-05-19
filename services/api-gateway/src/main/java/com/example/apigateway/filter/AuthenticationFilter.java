package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	private static final String[] WHITELIST = { "/user/register", "/user/login"};

	private final WebClient webClient;

	@Autowired
	public AuthenticationFilter(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("lb://user-service").build();
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		for (String allowed : WHITELIST) {
			if (path.startsWith(allowed)) {
				return chain.filter(exchange);
			}
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		return webClient.post().uri("/user/validate").header("Authorization", authHeader).retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, response -> {
					logger.warn("User-service returned 4xx for {}", path);
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return Mono.error(new RuntimeException("Unauthorized"));
				}).onStatus(HttpStatusCode::is5xxServerError, response -> {
					logger.error("User-service returned 5xx for {}", path);
					exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
					return Mono.error(new RuntimeException("Service unavailable"));
				}).bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				}).flatMap(response -> {
					Boolean valid = (Boolean) response.get("valid");
					if (valid == null || !valid) {
						exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
						return exchange.getResponse().setComplete();
					}

					String username = (String) response.get("username");
					String role = (String) response.get("role");

					ServerWebExchange modified = exchange.mutate().request(r -> r.headers(headers -> {
						headers.add("X-User-Name", username);
						headers.add("X-User-Role", role);
					})).build();

					return chain.filter(modified);
				}).onErrorResume(e -> {
					logger.error("JWT validation or user-service communication failed: {}", e.getMessage());
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				});

	}

	@Override
	public int getOrder() {
		return -1;
	}
}

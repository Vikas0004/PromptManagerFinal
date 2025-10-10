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

	private static final String[] WHITELIST = { "/user/register", "/user/login" };

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

//package com.example.apigateway.filter;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
//@Component
//public class AuthenticationFilter implements GlobalFilter, Ordered {
//
//    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
//
//    private static final String[] WHITELIST = { "/user/register", "/user/login" };
//
//    private final WebClient webClient;
//
//    @Autowired
//    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.baseUrl("lb://user-service").build();
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String path = exchange.getRequest().getURI().getPath();
//        HttpMethod method = exchange.getRequest().getMethod();
//
//        logger.info("➡️ Incoming {} request to: {}", method, path);
//
//        // Allow whitelisted endpoints
//        for (String allowed : WHITELIST) {
//            if (path.startsWith(allowed)) {
//                logger.info("🟢 Whitelisted endpoint detected ({}). Forwarding without auth.", path);
//                return chain.filter(exchange);
//            }
//        }
//
//        // Check for Authorization header
//        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            logger.warn("🔴 Missing or invalid Authorization header for {}", path);
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        logger.info("🟡 Validating JWT via user-service → POST /user/validate");
//
//        // Validate token via user-service
//        return webClient.post()
//                .uri("/user/validate")
//                .header("Authorization", authHeader)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, response -> {
//                    logger.warn("⚠️ User-service returned 4xx for {}", path);
//                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                    return Mono.error(new RuntimeException("Unauthorized"));
//                })
//                .onStatus(HttpStatusCode::is5xxServerError, response -> {
//                    logger.error("🔥 User-service returned 5xx for {}", path);
//                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
//                    return Mono.error(new RuntimeException("Service unavailable"));
//                })
//                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
//                .flatMap(response -> {
//                    Boolean valid = (Boolean) response.get("valid");
//                    if (valid == null || !valid) {
//                        logger.warn("❌ Invalid JWT for {}", path);
//                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                        return exchange.getResponse().setComplete();
//                    }
//
//                    String username = (String) response.get("username");
//                    String role = (String) response.get("role");
//
//                    logger.info("✅ JWT validated successfully. User: {}, Role: {}", username, role);
//                    logger.info("➡️ Forwarding {} request to target service: {}", method, path);
//
//                    ServerWebExchange modified = exchange.mutate().request(r -> r.headers(headers -> {
//                        headers.add("X-User-Name", username);
//                        headers.add("X-User-Role", role);
//                    })).build();
//
//                    return chain.filter(modified);
//                })
//                .onErrorResume(e -> {
//                    logger.error("❌ JWT validation or user-service communication failed: {}", e.getMessage());
//                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                    return exchange.getResponse().setComplete();
//                });
//    }
//
//    @Override
//    public int getOrder() {
//        return -1;
//    }
//}
//
//package com.example.apigateway.filter;
//
//import java.net.URI;
//import java.util.Map;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
//import org.springframework.core.Ordered;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
//public class AuthenticationFilter implements GlobalFilter, Ordered {
//
//	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
//
//	private static final String[] WHITELIST = { "/user/register", "/user/login" };
//
//	private final WebClient webClient;
//
//	@Autowired
//	public AuthenticationFilter(WebClient.Builder webClientBuilder) {
//		this.webClient = webClientBuilder.baseUrl("lb://user-service").build();
//	}
//
//	@Override
//	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//		String path = exchange.getRequest().getURI().getPath();
//		HttpMethod method = exchange.getRequest().getMethod();
//
//		logger.info("➡️ Incoming {} request to: {}", method, path);
//
//		// Allow whitelisted endpoints
//		for (String allowed : WHITELIST) {
//			if (path.startsWith(allowed)) {
//				logger.info("🟢 Whitelisted endpoint detected ({}). Forwarding without auth.", path);
//				return chain.filter(exchange);
//			}
//		}
//
//		// Allow preflight OPTIONS through (don't validate JWT here)
//		if (method == HttpMethod.OPTIONS) {
//			logger.info("⚪ OPTIONS preflight for {}, forwarding without auth.", path);
//			return chain.filter(exchange);
//		}
//
//		// Check for Authorization header
//		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//			logger.warn("🔴 Missing or invalid Authorization header for {}", path);
//			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//			return exchange.getResponse().setComplete();
//		}
//
//		logger.info("🟡 Validating JWT via user-service → POST /user/validate");
//
//		return webClient.post().uri("/user/validate").header("Authorization", authHeader).retrieve()
//				.onStatus(HttpStatusCode::is4xxClientError, response -> {
//					logger.warn("⚠️ User-service returned 4xx for {}", path);
//					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//					return Mono.error(new RuntimeException("Unauthorized"));
//				}).onStatus(HttpStatusCode::is5xxServerError, response -> {
//					logger.error("🔥 User-service returned 5xx for {}", path);
//					exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
//					return Mono.error(new RuntimeException("Service unavailable"));
//				}).bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
//				}).flatMap(response -> {
//					Boolean valid = (Boolean) response.get("valid");
//					if (valid == null || !valid) {
//						logger.warn("❌ Invalid JWT for {}", path);
//						exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//						return exchange.getResponse().setComplete();
//					}
//
//					String username = (String) response.get("username");
//					String role = (String) response.get("role");
//
//					logger.info("✅ JWT validated successfully. User: {}, Role: {}", username, role);
//
//					// Attach headers to the actual outgoing HTTP request
//					ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().header("X-User-Name", username)
//							.header("X-User-Role", role).build();
//
//					ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
//
//					logger.info("✅ Injected headers: X-User-Name={}, X-User-Role={}", username, role);
//
//					// Log resolved backend URI if available
//					URI resolved = mutatedExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
//					if (resolved != null) {
//						logger.info("🌐 Resolved backend URI: {}", resolved);
//					} else {
//						logger.info("🌐 Resolved backend URI not available yet.");
//					}
//
//					return chain.filter(mutatedExchange);
//				}).onErrorResume(e -> {
//					String msg = e.getMessage();
//					if (msg != null && msg.contains("405")) {
//						// benign parallel validation request; don’t treat as failure
//						logger.warn("⚠️ Ignoring benign 405 from concurrent validation request");
//						return Mono.empty();
//					}
//					logger.error("❌ JWT validation failed: {}", msg);
//					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//					return exchange.getResponse().setComplete();
//				});
//
//	}
//
//	@Override
//	public int getOrder() {
//		return -1;
//	}
//}

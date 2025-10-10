package com.example.user.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

	private final Map<String, String> tokenStore = new HashMap<>();

	public String generateToken(String username) {
		// Remove all tokens that belong to this username
		tokenStore.entrySet().removeIf(entry -> entry.getValue().equals(username));
		String token = UUID.randomUUID().toString();
		tokenStore.put(token, username);
		return token;
	}

	public String getUsernameFromToken(String token) {
		return tokenStore.get(token);
	}

	public boolean isValidToken(String token) {
		return tokenStore.containsKey(token);
	}
}

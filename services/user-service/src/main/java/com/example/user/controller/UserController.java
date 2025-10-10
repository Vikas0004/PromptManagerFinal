package com.example.user.controller;

import com.example.user.model.User;
import com.example.user.service.JwtService;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtService jwtService;

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@RequestParam String username, @RequestParam String password,
			@RequestParam String email) {
		userService.registerUser(username, password, email);
		return ResponseEntity.ok(Map.of("message", "User registered successfully", "role", "USER"));
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
		if (userService.validateUser(username, password)) {
			User user = userService.findByUsername(username).orElseThrow();
			String token = jwtService.generateToken(username, user.getRole().name());
			return ResponseEntity.ok(Map.of("token", token, "role", user.getRole().name()));
		} else {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
		}
	}

	@PostMapping("/validate")
	public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Missing token"));
		}

		String token = authHeader.substring(7);
		boolean isValid = jwtService.validateToken(token);

		if (!isValid) {
			return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Invalid or expired token"));
		}

		String username = jwtService.extractUsername(token);
		String role = jwtService.extractRole(token);

		return ResponseEntity.ok(Map.of("valid", true, "username", username, "role", role));
	}

}

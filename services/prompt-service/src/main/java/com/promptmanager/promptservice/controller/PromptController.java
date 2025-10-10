package com.promptmanager.promptservice.controller;

import com.promptmanager.promptservice.model.Prompt;
import com.promptmanager.promptservice.service.PromptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/prompts")
public class PromptController {

	private final PromptService promptService;

	public PromptController(PromptService promptService) {
		this.promptService = promptService;
	}

	// 🟢 Create prompt for logged-in user
	@PostMapping("/add")
	public ResponseEntity<?> createPrompt(@RequestBody Prompt prompt, HttpServletRequest request) {
		System.out.println("Request Recieved To Add Prompt");
		String username = request.getHeader("X-User-Name");

		if (username == null || username.isBlank()) {
			System.out.println("No Request Header Found, Rejecting Request");
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		prompt.setUserId(username);
		Prompt created = promptService.createPrompt(prompt);
		System.out.println("Prompt Created  Successfully!!");
		return ResponseEntity.ok(created);
	}

	// 🟢 Get all prompts for the logged-in user
	@GetMapping("/my")
	public ResponseEntity<?> getMyPrompts(HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		List<Prompt> prompts = promptService.getUserPrompts(username);
		return ResponseEntity.ok(prompts);
	}

	// 🟣 Get all prompts
	@GetMapping("/all")
	public ResponseEntity<?> getAllPrompts(HttpServletRequest request) {

		List<Prompt> allPrompts = promptService.getAllPrompts();
		return ResponseEntity.ok(allPrompts);
	}

	// 🔵 Get prompt details by ID
	@GetMapping("/details/{id}")
	public ResponseEntity<?> getPrompt(@PathVariable UUID id, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");

		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		Prompt prompt = promptService.getPromptById(id);
		if (prompt == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(prompt);
	}

	// 🟡 Update prompt (only owner or admin)
	@PutMapping("/{id}")
	public ResponseEntity<?> updatePrompt(@PathVariable UUID id, @RequestBody Prompt prompt,
			HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		String role = request.getHeader("X-User-Role");

		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		Prompt existing = promptService.getPromptById(id);
		if (existing == null) {
			return ResponseEntity.notFound().build();
		}

		// Ownership or admin check
		if (!existing.getUserId().equals(username) && !"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body("Access denied: you can only update your own prompts.");
		}

		prompt.setUserId(existing.getUserId()); // Preserve ownership
		Prompt updated = promptService.updatePrompt(id, prompt);
		return ResponseEntity.ok(updated);
	}

	// 🔴 Delete prompt (only owner or admin)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePrompt(@PathVariable UUID id, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		String role = request.getHeader("X-User-Role");

		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		Prompt existing = promptService.getPromptById(id);
		if (existing == null) {
			return ResponseEntity.notFound().build();
		}

		if (!existing.getUserId().equals(username) && !"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body("Access denied: you can only delete your own prompts.");
		}

		promptService.deletePrompt(id);
		return ResponseEntity.noContent().build();
	}

	// 🔍 Search within user's own prompts
	@GetMapping("/search")
	public ResponseEntity<?> searchPrompts(@RequestParam String query, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		List<Prompt> results = promptService.searchPrompts(query);
		return ResponseEntity.ok(results);
	}
}

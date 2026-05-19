package com.promptmanager.analyticsservice.controller;

import com.promptmanager.analyticsservice.model.PromptStats;
import com.promptmanager.analyticsservice.service.AnalyticsService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.promptmanager.analyticsservice.events.PromptViewedEvent;

import com.promptmanager.analyticsservice.messaging.AnalyticsEventProducer;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

	private final AnalyticsService analyticsService;
	private final AnalyticsEventProducer eventProducer;

	public AnalyticsController(AnalyticsService analyticsService, AnalyticsEventProducer eventProducer) {
		this.analyticsService = analyticsService;
		this.eventProducer = eventProducer;
	}

	// User analytics
	@GetMapping("/my")
	public ResponseEntity<?> getUserAnalytics(HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		List<PromptStats> userStats = analyticsService.getUserAnalytics(username);
		return ResponseEntity.ok(userStats);
	}

	// Global analytics (Admin only)
	@GetMapping("/global")
	public ResponseEntity<?> getGlobalAnalytics(HttpServletRequest request) {
		String role = request.getHeader("X-User-Role");
		if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body("Access denied: admin privileges required.");
		}
		return ResponseEntity.ok(analyticsService.getGlobalAnalytics());
	}

	// Increment stats
//	@PostMapping("/increment/view/{promptId}")
//	public ResponseEntity<?> incrementView(@PathVariable UUID promptId, HttpServletRequest request) {
//		String username = request.getHeader("X-User-Name");
//		if (username == null || username.isBlank()) {
//			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
//		}
//		return ResponseEntity.ok(analyticsService.recordView(promptId, username));
//	}

	
	//Now uses RabbiitMQ
	@PostMapping("/increment/view/{promptId}")
	public ResponseEntity<?> incrementView(@PathVariable UUID promptId, HttpServletRequest request) {

		String username = request.getHeader("X-User-Name");

		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}

		PromptViewedEvent event = new PromptViewedEvent(promptId, username);

		eventProducer.publishViewEvent(event);

		return ResponseEntity.accepted().body("View event published successfully");
	}

	@PostMapping("/increment/copy/{promptId}")
	public ResponseEntity<?> incrementCopy(@PathVariable UUID promptId, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		return ResponseEntity.ok(analyticsService.recordCopy(promptId, username));
	}

	@PostMapping("/increment/favorite/{promptId}")
	public ResponseEntity<?> incrementFavorite(@PathVariable UUID promptId, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		return ResponseEntity.ok(analyticsService.recordFavorite(promptId, username));
	}

	@PostMapping("/decrement/favorite/{promptId}")
	public ResponseEntity<?> decrementFavorite(@PathVariable UUID promptId, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		return ResponseEntity.ok(analyticsService.decrementFavorite(promptId, username));
	}

	@DeleteMapping("/delete/{promptId}")
	public ResponseEntity<Map<String, Object>> deletePrompt(@PathVariable UUID promptId, HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("code", 401, "status", "error", "message", "Unauthorized: missing username header."));
		}
		try {
			analyticsService.deletePromptStats(promptId);
			return ResponseEntity.ok(Map.of("code", 200, "status", "success", "message", "Deleted successfully"));
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("code", 404, "status", "error", "message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("code", 500, "status", "error", "message", "Unexpected error during deletion"));
		}
	}

	@GetMapping("/check-favorite/{promptId}")
	public ResponseEntity<Map<String, Object>> checkIfFavorited(@PathVariable UUID promptId) {
		try {
			boolean isFavorited = analyticsService.isPromptFavorited(promptId);

			if (isFavorited) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(Map.of("code", 409, "status", "error", "canDelete", false, "message",
								"Cannot delete prompt. It is marked as favorite by one or more users."));
			}

			return ResponseEntity.ok(Map.of("code", 200, "status", "success", "canDelete", true, "message",
					"Prompt can be safely deleted."));
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("code", 404, "status", "error", "canDelete", false, "message", e.getMessage()));
		}
	}

	// Most viewed / favorited / copied
	@GetMapping("/most-viewed")
	public ResponseEntity<?> mostViewed(HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		String role = request.getHeader("X-User-Role");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		if ("ADMIN".equalsIgnoreCase(role))
			return ResponseEntity.ok(analyticsService.getMostViewedPromptsGlobal());
		else
			return ResponseEntity.ok(analyticsService.getMostViewedPromptsForUser(username));
	}

	@GetMapping("/most-favorited")
	public ResponseEntity<?> mostFavorited(HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		String role = request.getHeader("X-User-Role");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		if ("ADMIN".equalsIgnoreCase(role))
			return ResponseEntity.ok(analyticsService.getMostFavoritedPromptsGlobal());
		else
			return ResponseEntity.ok(analyticsService.getMostFavoritedPromptsForUser(username));
	}

	@GetMapping("/most-copied")
	public ResponseEntity<?> mostCopied(HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		String role = request.getHeader("X-User-Role");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		if ("ADMIN".equalsIgnoreCase(role))
			return ResponseEntity.ok(analyticsService.getMostCopiedPromptsGlobal());
		else
			return ResponseEntity.ok(analyticsService.getMostCopiedPromptsForUser(username));
	}

	@GetMapping("/summary")
	public ResponseEntity<?> getSummary(HttpServletRequest request) {
		String role = request.getHeader("X-User-Role");

		// Only ADMINs should access the full global summary
		if (!"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body("Access denied: admin role required.");
		}

		Map<String, Object> summary = analyticsService.getSummary();
		return ResponseEntity.ok(summary);
	}

	@GetMapping("/favorites")
	public ResponseEntity<?> getUserFavorites(HttpServletRequest request) {
		String username = request.getHeader("X-User-Name");
		if (username == null || username.isBlank()) {
			return ResponseEntity.status(401).body("Unauthorized: missing username header.");
		}
		return ResponseEntity.ok(analyticsService.getUserFavorites(username));
	}

}

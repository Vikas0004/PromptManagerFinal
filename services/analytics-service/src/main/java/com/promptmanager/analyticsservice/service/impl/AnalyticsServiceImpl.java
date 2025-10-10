package com.promptmanager.analyticsservice.service.impl;

import com.promptmanager.analyticsservice.dto.PromptSummaryDTO;
import com.promptmanager.analyticsservice.model.PromptStats;
import com.promptmanager.analyticsservice.repository.PromptStatsRepository;
import com.promptmanager.analyticsservice.service.AnalyticsService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

	private final PromptStatsRepository repository;

	public AnalyticsServiceImpl(PromptStatsRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<PromptStats> getUserAnalytics(String username) {
		// TODO: integrate user ownership mapping once prompt-service exposes userId
		return repository.findAll();
	}

	@Override
	public List<PromptStats> getGlobalAnalytics() {
		return repository.findAll();
	}

	@Override
	public List<PromptStats> getMostViewedPromptsForUser(String username) {
		// TODO: Filter by user's prompts once prompt-service user link exists
		return repository.findMostViewedByUser(username);
	}

	@Override
	public List<PromptSummaryDTO> getMostViewedPromptsGlobal() {
		return repository.findMostViewed();
	}

	@Override
	public List<PromptStats> getMostFavoritedPromptsForUser(String username) {
		// TODO: Filter by user's prompts once prompt-service user link exists
		return repository.findMostFavoriteByUser(username);
	}

	@Override
	public List<PromptSummaryDTO> getMostFavoritedPromptsGlobal() {
		return repository.findMostFavorited();
	}

	@Override
	public List<PromptStats> getMostCopiedPromptsForUser(String username) {
		// TODO: Filter by user's prompts once prompt-service user link exists
		return repository.findMostCopiedByUser(username);
	}

	@Override
	public List<PromptSummaryDTO> getMostCopiedPromptsGlobal() {
		return repository.findMostCopied();
	}

	@Override
	public PromptStats recordView(UUID promptId, String userName) {
		PromptStats stats = repository.findByPromptIdAndUserId(promptId, userName);
		if (stats == null) {
			stats = new PromptStats();
			stats.setPromptId(promptId);
			stats.setUserId(userName);
		}
		stats.setViews(stats.getViews() + 1);
		return repository.save(stats);
	}

	@Override
	public PromptStats recordCopy(UUID promptId, String userName) {
		PromptStats stats = repository.findByPromptIdAndUserId(promptId, userName);
		if (stats == null) {
			stats = new PromptStats();
			stats.setPromptId(promptId);
			stats.setUserId(userName);
		}
		stats.setCopies(stats.getCopies() + 1);
		return repository.save(stats);
	}

	@Override
	public PromptStats decrementFavorite(UUID promptId, String userName) {
		PromptStats stats = repository.findByPromptIdAndUserId(promptId, userName);
		if (stats == null) {
			return null;
		}
		stats.setFavorites(stats.getFavorites() - 1);
		return repository.save(stats);
	}

	@Override
	public PromptStats recordFavorite(UUID promptId, String userName) {
		PromptStats stats = repository.findByPromptIdAndUserId(promptId, userName);
		if (stats == null) {
			stats = new PromptStats();
			stats.setPromptId(promptId);
			stats.setUserId(userName);
		}
		stats.setFavorites(stats.getFavorites() + 1);
		return repository.save(stats);
	}

	@Override
	public Map<String, Object> getSummary() {
		Map<String, Object> summary = new java.util.HashMap<>();

		var mostViewed = repository.findMostViewed().stream().limit(5)
				.map(p -> Map.of("promptId", p.getPromptId(), "views", p.getViews())).toList();

		var mostFavorited = repository.findMostFavorited().stream().limit(5)
				.map(p -> Map.of("promptId", p.getPromptId(), "favorites", p.getFavorites())).toList();

		var mostCopied = repository.findMostCopied().stream().limit(5)
				.map(p -> Map.of("promptId", p.getPromptId(), "copies", p.getCopies())).toList();

		summary.put("mostViewed", mostViewed);
		summary.put("mostFavorited", mostFavorited);
		summary.put("mostCopied", mostCopied);

		return summary;
	}

	@Override
	public List<UUID> getUserFavorites(String username) {
		return repository.findAllByUserIdAndFavoritesGreaterThan(username, 0).stream().map(PromptStats::getPromptId)
				.toList();
	}

	@Override
	public void deletePromptStats(UUID promptId) {

		repository.deleteByPromptIdSafe(promptId);

	}

	@Override
	public boolean isPromptFavorited(UUID promptId) {
		List<PromptStats> statsList = repository.findAllByPromptId(promptId);

		if (statsList == null || statsList.isEmpty()) {
			return false;
		}

		return statsList.stream().anyMatch(stats -> stats.getFavorites() > 0);
	}

}

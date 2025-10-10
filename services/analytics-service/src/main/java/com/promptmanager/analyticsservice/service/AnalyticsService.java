package com.promptmanager.analyticsservice.service;

import com.promptmanager.analyticsservice.dto.PromptSummaryDTO;
import com.promptmanager.analyticsservice.model.PromptStats;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AnalyticsService {
	List<PromptStats> getUserAnalytics(String username);

	List<PromptStats> getGlobalAnalytics();

	List<PromptStats> getMostViewedPromptsForUser(String username);

	List<PromptSummaryDTO> getMostViewedPromptsGlobal();

	List<PromptStats> getMostFavoritedPromptsForUser(String username);

	List<PromptSummaryDTO> getMostFavoritedPromptsGlobal();

	List<PromptStats> getMostCopiedPromptsForUser(String username);

	List<PromptSummaryDTO> getMostCopiedPromptsGlobal();

	PromptStats recordView(UUID promptId, String userName);

	PromptStats recordCopy(UUID promptId, String userName);

	PromptStats recordFavorite(UUID promptId, String userName);

	PromptStats decrementFavorite(UUID promptId, String userName);

	void deletePromptStats(UUID promptId);

	Map<String, Object> getSummary();

	List<UUID> getUserFavorites(String username);

	boolean isPromptFavorited(UUID promptId);
}

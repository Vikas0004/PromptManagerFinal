package com.promptmanager.analyticsservice.dto;

import java.util.UUID;

public class PromptSummaryDTO {
	private UUID promptId;
	private long favorites;
	private long views;
	private long copies;

	public PromptSummaryDTO(UUID promptId, long favorites, long views, long copies) {
		this.promptId = promptId;
		this.favorites = favorites;
		this.views = views;
		this.copies = copies;
	}

	public UUID getPromptId() {
		return promptId;
	}

	public long getFavorites() {
		return favorites;
	}

	public long getViews() {
		return views;
	}

	public long getCopies() {
		return copies;
	}
}

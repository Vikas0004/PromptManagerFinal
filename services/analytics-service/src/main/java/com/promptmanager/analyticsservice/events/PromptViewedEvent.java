package com.promptmanager.analyticsservice.events;

import java.io.Serializable;
import java.util.UUID;

public class PromptViewedEvent implements Serializable {

	private UUID promptId;

	private String username;

	public PromptViewedEvent() {
	}

	public PromptViewedEvent(UUID promptId, String username) {
		this.promptId = promptId;
		this.username = username;
	}

	public UUID getPromptId() {
		return promptId;
	}

	public void setPromptId(UUID promptId) {
		this.promptId = promptId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
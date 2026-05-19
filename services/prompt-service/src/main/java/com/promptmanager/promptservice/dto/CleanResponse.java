package com.promptmanager.promptservice.dto;

public class CleanResponse {
	private String cleanedText;
	private double duplicateScore;

	public String getCleanedText() {
		return cleanedText;
	}

	public void setCleanedText(String cleanedText) {
		this.cleanedText = cleanedText;
	}

	public double getDuplicateScore() {
		return duplicateScore;
	}

	public void setDuplicateScore(double duplicateScore) {
		this.duplicateScore = duplicateScore;
	}
}

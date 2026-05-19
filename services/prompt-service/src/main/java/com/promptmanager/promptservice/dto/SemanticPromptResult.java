package com.promptmanager.promptservice.dto;

import com.promptmanager.promptservice.model.Prompt;

public class SemanticPromptResult {

	private Prompt prompt;
	private double similarityScore;

	public SemanticPromptResult() {
	}

	public SemanticPromptResult(Prompt prompt, double similarityScore) {
		this.prompt = prompt;
		this.similarityScore = similarityScore;
	}

	public Prompt getPrompt() {
		return prompt;
	}

	public void setPrompt(Prompt prompt) {
		this.prompt = prompt;
	}

	public double getSimilarityScore() {
		return similarityScore;
	}

	public void setSimilarityScore(double similarityScore) {
		this.similarityScore = similarityScore;
	}
}

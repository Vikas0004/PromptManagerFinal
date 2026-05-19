package com.promptmanager.promptservice.dto;

import java.util.List;

public class EmbedResponse {
	private List<Double> embedding;

	public List<Double> getEmbedding() {
		return embedding;
	}

	public void setEmbedding(List<Double> embedding) {
		this.embedding = embedding;
	}

}

package com.promptmanager.promptservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.promptmanager.promptservice.dto.CleanRequest;
import com.promptmanager.promptservice.dto.CleanResponse;
import com.promptmanager.promptservice.dto.EmbedRequest;
import com.promptmanager.promptservice.dto.EmbedResponse;
import com.promptmanager.promptservice.dto.PredictRequest;
import com.promptmanager.promptservice.dto.PredictResponse;

@Service
public class AIIntegrationService {

	private final WebClient webClient;

	public AIIntegrationService(WebClient webClient) {
		this.webClient = webClient;
	}

	public CleanResponse cleanPrompt(String title, String description) {

		CleanRequest request = new CleanRequest();
		request.setTitle(title);
		request.setDescription(description);

		return webClient.post().uri("http://preprocessing-service:8001/clean").bodyValue(request).retrieve()
				.bodyToMono(CleanResponse.class).block();
	}

	public PredictResponse predictTool(String text) {

		PredictRequest request = new PredictRequest();
		request.setText(text);

		return webClient.post().uri("http://ml-service:8002/predict").bodyValue(request).retrieve()
				.bodyToMono(PredictResponse.class).block();
	}

	public EmbedResponse generateEmbedding(String text) {

		EmbedRequest request = new EmbedRequest();
		request.setText(text);

		return webClient.post().uri("http://ml-service:8002/embed").bodyValue(request).retrieve()
				.bodyToMono(EmbedResponse.class).block();
	}

	public List<Double> generateEmbeddingVector(String text) {

		EmbedRequest request = new EmbedRequest();
		request.setText(text);

		EmbedResponse response = webClient.post().uri("http://ml-service:8002/embed").bodyValue(request).retrieve()
				.bodyToMono(EmbedResponse.class).block();

		return response.getEmbedding();
	}
}
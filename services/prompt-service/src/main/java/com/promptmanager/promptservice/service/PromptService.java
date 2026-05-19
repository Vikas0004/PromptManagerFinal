package com.promptmanager.promptservice.service;

import com.promptmanager.promptservice.dto.CleanResponse;
import com.promptmanager.promptservice.dto.EmbedResponse;
import com.promptmanager.promptservice.dto.PredictResponse;
import com.promptmanager.promptservice.dto.SemanticPromptResult;
import com.promptmanager.promptservice.model.Prompt;
import com.promptmanager.promptservice.repository.PromptRepository;
import com.promptmanager.promptservice.util.EmbeddingUtils;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PromptService {

	private final PromptRepository promptRepository;
	private final AIIntegrationService aiIntegrationService;

	public PromptService(PromptRepository promptRepository, AIIntegrationService aiIntegrationService) {
		this.promptRepository = promptRepository;
		this.aiIntegrationService = aiIntegrationService;
	}

	public Prompt createPrompt(Prompt prompt) {

		enrichPromptWithAI(prompt);

		return promptRepository.save(prompt);
	}

	public List<Prompt> getAllPrompts() {
		return promptRepository.findAll();
	}

	public List<Prompt> getUserPrompts(String userId) {
		return promptRepository.findByUserId(userId);
	}

	public Prompt getPromptById(UUID id) {

		return promptRepository.findById(id).orElseThrow(() -> new RuntimeException("Prompt not found"));
	}

	public Prompt updatePrompt(UUID id, Prompt updatedPrompt) {

		Prompt existing = getPromptById(id);

		existing.setTitle(updatedPrompt.getTitle());
		existing.setDescription(updatedPrompt.getDescription());
		existing.setAiTool(updatedPrompt.getAiTool());

		enrichPromptWithAI(existing);

		return promptRepository.save(existing);
	}

	public void deletePrompt(UUID id) {
		promptRepository.deleteById(id);
	}

	public List<Prompt> searchPrompts(String query) {

		return promptRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
	}

	public List<Prompt> searchPromptsForUser(String query, String userId) {

		return promptRepository.findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(userId, query,
				query);
	}

	private void enrichPromptWithAI(Prompt prompt) {

		CleanResponse cleanResponse = aiIntegrationService.cleanPrompt(prompt.getTitle(), prompt.getDescription());

		PredictResponse predictResponse = aiIntegrationService.predictTool(cleanResponse.getCleanedText());

		EmbedResponse embedResponse = aiIntegrationService.generateEmbedding(cleanResponse.getCleanedText());

		prompt.setCleanedPrompt(cleanResponse.getCleanedText());

		prompt.setPredictedAiTool(predictResponse.getPredictedTool());

		prompt.setPredictionConfidence(predictResponse.getConfidence());

		prompt.setEmbeddingVector(embedResponse.getEmbedding().toString());
	}

	public List<SemanticPromptResult> semanticSearch(String query, String userId) {

		List<Double> queryEmbedding = aiIntegrationService.generateEmbeddingVector(query);

		List<Prompt> prompts = promptRepository.findByUserId(userId);

		List<SemanticPromptResult> results = prompts.stream().map(prompt -> {

			List<Double> storedEmbedding = EmbeddingUtils.parseEmbedding(prompt.getEmbeddingVector());

			double similarity = EmbeddingUtils.cosineSimilarity(queryEmbedding, storedEmbedding);

			return new SemanticPromptResult(prompt, similarity);
		})

				.sorted(Comparator.comparingDouble(SemanticPromptResult::getSimilarityScore).reversed())

				.limit(10)

				.collect(Collectors.toList());

		return results;
	}
}
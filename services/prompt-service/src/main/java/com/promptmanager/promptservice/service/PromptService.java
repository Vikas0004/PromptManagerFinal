package com.promptmanager.promptservice.service;

import com.promptmanager.promptservice.model.Prompt;
import com.promptmanager.promptservice.repository.PromptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PromptService {

	private final PromptRepository promptRepository;

	public PromptService(PromptRepository promptRepository) {
		this.promptRepository = promptRepository;
	}

	public Prompt createPrompt(Prompt prompt) {
		return promptRepository.save(prompt);
	}

	public List<Prompt> getAllPrompts() {
		return promptRepository.findAll();
	}

	public List<Prompt> getUserPrompts(String userId) {
		return promptRepository.findByUserId(userId);
	}

	public Prompt getPromptById(UUID id) {
		Prompt prompt = promptRepository.findById(id).orElseThrow(() -> new RuntimeException("Prompt not found"));

		return prompt;
	}

	public Prompt updatePrompt(UUID id, Prompt updatedPrompt) {
		Prompt existing = getPromptById(id);
		existing.setTitle(updatedPrompt.getTitle());
		existing.setDescription(updatedPrompt.getDescription());
		existing.setAiTool(updatedPrompt.getAiTool());
		Prompt saved = promptRepository.save(existing);

		return saved;
	}

	public void deletePrompt(UUID id) {
		promptRepository.deleteById(id);
	}

	public List<Prompt> searchPrompts(String query) {
		return promptRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
	}

	// 🆕 Add user-scoped search for security
	public List<Prompt> searchPromptsForUser(String query, String userId) {
		return promptRepository.findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(userId, query,
				query);
	}

}

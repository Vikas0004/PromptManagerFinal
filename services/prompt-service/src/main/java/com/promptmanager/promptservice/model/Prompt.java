package com.promptmanager.promptservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prompts")
public class Prompt {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String aiTool;

    @Column(columnDefinition = "TEXT")
    private String cleanedPrompt;

    private String predictedAiTool;

    private Double predictionConfidence;

    @Column(columnDefinition = "TEXT")
    private String embeddingVector;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAiTool() {
		return aiTool;
	}

	public void setAiTool(String aiTool) {
		this.aiTool = aiTool;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCleanedPrompt() {
		return cleanedPrompt;
	}

	public void setCleanedPrompt(String cleanedPrompt) {
		this.cleanedPrompt = cleanedPrompt;
	}

	public String getPredictedAiTool() {
		return predictedAiTool;
	}

	public void setPredictedAiTool(String predictedAiTool) {
		this.predictedAiTool = predictedAiTool;
	}

	public Double getPredictionConfidence() {
		return predictionConfidence;
	}

	public void setPredictionConfidence(Double predictionConfidence) {
		this.predictionConfidence = predictionConfidence;
	}

	public String getEmbeddingVector() {
		return embeddingVector;
	}

	public void setEmbeddingVector(String embeddingVector) {
		this.embeddingVector = embeddingVector;
	}
}

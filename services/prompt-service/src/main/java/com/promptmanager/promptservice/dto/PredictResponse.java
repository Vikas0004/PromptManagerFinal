package com.promptmanager.promptservice.dto;

public class PredictResponse {
	private String predictedTool;
    private double confidence;
	public String getPredictedTool() {
		return predictedTool;
	}
	public void setPredictedTool(String predictedTool) {
		this.predictedTool = predictedTool;
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
}

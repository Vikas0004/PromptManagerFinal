package com.promptmanager.promptservice.util;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingUtils {

	public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {

		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.size(); i++) {

			dotProduct += vectorA.get(i) * vectorB.get(i);

			normA += Math.pow(vectorA.get(i), 2);

			normB += Math.pow(vectorB.get(i), 2);
		}

		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public static List<Double> parseEmbedding(String embeddingString) {

		embeddingString = embeddingString.replace("[", "").replace("]", "");

		String[] parts = embeddingString.split(",");

		List<Double> vector = new ArrayList<>();

		for (String part : parts) {
			vector.add(Double.parseDouble(part.trim()));
		}

		return vector;
	}
}
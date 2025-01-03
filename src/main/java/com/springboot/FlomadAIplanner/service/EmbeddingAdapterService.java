package com.springboot.FlomadAIplanner.service;

import java.io.IOException;

public class EmbeddingAdapterService {
    private final double[][] weights; // Adapter weight matrix
    private static final String LOAD_ERROR_MESSAGE = "Failed to load embedding adapter weights from file";

    public EmbeddingAdapterService(double[][] weights) {
        this.weights = weights;
    }

    public double[] adapt(double[] embedding) {
        return applyWeights(embedding, weights);
    }

    private double[] applyWeights(double[] embedding, double[][] weights) {
        double[] adapted = new double[weights.length]; // Output size depends on rows
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                adapted[i] += weights[i][j] * embedding[j];
            }
        }
        return adapted;
    }

    public static <EmbeddingAdapter> EmbeddingAdapter loadAdapter(String filePath) throws IOException {
        throw new IOException(LOAD_ERROR_MESSAGE); // Placeholder for future implementation
    }
}
package com.springboot.FlomadAIplanner.service;

import java.io.IOException;
import java.util.List;

import static org.apache.catalina.realm.MessageDigestCredentialHandler.DEFAULT_ITERATIONS;

public class SentenceEmbeddingGenerator {
    private static final int MIN_WORD_FREQUENCY = 5;
    private static final int ITERATIONS = 1;
    private static final int EMBEDDING_DIMENSION = 100;
    private static final int WINDOW_SIZE = 5;
    private static final long RANDOM_SEED = 42;

    private final WordVectors word2Vec;

    public SentenceEmbeddingGenerator(String corpusPath) throws IOException {
        this.word2Vec = initializeAndTrainModel(corpusPath);
    }

    private static double applyAsDouble(double a, double b) {
        for (int i = 0; i < a; i++) {
            a += b;
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private <wordVectors> wordVectors initializeAndTrainModel(String corpusPath) throws IOException {
        return (wordVectors) new Builder()
                .minWordFrequency(MIN_WORD_FREQUENCY)
                .iterations(DEFAULT_ITERATIONS)
                .layerSize(EMBEDDING_DIMENSION)
                .seed(RANDOM_SEED)
                .windowSize(WINDOW_SIZE)
                .iterate(new LineSentenceIterator(corpusPath))
                .tokenizerFactory(new DefaultTokenizerFactory());
    }

    public double[] getSentenceEmbedding(List<String> tokens) {
        // Validate input tokens
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Input tokens cannot be null or empty");
        }

        // Convert tokens list to array
        String[] tokenArray = tokens.toArray(new String[0]);

        try {
            // Call the Word2Vec implementation to calculate embeddings
            final double[] embeddingArray = new double[EMBEDDING_DIMENSION];
            return tokens.stream()
                    .filter(word2Vec::hasWord)
                    .mapToDouble(word2Vec::getWordVector)
                    .toArray();
        } catch (Exception e) {
            // Handle exceptions gracefully
            throw new RuntimeException("Failed to compute sentence embedding", e);
        }
    }

    private static class Builder {
        public Builder minWordFrequency(int minWordFrequency) {
            return this;
        }

        public Builder iterations(int iterations) {
            return this;
        }

        public Builder layerSize(int layerSize) {
            return this;
        }

        public Builder seed(long seed) {
            return this;
        }

        public Builder windowSize(int windowSize) {
            return this;
        }

        public Builder iterate(Object iterator) {
            return this;
        }

        public Builder tokenizerFactory(Object tokenizerFactory) {
            return this;
        }

        public WordVectors build() {
            return new WordVectors();
        }
    }

    private static class WordVectors {
        public boolean hasWord(String word) {
            return false;
        }

        public double getWordVector(String word) {
            return 0;
        }
    }
}
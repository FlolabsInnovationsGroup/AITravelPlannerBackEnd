package com.springboot.FlomadAIplanner.config;

import ai.onnxruntime.OrtException;
import com.springboot.FlomadAIplanner.repositories.LuceneIndexer;
import com.springboot.FlomadAIplanner.repositories.LuceneSearcher;
import com.springboot.FlomadAIplanner.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FloBotConfig {

    @Bean
    public LuceneIndexer luceneIndexer() throws IOException {
        LuceneIndexer indexer = new LuceneIndexer();
        // Load and index documents
        indexer.close();
        return indexer;
    }

    @Bean
    public LuceneSearcher luceneSearcher(LuceneIndexer indexer) throws IOException {
        return new LuceneSearcher(indexer.getIndexDirectory());
    }

    @Bean
    public QueryExpanderService queryExpanderService() throws Exception {
        return new QueryExpanderService();
    }

    @Bean
    public SentenceEmbeddingGenerator sentenceEmbeddingGenerator() throws IOException {
        return new SentenceEmbeddingGenerator("src/corpus.txt");
    }

    @Bean
    public UMAPReducerService umapReducerService() {
        return new UMAPReducerService();
    }

    @Bean
    public CrossEncoderRerankerService crossEncoderRerankerService() throws OrtException, OrtException {
        return new CrossEncoderRerankerService();
    }

    @Bean
    public TextGenerationService textGenerationService() throws OrtException, IOException {
        return new TextGenerationService();
    }

    @Bean
    public EmbeddingAdapterService embeddingAdapterService() {
        double[][] weights = loadAdapterWeights(); // Implement loading logic
        return new EmbeddingAdapterService(weights);
    }

    private double[][] loadAdapterWeights() {
        // Implement adapter weight loading
        return new double[50][100]; // Example dimensions
    }
}

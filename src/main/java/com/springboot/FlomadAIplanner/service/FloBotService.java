package com.springboot.FlomadAIplanner.service;
import com.springboot.FlomadAIplanner.repositories.LuceneSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FloBotService {

    @Autowired
    private QueryExpanderService queryExpander;

    @Autowired
    private SentenceEmbeddingGenerator embeddingGenerator;

    @Autowired
    private UMAPReducerService umapReducer;

    @Autowired
    private EmbeddingAdapterService embeddingAdapter;

    @Autowired
    private LuceneSearcher luceneSearcher;

    @Autowired
    private CrossEncoderRerankerService reranker;

    @Autowired
    private TextGenerationService textGenerator;

    public String generateTravelPlan(String query) throws Exception {
        // Step 1: Query Expansion
        String expandedQuery = queryExpander.expandQuery(query);

        // Step 2: Embedding Generation
        double[] queryEmbedding = embeddingGenerator.getSentenceEmbedding(Collections.singletonList(expandedQuery));

        // Step 3: Dimensionality Reduction
        double[][] reducedQuery = umapReducer.reduce(new double[][]{queryEmbedding});
        double[] finalQueryEmbedding = reducedQuery[0];

        // Step 4: Adapt Embedding
        double[] adaptedQuery = embeddingAdapter.adapt(finalQueryEmbedding);

        // Step 5: Retrieval
        TopDocs topDocs = luceneSearcher.search(adaptedQuery, 10);
        List<String> passages = new ArrayList<>();
        for (var sd : topDocs.scoreDocs) {
            Document doc = luceneSearcher.getDocument(sd.doc);
            passages.add(doc.get("content"));
        }

        // Step 6: Re-ranking
        List<String> rankedPassages = reranker.rerank(expandedQuery, passages);

        // Step 7: Generation
        String combinedPassages = String.join(" ", rankedPassages.subList(0, Math.min(3, rankedPassages.size())));
        String prompt = "Based on the following information: " + combinedPassages + "\nProvide a travel plan for: " + query;

        return textGenerator.generate(prompt);
    }
}

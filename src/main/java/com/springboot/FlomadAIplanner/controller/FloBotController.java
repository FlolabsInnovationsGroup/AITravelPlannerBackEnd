package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.repositories.LuceneSearcher;
import com.springboot.FlomadAIplanner.service.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/travel-planner")
public class FloBotController {

    @Autowired
    private LuceneSearcher searcher;

    @Autowired
    private SentenceEmbeddingGenerator embeddingGenerator;

    @Autowired
    private UMAPReducerService umapReducer;

    @Autowired
    private CrossEncoderRerankerService reranker;

    @Autowired
    private TextGenerationService textGenerator;

    @Autowired
    private EmbeddingAdapterService adapter;

    public FloBotController(LuceneSearcher searcher, QueryExpander queryExpander) {
        this.searcher = searcher;
    }

    @GetMapping("/plan")
    public String getTravelPlan(@RequestParam String query) throws Exception {
        // Step 1: Query Expansion
        String expandedUserQuery = expandQuery(query);

        // Step 2: Embedding Generation
        String expandedQuery = "";
        double[] queryEmbedding = embeddingGenerator.getSentenceEmbedding(Collections.singletonList(expandedQuery));

        // Step 3: Dimensionality Reduction
        double[][] reducedQuery = umapReducer.reduce(new double[][]{queryEmbedding});
        double[] finalQueryEmbedding = reducedQuery[0];

        // Step 4: Adapt Embedding
        double[] adaptedQuery = adapter.adapt(finalQueryEmbedding);

        // Step 5: Retrieval
        TopDocs topDocs = searcher.search(adaptedQuery, 10);
        List<String> passages = new ArrayList<>();
        for (ScoreDoc sd : topDocs.scoreDocs) {
            Document doc = searcher.getDocument(sd.doc);
            passages.add(doc.get("content"));
        }

        // Step 6: Re-ranking
        List<String> rankedPassages = reranker.rerank(expandedQuery, passages);

        // Step 7: Generation
        String combinedPassages = String.join(" ", rankedPassages.subList(0, Math.min(3, rankedPassages.size())));
        String prompt = "Based on the following information: " + combinedPassages + "\nProvide a travel plan for: " + query;

        return textGenerator.generate(prompt);
    }

    private String expandQuery(String query) {
        return query;
    }
}

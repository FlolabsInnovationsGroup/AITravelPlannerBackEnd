package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.repositories.LuceneSearcher;
import com.springboot.FlomadAIplanner.service.*;

import ai.onnxruntime.OrtException;
import io.jsonwebtoken.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
    
        public QueryBuilder queryExpander;
        
            public QueryBuilder getQueryExpander() {
            return queryExpander;
        }

        public void setQueryExpander(QueryBuilder queryExpander) {
            this.queryExpander = queryExpander;
        }

            public FloBotController(LuceneSearcher searcher) {
                this.searcher = searcher;
    }

    @GetMapping("/plan")
    public String getTravelPlan(@RequestParam String query) throws IOException, java.io.IOException, OrtException, QueryNodeException {
        // Step 1: Query Expansion
        QueryNode expandedUserQuery = expandQuery(query);
                String expandedQuery = queryExpander.build(expandedUserQuery).toString();
        
        
                // Step 2: Embedding Generation
                String expandQuery = "";
                double[] queryEmbedding = embeddingGenerator.getSentenceEmbedding(Collections.singletonList(expandQuery));
        
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
        
        
        private QueryNode expandQuery(String query) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'expandQuery'");
        }
        
        private QueryNode expandQuery(QueryNode query) {
            return query;
        }
}

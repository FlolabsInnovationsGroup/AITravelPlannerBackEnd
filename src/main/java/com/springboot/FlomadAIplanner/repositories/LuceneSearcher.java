package com.springboot.FlomadAIplanner.repositories;

import lombok.Getter;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;

import java.io.IOException;

public class LuceneSearcher {
    private final IndexSearcher indexSearcher;
    @Getter
    private double[] queryEmbedding;

    public LuceneSearcher(Directory directory) throws IOException {
        IndexReader indexReader = DirectoryReader.open(directory);
        this.indexSearcher = new IndexSearcher(indexReader);
    }

    public TopDocs search(double[] queryEmbedding, int topK) throws IOException {
        this.queryEmbedding = queryEmbedding;
        // Implement a custom similarity function or use a script query
        // Placeholder: Return all documents; implement actual vector similarity
        MatchAllDocsQuery query = new MatchAllDocsQuery();
        return indexSearcher.search(query, topK);
    }

    public Document getDocument(int docId) throws IOException {
        return indexSearcher.doc(docId);
    }

}
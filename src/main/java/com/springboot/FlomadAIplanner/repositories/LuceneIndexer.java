package com.springboot.FlomadAIplanner.repositories;

import com.springboot.FlomadAIplanner.model.TravelDocument;
import lombok.Getter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class LuceneIndexer {
    @Getter
    private final Directory indexDirectory;
    private final IndexWriter indexWriter;

    public LuceneIndexer() throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        indexDirectory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(indexDirectory, config);
    }

    public void indexDocument(TravelDocument doc) throws IOException {
        Document luceneDoc = new Document();
        luceneDoc.add(new StringField("id", doc.getId(), Field.Store.YES));
        luceneDoc.add(new TextField("title", doc.getTitle(), Field.Store.YES));
        luceneDoc.add(new TextField("content", doc.getContent(), Field.Store.YES));
        luceneDoc.add(new TextField("tags", String.join(" ", doc.getTags()), Field.Store.YES));
        indexWriter.addDocument(luceneDoc);
    }

    public void close() throws IOException {
        indexWriter.close();
    }


}
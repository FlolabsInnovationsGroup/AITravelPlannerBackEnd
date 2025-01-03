package com.springboot.FlomadAIplanner.service;

import net.sf.extjwnl.dictionary.Dictionary;

import java.util.HashSet;
import java.util.Set;

public class QueryExpanderService {
    private final Dictionary dictionary;

    public QueryExpanderService() throws Exception {
        dictionary = Dictionary.getDefaultResourceInstance();
    }

    public String expandQuery(String query) throws Exception {
        String[] terms = query.split("\\s+");
        Set<String> expandedTerms = new HashSet<>();
        for (String term : terms) {
            expandedTerms.add(term);
            // Retrieve synonyms
            var synSets = dictionary.lookupAllIndexWords(term).getIndexWordCollection();
            for (var synSet : synSets) {
                for (var synonym : synSets) {
                    expandedTerms.add(null);
                }
            }
        }
        return String.join(" ", expandedTerms);
    }
}

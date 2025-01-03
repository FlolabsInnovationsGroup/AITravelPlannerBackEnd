package com.springboot.FlomadAIplanner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class GPT2Tokenizer {

    private static final String VOCAB_FILE = "src/main/resources/tokenizer/vocab.json";
    private static final String MERGES_FILE = "src/main/resources/tokenizer/merges.txt";

    private Map<String, Integer> vocab;
    private Map<String, Integer> bpeRanks;
    private Pattern pattern;

    public GPT2Tokenizer() throws IOException {
        loadVocabulary();
    }

    /**
     * Loads the vocabulary and merges files.
     */
    private void loadVocabulary() throws IOException {
        // Initialize ObjectMapper for JSON parsing
        ObjectMapper mapper = new ObjectMapper();

        // Load vocab.json
        InputStream vocabStream = getClass().getClassLoader().getResourceAsStream(VOCAB_FILE);
        if (vocabStream == null) {
            throw new FileNotFoundException("Vocabulary file not found: " + VOCAB_FILE);
        }
        vocab = mapper.readValue(vocabStream, new TypeReference<Map<String, Integer>>() {});

        // Load merges.txt
        InputStream mergesStream = getClass().getClassLoader().getResourceAsStream(MERGES_FILE);
        if (mergesStream == null) {
            throw new FileNotFoundException("Merges file not found: " + MERGES_FILE);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(mergesStream, StandardCharsets.UTF_8));
        String line;
        List<List<String>> merges = new ArrayList<>();

        // Skip the first line which is a comment
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] pair = line.split(" ");
            merges.add(Arrays.asList(pair[0], pair[1]));
        }

        // Create a map for quick lookup of BPE ranks
        bpeRanks = new HashMap<>();
        for (int i = 0; i < merges.size(); i++) {
            List<String> pair = merges.get(i);
            bpeRanks.put(pair.get(0) + " " + pair.get(1), i);
        }

        // Initialize a regex pattern to split on word boundaries
        pattern = Pattern.compile("[ \\t\\r\\n]+");
    }

    /**
     * Encodes a text string into a list of token IDs using BPE.
     *
     * @param text The input text to encode.
     * @return An array of token IDs.
     */
    public int[] encode(String text) {
        List<String> tokens = tokenize(text);
        return tokens.stream()
                .mapToInt(token -> vocab.getOrDefault(token, vocab.get("<unk>")))
                .toArray();
    }

    /**
     * Tokenizes a string into a list of tokens using BPE.
     *
     * @param text The input text to tokenize.
     * @return A list of token strings.
     */
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        // Split the text into words based on the regex pattern
        String[] words = pattern.split(text);
        for (String word : words) {
            if (word.isEmpty()) continue;
            List<String> wordTokens = bpe(word);
            tokens.addAll(wordTokens);
        }
        return tokens;
    }

    /**
     * Applies BPE to a single word.
     *
     * @param word The word to encode.
     * @return A list of BPE tokens.
     */
    private List<String> bpe(String word) {
        // Initialize the list of symbols (characters + end-of-word)
        List<String> symbols = new ArrayList<>();
        for (char c : word.toCharArray()) {
            symbols.add(String.valueOf(c));
        }
        symbols.add("</w>"); // End-of-word token

        while (symbols.size() > 1) {
            int minRank = Integer.MAX_VALUE;
            int mergePosition = -1;

            // Find the pair with the lowest rank
            for (int i = 0; i < symbols.size() - 1; i++) {
                String pair = symbols.get(i) + " " + symbols.get(i + 1);
                if (bpeRanks.containsKey(pair)) {
                    int rank = bpeRanks.get(pair);
                    if (rank < minRank) {
                        minRank = rank;
                        mergePosition = i;
                    }
                }
            }

            // If no merges are found, break
            if (mergePosition == -1) {
                break;
            }

            // Merge the pair
            String merged = symbols.get(mergePosition) + symbols.get(mergePosition + 1);
            symbols.remove(mergePosition + 1);
            symbols.set(mergePosition, merged);
        }

        // Remove the end-of-word token
        if (!symbols.isEmpty() && symbols.get(symbols.size() - 1).equals("</w>")) {
            symbols.remove(symbols.size() - 1);
        }

        return symbols;
    }

    public String decode(int nextToken) {
        return "";
    }
}
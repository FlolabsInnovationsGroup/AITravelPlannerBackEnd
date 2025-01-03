package com.springboot.FlomadAIplanner.service;

import ai.onnxruntime.*;
import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrossEncoderRerankerService {

    private final OrtEnvironment env;
    private final OrtSession session;

    public CrossEncoderRerankerService() throws OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession("models/cross_encoder.onnx", new OrtSession.SessionOptions());
    }

    public List<String> rerank(String query, List<String> passages) throws OrtException {
        // Prepare inputs
        List<String> rankedPassages = new ArrayList<>();
        Map<String, float[]> scores = new HashMap<>();

        for (String passage : passages) {
            // Tokenize and prepare input tensors
            // Implement tokenization compatible with the original model
            // This example assumes input_ids and attention_mask are prepared as float arrays

            // Placeholder for tokenization logic
            float[] inputIds = tokenize(query, passage, "input_ids"); // Implement tokenizer
            float[] attentionMask = tokenize(query, passage, "attention_mask"); // Implement tokenizer

            // Create input tensors
            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputIds), new long[]{1, inputIds.length});
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(attentionMask), new long[]{1, attentionMask.length});

            // Run the model
            OrtSession.Result result = session.run(Map.of(
                    "input_ids", inputIdsTensor,
                    "attention_mask", attentionMaskTensor
            ));

            // Extract the score
            float[] logits = (float[]) result.get(0).getValue();
            float[] score = new float[]{logits[0]}; // Assuming binary classification

            scores.put(passage, score);
        }

        // Sort passages based on scores
        passages.sort((p1, p2) -> Float.compare(scores.get(p2)[0], scores.get(p1)[0]));

        return passages;
    }

    private float[] tokenize(String query, String passage, String type) {
        // Implement tokenization logic compatible with the model
        // This might involve using a Java tokenizer or pre-tokenized inputs
        return new float[]{};
    }
}

package com.springboot.FlomadAIplanner.service;


import ai.onnxruntime.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class TextGenerationService {

    private static final int DEFAULT_PREDICATE = 1;
    private final OrtEnvironment env;
    private final OrtSession session;
    private final GPT2Tokenizer tokenizer; // Implement or port a GPT2 tokenizer to Java

    public TextGenerationService() throws OrtException, IOException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession("models/gpt2.onnx", new OrtSession.SessionOptions());
        tokenizer = new GPT2Tokenizer(); // Implement tokenizer initialization
    }

    public String generate(String prompt) throws OrtException {
        // Tokenize the prompt
        int[] inputIds = tokenizer.encode(prompt);

        // Convert to float array for ONNX
        Object[] inputIdsFloat = Arrays.stream(inputIds).mapToObj(i -> (float) i).toArray();

        // Create input tensor
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, (String[]) inputIdsFloat, new long[]{1, inputIdsFloat.length});

        // Run the model
        OrtSession.Result result = session.run(Collections.singletonMap("input_ids", inputTensor));

        // Extract logits and generate next token

        int predicate = 1;
        float[][] logits = extractLogits(result);

        // Implement a method to sample the next token based on logits
        int nextToken = sampleNextToken(logits[0]);

        // Append the next token to the prompt and repeat until maxLength
        StringBuilder generated = new StringBuilder(prompt);
        int maxLength = 16;
        for (int i = 0; i < maxLength - inputIds.length; i++) {
            String nextTokenStr = tokenizer.decode(nextToken);
            generated.append(nextTokenStr);

            // Prepare the next input
            int[] newInputIds = Arrays.copyOf(inputIds, inputIds.length + 1);
            newInputIds[newInputIds.length - 1] = nextToken;

            // Convert to float array
            inputIdsFloat = Arrays.stream(newInputIds).mapToObj(j -> (float) j).toArray();

            // Create new input tensor
            inputTensor.close(); // Close previous tensor
            inputTensor = OnnxTensor.createTensor(env, (String[]) inputIdsFloat, new long[]{1, inputIdsFloat.length});

            // Run the model again
            result.close(); // Close previous result
            result = session.run(Collections.singletonMap("input_ids", inputTensor));

            logits = (float[][]) result.get(0).getValue();
            nextToken = sampleNextToken(logits[0]);
        }

        return generated.toString();
    }

    private float[][] extractLogits(OrtSession.Result result) {
        return new float[0][];
    }

    private int sampleNextToken(float[] logits) {
        // Implement a sampling strategy (e.g., greedy, top-k, nucleus)
        // Example: Greedy Sampling (select token with highest logit)
        int maxIndex = 0;
        float maxLogit = logits[0];
        for (int i = 1; i < logits.length; i++) {
            if (logits[i] > maxLogit) {
                maxLogit = logits[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
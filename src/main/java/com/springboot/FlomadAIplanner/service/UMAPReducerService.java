package com.springboot.FlomadAIplanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;

public class UMAPReducerService {
    private static final String UMAP_URL = "http://localhost:8080/reduce";
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public UMAPReducerService() {
        client = new OkHttpClient();
        mapper = new ObjectMapper();
    }

    public double[][] reduce(double[][] vectors) throws IOException {
        String json = mapper.writeValueAsString(new VectorRequest(vectors));
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(UMAP_URL)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String responseBody = response.body().string();
            VectorResponse vectorResponse = mapper.readValue(responseBody, VectorResponse.class);
            return vectorResponse.getReducedVectors();
        }
    }

    // Helper classes
    static class VectorRequest {
        public double[][] vectors;

        public VectorRequest(double[][] vectors) {
            this.vectors = vectors;
        }
    }

    static class VectorResponse {
        public double[][] reduced_vectors;

        public double[][] getReducedVectors() {
            return reduced_vectors;
        }

        public void setReducedVectors(double[][] reduced_vectors) {
            this.reduced_vectors = reduced_vectors;
        }
    }
}

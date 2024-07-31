package com.springboot.FlomadAIplanner.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;

public class ChatBot {
    private static final String API_KEY = "YOUR_OPENAI_API_KEY";
    private static final String API_URL = "https://api.openai.com/v1/engines/davinci-codex/completions";

    public String getResponse(String prompt) throws IOException {
        OkHttpClient client = new OkHttpClient();

        JsonObject json = new JsonObject();
        json.addProperty("prompt", prompt);
        json.addProperty("max_tokens", 150);

        RequestBody body = RequestBody.create(
                json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JsonObject responseObject = JsonParser.parseString(responseBody).getAsJsonObject();
            return responseObject.get("choices").getAsJsonArray()
                    .get(0).getAsJsonObject().get("text").getAsString().trim();
        }
    }
}

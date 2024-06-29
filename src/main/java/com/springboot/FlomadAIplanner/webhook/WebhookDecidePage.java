package com.springboot.FlomadAIplanner.webhook;

import com.google.cloud.dialogflow.cx.v3beta1.WebhookResponse;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * @class
 * @description Since the dialogflow CX can only communicate with a function that have been deployed on the internet, all code here
 * transfered to webhook file in root path
 */
public class WebhookDecidePage implements HttpFunction {
    private static final Logger logger = Logger.getLogger(WebhookDecidePage.class.getName());

    @Override
    public void service(final HttpRequest request, final HttpResponse response) throws Exception {
        String requestBody = request.getReader().lines().collect(Collectors.joining());
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
        JsonObject parametersJson = json.getAsJsonObject("parameters");

        Map<String, Value> parameters = new HashMap<>();
        for (String key : parametersJson.keySet()) {
            JsonElement value = parametersJson.get(key);
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                parameters.put(key, Value.newBuilder().setStringValue(value.getAsString()).build());
            }
        }

        WebhookResponse webhookResponse = handleWebhookRequest(parameters);

        String webhookResponseJson = JsonFormat.printer().print(webhookResponse);

        BufferedWriter writer = response.getWriter();
        writer.write(webhookResponseJson);
    }

    public WebhookResponse handleWebhookRequest(Map<String, Value> parameters) {
        boolean shouldGoToPageB = false;
        if (parameters.containsKey("A")) {
            Value paramA = parameters.get("A");
            shouldGoToPageB = true;
        }


        WebhookResponse.Builder responseBuilder = WebhookResponse.newBuilder();

        if (shouldGoToPageB) {
            responseBuilder.setTargetPage("projects/your-project/locations/your-location/agents/your-agent/flows/your-flow/pages/PageB");
        } else {
            responseBuilder.setTargetPage("projects/your-project/locations/your-location/agents/your-agent/flows/your-flow/pages/PageC");
        }

        return responseBuilder.build();
    }
}

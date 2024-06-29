package com.springboot.FlomadAIplanner.webhook;

import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class WebhookHelloWorld {

    private static final Logger logger = Logger.getLogger(WebhookHelloWorld.class.getName());
    // @PostMapping("/webhook")
    // public String handleWebhook(@RequestBody String request) {
    //     logger.info("Hello World!!!!!!!!!!!!!Received webhook request: " + request);
    //     return "Hello World";
    // }
}

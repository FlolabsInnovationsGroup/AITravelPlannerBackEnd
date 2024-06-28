package com.springboot.FlomadAIplanner.DialogflowWebhookService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @class
 * @description Since the dialogflow CX can only communicate with a function that have been deployed on the internet, all code here
 * transfered to webhook file in root path
 */
@Slf4j
@RestController
public class SkipFlightReq {

    @PostMapping("/SkipFlightReq")
    public void handleDialogflowWebhook(@RequestBody String requestBody) {
        log.info("Received Dialogflow request: {}", requestBody);
    }
}
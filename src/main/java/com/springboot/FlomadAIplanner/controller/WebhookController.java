package com.springboot.FlomadAIplanner.controller;

// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RestController;

// import com.springboot.FlomadAIplanner.DialogflowWebhookService.WbhookFlightJumpPage;

// import org.springframework.beans.factory.annotation.Autowired;

// @RestController
// public class WebhookController {

//     private final WbhookFlightJumpPage wbhookFlightJumpPage;

//     @Autowired
//     public WebhookController(WbhookFlightJumpPage wbhookFlightJumpPage) {
//         this.wbhookFlightJumpPage = wbhookFlightJumpPage;
//     }

//     @PostMapping("/webhook")
//     public String handleWebhook(@RequestBody String request) {
//         try {
//             return wbhookFlightJumpPage.processRequest(request);
//         } catch (Exception e) {
//             return "Error handling request: " + e.getMessage();
//         }
//     }
// }


import com.springboot.FlomadAIplanner.DTO.DialogflowRequest;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.DialogflowService;
import com.springboot.FlomadAIplanner.webhook.WebhookFlightJumpPageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WebhookController {
    private final WebhookFlightJumpPageService service;
    @Autowired
    private DialogflowService dialogflowService;

    @Autowired
    public WebhookController(WebhookFlightJumpPageService service) {
        this.service = service;
    }

    @CrossOrigin(origins = {"http://localhost:4200", "https://aitravelplanner-e6f156c81d14.herokuapp.com/"})
    @PostMapping("/dialogflowLiaison")
    public ResponseEntity<String> handleDialogflowRequest(@RequestBody DialogflowRequest request) {
        log.info("Received request: {}", request.getMessage());
        try {
            String userInput = request.getMessage();
            String responseText = dialogflowService.processInput(userInput);
            return ResponseEntity.ok(responseText);
        } catch (Exception e) {
            log.error("An error occurred while processing the request: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/webhook")
    public String handleWebhook(@RequestBody String request) throws AmadeusAPIException {
        return service.processRequest(request);
    }
}

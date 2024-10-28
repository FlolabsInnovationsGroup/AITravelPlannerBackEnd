package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.dto.GuestDTO;
import com.springboot.FlomadAIplanner.service.model.GuestSessionService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * GuestController handles the interactions for guest users.
 * It tracks the number of messages sent by the guest and limits them to 5 messages.
 */
@RestController
@RequestMapping("/sessions")
public class GuestController {

    private final GuestSessionService guestSessionService;

    public GuestController(GuestSessionService guestSessionService) {
        this.guestSessionService = guestSessionService;
    }

    /**
     * Endpoint for guest users to send messages.
     * Limits guest users to 5 messages before requiring signup.
     *
     * @param session HttpSession to store and retrieve message count.
     * @return A message indicating the number of messages sent or requesting sign-up.
     */
    @PostMapping("/guest")
    public String postGuestMessage(@RequestBody GuestDTO guestDTO) {
        String sessionId = guestDTO.getSessionId();

        // Handle message count with the service layer.
        boolean canSendMessage = guestSessionService.incrementMessageCount(sessionId);

        if (!canSendMessage) {
            return "You have reached the limit of 5 messages. Please sign up to continue.";
        }

        return "Message from guest user. Session ID: " + sessionId;
    }
}


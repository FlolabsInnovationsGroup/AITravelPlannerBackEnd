package com.springboot;

import com.springboot.FlomadAIplanner.controller.model.GuestController;
import com.springboot.FlomadAIplanner.dto.GuestDTO;
import com.springboot.FlomadAIplanner.service.model.GuestSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GuestControllerTest {

    @Mock
    private GuestSessionService guestSessionService;

    @InjectMocks
    private GuestController guestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPostGuestMessage_Success() {
        // Mock the service to allow message sending
        when(guestSessionService.incrementMessageCount(anyString())).thenReturn(true);

        GuestDTO guestDTO = new GuestDTO();
        guestDTO.setSessionId("12345");

        // Call the controller method
        String result = guestController.postGuestMessage(guestDTO);

        // Verify the result
        assertEquals("Message from guest user. Session ID: 12345", result);
    }

    @Test
    void testPostGuestMessage_LimitReached() {
        // Mock the service to reject messages after limit
        when(guestSessionService.incrementMessageCount(anyString())).thenReturn(false);

        GuestDTO guestDTO = new GuestDTO();
        guestDTO.setSessionId("12345");

        // Call the controller method
        String result = guestController.postGuestMessage(guestDTO);

        // Verify the result
        assertEquals("You have reached the limit of 5 messages. Please sign up to continue.", result);
    }
}


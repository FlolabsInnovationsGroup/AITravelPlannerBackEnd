package com.springboot.FlomadAIplanner.controller.login;

import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;
import com.springboot.FlomadAIplanner.dto.auth.GuestTokenResponse;
import com.springboot.FlomadAIplanner.exceptions.validators.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.service.login.AuthService;
import com.springboot.FlomadAIplanner.service.login.SessionAuthService;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SessionAuthService sessionAuthService;
    private final AuthService authService;
    private final UserValidator userValidator;

    public AuthController(SessionAuthService sessionAuthService, 
                         AuthService authService,
                         UserValidator userValidator) {
        this.sessionAuthService = sessionAuthService;
        this.authService = authService;
        this.userValidator = userValidator;
    }

    @Operation(summary = "Sign-up new user", description = "Registers a new user and automatically logs them in")
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signUp(@Valid @RequestBody UserAuthDTO userAuthDTO,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            userValidator.validateUserDoesNotExist(userAuthDTO);
            return sessionAuthService.registerUser(userAuthDTO, request, response);
        } catch (UserAlreadyExistsException e) {
            log.error("Sign-up failed for user: {}. Error: {}", userAuthDTO.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new LoginResponse(false, null, "Sign-up failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Verify user email", description = "Verifies user email using the verification token provided in the email")
    @GetMapping("/verify")
    public ResponseEntity<LoginResponse> verifyUser(@RequestParam("token") String token) {
        return sessionAuthService.verifyUser(token);
    }

    @Operation(summary = "Login user", description = "Logs in a user and creates a session")
    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        return sessionAuthService.login(loginRequest, request, response);
    }

    @Operation(summary = "Create guest session", description = "Creates a guest session for anonymous users")
    @PostMapping("/guest-session")
    public ResponseEntity<GuestTokenResponse> createGuestSession(HttpServletRequest request,
                                                           HttpServletResponse response) {
        log.info("Creating guest session");
        // Generate guest session and trigger side effects (session creation, cookies)
        sessionAuthService.generateGuestSession(request, response);
        
        // Extract session ID from the request and create a token
        String sessionId = request.getSession().getId();
        
        return ResponseEntity.ok(new GuestTokenResponse(sessionId));
    }

    @Operation(summary = "Create guest token", description = "Creates a JWT token for anonymous guest users")
    @PostMapping("/guest-token")
    public ResponseEntity<GuestTokenResponse> createGuestToken() {
        log.info("Creating JWT guest token");
        
        try {
            // Generate guest JWT token via AuthService
            ResponseEntity<LoginResponse> tokenResponse = authService.generateGuestToken();
            
            if (tokenResponse.getBody() != null && tokenResponse.getBody().getToken() != null) {
                String jwtToken = tokenResponse.getBody().getToken();
                log.info("✅ Guest JWT token created successfully");
                return ResponseEntity.ok(new GuestTokenResponse(jwtToken));
            }
            
            log.error("❌ Failed to generate guest token - no token in response");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GuestTokenResponse(null));
                    
        } catch (Exception e) {
            log.error("❌ Error creating guest token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GuestTokenResponse(null));
        }
    }

    @Operation(summary = "Get session state", description = "Returns the current session state and user information")
    @GetMapping("/session-state")
    public ResponseEntity<SessionAuthService.SessionStateResponse> getSessionState(HttpServletRequest request) {
        return sessionAuthService.getSessionState(request);
    }

    @Operation(summary = "Logout user", description = "Logs out the current user and invalidates the session")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        return sessionAuthService.logout(request, response);
    }
}

package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;
import com.springboot.FlomadAIplanner.exceptions.validators.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.exceptions.validators.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.RoleService;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class SessionAuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final UserValidator userValidator;
    private final AuthenticationManager authenticationManager;
    private final Converter<User, UserDetails> userConverter;
    private final Converter<UserAuthDTO, User> userDTOToUserConverter;

    public SessionAuthService(UserService userService, RoleService roleService, UserValidator userValidator,
                             AuthenticationManager authenticationManager, Converter<User, UserDetails> userConverter,
                             Converter<UserAuthDTO, User> userDTOToUserConverter) {
        this.userService = userService;
        this.roleService = roleService;
        this.userValidator = userValidator;
        this.authenticationManager = authenticationManager;
        this.userConverter = userConverter;
        this.userDTOToUserConverter = userDTOToUserConverter;
    }

    public ResponseEntity<LoginResponse> registerUser(UserAuthDTO userAuthDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("Attempting to register user: {}", userAuthDTO.getUsername());

            // Validate user doesn't exist
            userValidator.validateUserDoesNotExist(userAuthDTO);

            // Create new user
            User newUser = userDTOToUserConverter.convert(userAuthDTO);
            newUser.setProvider("none");
            User savedUser = userService.registerUser(newUser);

            log.info("User registered successfully: {}", savedUser.getUsername());

            // Auto-login the user after registration
            return autoLoginUser(savedUser, request, response, "User registered and logged in successfully");

        } catch (UserAlreadyExistsException e) {
            log.error("Registration failed for user: {}. Error: {}", userAuthDTO.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new LoginResponse(false, null, "Registration failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null, "Registration failed: " + e.getMessage()));
        }
    }

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("Attempting login for user: {}", loginRequest.getUsername());

            // Authenticate user - authentication is used to verify credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // Get user details
            User user = userService.findByUsernameOrEmail(loginRequest.getUsername());
            if (user == null) {
                log.error("User not found: {}", loginRequest.getUsername());
                throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
            }

            log.info("Authentication successful for user: {}", loginRequest.getUsername());

            // Auto-login the user
            return autoLoginUser(user, request, response, "Login successful");

        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed for user: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
        }
    }

    public ResponseEntity<LoginResponse> generateGuestSession(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("Generating guest session");

            // Create guest user
            User guestUser = createGuestUser();
            
            // Auto-login the guest user
            return autoLoginUser(guestUser, request, response, "Guest session created successfully");

        } catch (Exception e) {
            log.error("Error creating guest session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null, "Failed to create guest session: " + e.getMessage()));
        }
    }

    public ResponseEntity<LoginResponse> verifyUser(String token) {
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body(new LoginResponse(false, null, "Invalid verification token."));
        }
        userService.verifyUser(user);
        return ResponseEntity.ok(new LoginResponse(true, null, "Email verified successfully."));
    }

    public ResponseEntity<SessionStateResponse> getSessionState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return ResponseEntity.ok(new SessionStateResponse(false, null, null, null, "No active session"));
        }

        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String userRole = (String) session.getAttribute("userRole");

        if (userId == null) {
            return ResponseEntity.ok(new SessionStateResponse(false, null, null, null, "Session exists but no user data"));
        }

        return ResponseEntity.ok(new SessionStateResponse(true, userId, username, userRole, "Active session"));
    }

    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                log.info("User session invalidated");
            }

            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(new LoginResponse(true, null, "Logout successful"));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null, "Logout failed: " + e.getMessage()));
        }
    }

    private ResponseEntity<LoginResponse> autoLoginUser(User user, HttpServletRequest request, HttpServletResponse response, String message) {
        try {
            // Create authentication token
            UserDetails userDetails = userConverter.convert(user);
            UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Create or get session
            HttpSession session = request.getSession(true);
            
            // Store user information in session
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRoles().stream()
                    .findFirst()
                    .map(role -> role.getName())
                    .orElse("USER"));
            session.setAttribute("isEmailVerified", user.getIsEmailVerified());

            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(1800);

            log.info("User {} successfully logged in with session ID: {}", user.getUsername(), session.getId());

            return ResponseEntity.ok(new LoginResponse(user.getIsEmailVerified(), null, message));

        } catch (Exception e) {
            log.error("Error during auto-login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null, "Auto-login failed: " + e.getMessage()));
        }
    }

    private User createGuestUser() {
        log.info("Creating new guest user");
        User guestUser = new User();
        guestUser.setRoles(Collections.singleton(roleService.findByName("GUEST")));
        guestUser.setLastActive(LocalDateTime.now());
        guestUser.setUsername("guest_" + UUID.randomUUID());
        guestUser.setPassword("");
        guestUser.setEmail("guest@" + UUID.randomUUID() + ".temp");
        guestUser.setIsEmailVerified(false);
        guestUser.setProvider("guest");
        
        User savedGuestUser = userService.save(guestUser);
        log.info("Created new guest user with ID: {}", savedGuestUser.getId());
        return savedGuestUser;
    }

    // Inner class for session state response
    public static class SessionStateResponse {
        private final boolean authenticated;
        private final String userId;
        private final String username;
        private final String userRole;
        private final String message;

        public SessionStateResponse(boolean authenticated, String userId, String username, String userRole, String message) {
            this.authenticated = authenticated;
            this.userId = userId;
            this.username = username;
            this.userRole = userRole;
            this.message = message;
        }

        // Getters
        public boolean isAuthenticated() { return authenticated; }
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getUserRole() { return userRole; }
        public String getMessage() { return message; }
    }
} 
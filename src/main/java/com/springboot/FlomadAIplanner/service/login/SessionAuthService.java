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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public final class SessionAuthService {

    /** Session timeout in seconds (30 minutes). */
    private static final int SESSION_TIMEOUT_SECONDS = 1800;

    /** User service. */
    private final UserService userService;
    /** Role service. */
    private final RoleService roleService;
    /** User validator. */
    private final UserValidator userValidator;
    /** Authentication manager. */
    private final AuthenticationManager authenticationManager;
    /** Converter from User to UserDetails. */
    private final Converter<User, UserDetails> userConverter;
    /** Converter from UserAuthDTO to User. */
    private final Converter<UserAuthDTO, User> userDTOToUserConverter;

    /**
     * Constructor.
     *
     * @param pUserService user service
     * @param pRoleService role service
     * @param pUserValidator user validator
     * @param pAuthenticationManager authentication manager
     * @param pUserConverter user to UserDetails converter
     * @param pUserDTOToUserConverter UserAuthDTO to User converter
     */
    public SessionAuthService(
            final UserService pUserService,
            final RoleService pRoleService,
            final UserValidator pUserValidator,
            final AuthenticationManager pAuthenticationManager,
            final Converter<User, UserDetails> pUserConverter,
            final Converter<UserAuthDTO, User> pUserDTOToUserConverter) {
        this.userService = pUserService;
        this.roleService = pRoleService;
        this.userValidator = pUserValidator;
        this.authenticationManager = pAuthenticationManager;
        this.userConverter = pUserConverter;
        this.userDTOToUserConverter = pUserDTOToUserConverter;
    }

    /**
     * Registers a new user and auto-logs in.
     *
     * @param userAuthDTO registration data
     * @param request HTTP request
     * @param response HTTP response
     * @return login response
     */
    public ResponseEntity<LoginResponse> registerUser(
            final UserAuthDTO userAuthDTO,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        try {
            log.info("Registering user: {}", userAuthDTO.getUsername());

            userValidator.validateUserDoesNotExist(userAuthDTO);

            User newUser = userDTOToUserConverter.convert(userAuthDTO);
            newUser.setProvider("none");
            User savedUser = userService.registerUser(newUser);

            log.info("User registered: {}", savedUser.getUsername());

            return autoLoginUser(savedUser, request, response,
                    "User registered and logged in successfully");

        } catch (UserAlreadyExistsException e) {
            log.error("Registration failed for user: {}. Error: {}",
                    userAuthDTO.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new LoginResponse(false, null,
                            "Registration failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null,
                            "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Authenticates user and establishes session.
     *
     * @param loginRequest login credentials
     * @param request HTTP request
     * @param response HTTP response
     * @return login response
     */
    public ResponseEntity<LoginResponse> login(
            final LoginRequest loginRequest,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        try {
            log.info("Login attempt: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            User user = userService.findByUsernameOrEmail(
                    loginRequest.getUsername());
            if (user == null) {
                log.error("User not found: {}", loginRequest.getUsername());
                throw new UserNotFoundException(
                    "User not found: " + loginRequest.getUsername());
            }

            log.info("Authentication successful: {}", loginRequest.getUsername());

            return autoLoginUser(user, request, response,
                    "Login successful");

        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed for user: {}. Error: {}",
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null,
                            "Login failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null,
                            "Login failed: " + e.getMessage()));
        }
    }

    /**
     * Creates a guest session and auto-logs in.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @return login response
     */
    public ResponseEntity<LoginResponse> generateGuestSession(
            final HttpServletRequest request,
            final HttpServletResponse response) {
        try {
            log.info("Generating guest session");

            User guestUser = createGuestUser();

            return autoLoginUser(guestUser, request, response,
                    "Guest session created successfully");

        } catch (Exception e) {
            log.error("Error creating guest session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null,
                            "Failed to create guest session: "
                                    + e.getMessage()));
        }
    }

    /**
     * Verifies user by token.
     *
     * @param token verification token
     * @return login response
     */
    public ResponseEntity<LoginResponse> verifyUser(final String token) {
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, null,
                            "Invalid verification token."));
        }
        userService.verifyUser(user);
        return ResponseEntity.ok(
                new LoginResponse(true, null, "Email verified successfully."));
    }

    /**
     * Returns current session state.
     *
     * @param request HTTP request
     * @return session state response
     */
    public ResponseEntity<SessionStateResponse> getSessionState(
            final HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.ok(new SessionStateResponse(
                    false, null, null, null, "No active session"));
        }

        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String userRole = (String) session.getAttribute("userRole");

        if (userId == null) {
            return ResponseEntity.ok(new SessionStateResponse(
                    false, null, null, null, "Session exists but no user data"));
        }

        return ResponseEntity.ok(
                new SessionStateResponse(
                        true, userId, username, userRole, "Active session"));
    }

    /**
     * Logs out and invalidates session.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @return login response
     */
    public ResponseEntity<LoginResponse> logout(
            final HttpServletRequest request,
            final HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                log.info("User session invalidated");
            }

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(
                    new LoginResponse(true, null, "Logout successful"));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null,
                            "Logout failed: " + e.getMessage()));
        }
    }

    private ResponseEntity<LoginResponse> autoLoginUser(
            final User user,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final String message) {
        try {
            UserDetails userDetails = userConverter.convert(user);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext()
                    .setAuthentication(authenticationToken);

            HttpSession session = request.getSession(true);

            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRoles().stream()
                    .findFirst()
                    .map(role -> role.getName())
                    .orElse("USER"));
            session.setAttribute("isEmailVerified", user.getIsEmailVerified());

            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

            log.info("User {} logged in session: {}",
                    user.getUsername(), session.getId());

            return ResponseEntity.ok(
                    new LoginResponse(user.getIsEmailVerified(), null, message));

        } catch (Exception e) {
            log.error("Error during auto-login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null,
                            "Auto-login failed: " + e.getMessage()));
        }
    }

    private User createGuestUser() {
        log.info("Creating new guest user");
        User guestUser = new User();
        guestUser.setRoles(Collections.singleton(
                roleService.findByName("GUEST")));
        guestUser.setLastActive(LocalDateTime.now());
        guestUser.setUsername("guest_" + UUID.randomUUID());
        guestUser.setPassword("");
        String guestEmail = "guest@" + UUID.randomUUID() + ".temp";
        guestUser.setEmail(guestEmail);
        guestUser.setIsEmailVerified(false);
        guestUser.setProvider("guest");

        User savedGuestUser = userService.save(guestUser);
        log.info("Created new guest user with ID: {}", savedGuestUser.getId());
        return savedGuestUser;
    }

    /** Session state response DTO. */
    public static final class SessionStateResponse {
        /** Whether user is authenticated. */
        private final boolean authenticated;
        /** User ID. */
        private final String userId;
        /** Username. */
        private final String username;
        /** User role. */
        private final String userRole;
        /** Status message. */
        private final String message;

        /**
         * Constructor.
         *
         * @param pAuthenticated authenticated flag
         * @param pUserId user id
         * @param pUsername username
         * @param pUserRole user role
         * @param pMessage message
         */
        public SessionStateResponse(
                final boolean pAuthenticated,
                final String pUserId,
                final String pUsername,
                final String pUserRole,
                final String pMessage) {
            this.authenticated = pAuthenticated;
            this.userId = pUserId;
            this.username = pUsername;
            this.userRole = pUserRole;
            this.message = pMessage;
        }

        /** @return true if authenticated */
        public boolean isAuthenticated() {
            return authenticated;
        }

        /** @return user id */
        public String getUserId() {
            return userId;
        }

        /** @return username */
        public String getUsername() {
            return username;
        }

        /** @return user role */
        public String getUserRole() {
            return userRole;
        }

        /** @return message */
        public String getMessage() {
            return message;
        }
    }
}

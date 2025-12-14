package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;
import com.springboot.FlomadAIplanner.exceptions.validators.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.service.model.RoleService;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class AuthService extends AbstractAuthService {

    private final AuthenticationManager authenticationManager;
    private final Converter<User, UserDetails> userConverter;
    private final Converter<UserAuthDTO, User> userDTOToUserConverter;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;
    private final UserValidator userValidator;
    private final CurrentUserUtil currentUserUtil;

    public AuthService(UserService userService, SessionService sessionService,
                       MessageService messageService, AuthenticationManager authenticationManager,
                       Converter<User, UserDetails> userConverter, Converter<UserAuthDTO, User> userDTOToUserConverter,
                       RoleService roleService, JwtUtil jwtUtil, UserValidator userValidator,
                       CurrentUserUtil currentUserUtil) {
        super(userService, sessionService, messageService, roleService);
        this.authenticationManager = authenticationManager;
        this.userConverter = userConverter;
        this.userDTOToUserConverter = userDTOToUserConverter;
        this.roleService = roleService;
        this.jwtUtil = jwtUtil;
        this.userValidator = userValidator;
        this.currentUserUtil = currentUserUtil;
    }

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        try {
            log.info("Attempting login for user: {}", loginRequest.getUsername());

            // Authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            log.info("Authentication successful for user: {}", loginRequest.getUsername());

            // Validate user password matches
            User user = userService.findByUsernameOrEmail(loginRequest.getUsername());
            if (user == null) {
                log.error("User not found: {}", loginRequest.getUsername());
                throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
            }

            log.debug("User found: {}", user);

            userValidator.validateUserPasswordMatches(user, loginRequest.getPassword());
            log.info("Password validation successful for user: {}", loginRequest.getUsername());

            // Convert user to UserDetails
            UserDetails userDetails = userConverter.convert(user);
            String jwtToken = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new LoginResponse(user.getIsEmailVerified(), jwtToken, "Login successful"));
        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed for user: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
        }
    }

    public ResponseEntity<String> verifyUser(String token) {
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }
        userService.verifyUser(user);
        return ResponseEntity.ok("Email verified successfully.");
    }

    @SuppressWarnings("all")
    public User registerUser(UserAuthDTO userAuthDTO) {
        User existingUser = currentUserUtil.getCurrentUserFromSecurityContext();

        if (existingUser != null && userService.isGuest(existingUser)) {
            User newUser = createNewUserToRegister(userAuthDTO);
            copyChatsHistoryFromGuestUserToNewUser(newUser, existingUser);

            log.info("Upgraded guest user to registered user: {}", existingUser.getUsername());
            return newUser;
        } else if (existingUser != null && !userService.isGuest(existingUser)) {
            // If user is already a registered user, throw an exception
            String exceptionMessage = String.format("User %s is currently logged in, please log out to register a new user", existingUser.getUsername());
            throw new IllegalStateException(exceptionMessage);
        } else {
            // Existing logic to create a new user
            User user = userDTOToUserConverter.convert(userAuthDTO);
            userService.registerUser(user);
            return user;
        }
    }

    public ResponseEntity<LoginResponse> generateGuestToken() {
        // Allow creating guest tokens even if user is authenticated (for new sessions)
        log.info("Generating JWT guest token");
        
        try {
            User guestUser = createGuestUser();
            String token = generateTokenForUser(guestUser);
            log.info("✅ Successfully generated JWT token for guest user: {}", guestUser.getUsername());
            return ResponseEntity.ok(new LoginResponse(false, token, "Guest token generated successfully"));
        } catch (Exception e) {
            log.error("❌ Error generating guest token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null, "Failed to generate guest token: " + e.getMessage()));
        }
    }

    // Kept for future authentication checks - may be used in auth flow improvements
    @SuppressWarnings("unused")
    private boolean isUserAuthenticated() {
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        return user != null;
    }

    private User createGuestUser() {
        log.info("Creating new guest user");
        User guestUser = new User();
        guestUser.setRoles(Collections.singleton(roleService.findByName("GUEST")));
        guestUser.setLastActive(LocalDateTime.now());
        guestUser.setUsername("guest_" + UUID.randomUUID());
        guestUser.setPassword("");
        userService.save(guestUser);
        log.info("Created new guest user with ID: {}", guestUser.getId());
        return guestUser;
    }

    private String generateTokenForUser(User user) {
        log.info("Generating token for user: {}", user.getUsername());
        UserDetails userDetails = userConverter.convert(user);
        String token = jwtUtil.generateToken(userDetails);
        log.info("Generated token for user: {}", user.getUsername());
        return token;
    }
} 

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
public final class AuthService extends AbstractAuthService {

    /** Authentication manager. */
    private final AuthenticationManager authenticationManager;
    /** User to UserDetails converter. */
    private final Converter<User, UserDetails> userConverter;
    /** UserAuthDTO to User converter. */
    private final Converter<UserAuthDTO, User> userDTOToUserConverter;
    /** Role service. */
    private final RoleService roleService;
    /** JWT utility. */
    private final JwtUtil jwtUtil;
    /** User validator. */
    private final UserValidator userValidator;
    /** Current user helper. */
    private final CurrentUserUtil currentUserUtil;

    /**
     * Constructor.
     *
     * @param pUserService user service
     * @param pSessionService session service
     * @param pMessageService message service
     * @param pAuthenticationManager auth manager
     * @param pUserConverter user converter
     * @param pUserDTOToUserConverter DTO to user converter
     * @param pRoleService role service
     * @param pJwtUtil JWT util
     * @param pUserValidator user validator
     * @param pCurrentUserUtil current user util
     */
    public AuthService(
            final UserService pUserService,
            final SessionService pSessionService,
            final MessageService pMessageService,
            final AuthenticationManager pAuthenticationManager,
            final Converter<User, UserDetails> pUserConverter,
            final Converter<UserAuthDTO, User> pUserDTOToUserConverter,
            final RoleService pRoleService,
            final JwtUtil pJwtUtil,
            final UserValidator pUserValidator,
            final CurrentUserUtil pCurrentUserUtil) {
        super(pUserService, pSessionService, pMessageService, pRoleService);
        this.authenticationManager = pAuthenticationManager;
        this.userConverter = pUserConverter;
        this.userDTOToUserConverter = pUserDTOToUserConverter;
        this.roleService = pRoleService;
        this.jwtUtil = pJwtUtil;
        this.userValidator = pUserValidator;
        this.currentUserUtil = pCurrentUserUtil;
    }

    /**
     * Authenticates user and returns JWT.
     *
     * @param loginRequest credentials
     * @return login response with token
     */
    public ResponseEntity<LoginResponse> login(
            final LoginRequest loginRequest) {
        try {
            log.info("Login attempt: {}", loginRequest.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
            log.info("Authentication successful: {}",
                    loginRequest.getUsername());
            User user = getUserService()
                    .findByUsernameOrEmail(loginRequest.getUsername());
            if (user == null) {
                log.error("User not found: {}", loginRequest.getUsername());
                throw new UserNotFoundException(
                        "User not found: " + loginRequest.getUsername());
            }
            log.debug("User found: {}", user);
            userValidator.validateUserPasswordMatches(user,
                    loginRequest.getPassword());
            log.info("Password validation successful: {}",
                    loginRequest.getUsername());
            UserDetails userDetails = userConverter.convert(user);
            String jwtToken = jwtUtil.generateToken(userDetails);
            log.info("Login successful: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new LoginResponse(
                    user.getIsEmailVerified(), jwtToken, "Login successful"));
        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed: {}. Error: {}",
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null,
                            "Login failed: " + e.getMessage()));
        }
    }

    /**
     * Verifies user by token.
     *
     * @param token verification token
     * @return response message
     */
    public ResponseEntity<String> verifyUser(final String token) {
        User user = getUserService().findByVerificationToken(token);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body("Invalid verification token.");
        }
        getUserService().verifyUser(user);
        return ResponseEntity.ok("Email verified successfully.");
    }

    /**
     * Registers user or upgrades guest; returns created user.
     *
     * @param userAuthDTO registration data
     * @return created/updated user
     */
    @SuppressWarnings("all")
    public User registerUser(final UserAuthDTO userAuthDTO) {
        User existingUser =
                currentUserUtil.getCurrentUserFromSecurityContext();

        if (existingUser != null && getUserService().isGuest(existingUser)) {
            User newUser = createNewUserToRegister(userAuthDTO);
            copyChatsHistoryFromGuestUserToNewUser(newUser, existingUser);
            log.info("Upgraded guest to registered: {}",
                    existingUser.getUsername());
            return newUser;
        } else if (existingUser != null
                && !getUserService().isGuest(existingUser)) {
            String exceptionMessage = String.format(
                    "User %s is currently logged in, please log out to register",
                    existingUser.getUsername());
            throw new IllegalStateException(exceptionMessage);
        } else {
            User newUserFromDto =
                    userDTOToUserConverter.convert(userAuthDTO);
            getUserService().registerUser(newUserFromDto);
            return newUserFromDto;
        }
    }

    /**
     * Generates a guest JWT token.
     *
     * @return login response with guest token
     */
    public ResponseEntity<LoginResponse> generateGuestToken() {
        log.info("Generating JWT guest token");
        try {
            User guestUser = createGuestUser();
            String token = generateTokenForUser(guestUser);
            log.info("Generated JWT for guest: {}",
                    guestUser.getUsername());
            return ResponseEntity.ok(new LoginResponse(false, token,
                    "Guest token generated successfully"));
        } catch (Exception e) {
            log.error("Error generating guest token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, null,
                            "Failed to generate guest token: "
                                    + e.getMessage()));
        }
    }

    private boolean isUserAuthenticated() {
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        return user != null;
    }

    private User createGuestUser() {
        log.info("Creating new guest user");
        User guestUser = new User();
        guestUser.setRoles(Collections.singleton(
                roleService.findByName("GUEST")));
        guestUser.setLastActive(LocalDateTime.now());
        guestUser.setUsername("guest_" + UUID.randomUUID());
        guestUser.setPassword("");
        getUserService().save(guestUser);
        log.info("Created guest user ID: {}", guestUser.getId());
        return guestUser;
    }

    private String generateTokenForUser(final User user) {
        log.info("Generating token for user: {}", user.getUsername());
        UserDetails userDetails = userConverter.convert(user);
        String token = jwtUtil.generateToken(userDetails);
        log.info("Generated token for user: {}", user.getUsername());
        return token;
    }
}


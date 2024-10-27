package com.springboot.FlomadAIplanner.controller.login;

import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;
import com.springboot.FlomadAIplanner.exceptions.validators.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.service.login.AuthService;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserValidator userValidator;

    @Operation(summary = "Sign-up new user", description = "Registers a new user")
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserAuthDTO userAuthDTO) {
        try {
            userValidator.validateUserDoesNotExist(userAuthDTO);
            userAuthDTO.setProvider("none");
            authService.registerUser(userAuthDTO);
            return ResponseEntity.ok("User registered successfully. Please check your email for verification instructions.");
        } catch (UserAlreadyExistsException e) {
            log.error("Sign-up failed for user: {}. Error: {}", userAuthDTO.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Sign-up failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Verify user email", description = "Verifies user email using the verification token provided in the email")
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        return authService.verifyUser(token);

    }

    @Operation(summary = "Login user", description = "Logs in a user")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @Operation(summary = "Geneation Guest Token", description = "Generates a JWT token for guest user")
    @PostMapping("/guest-token")
    public ResponseEntity<LoginResponse>generateGuestToken() {
        return authService.generateGuestToken();
    }

//    private final UserService userService;
//    private final AuthenticationManager authenticationManager;
//    private final Converter<User, UserDetails> userConverter;
//    private final Converter<UserDTO, User> userDTOToUserConverter;
//    private final JwtUtil jwtUtil;
//    private final UserValidator userValidator;
//
//    @GetMapping("/user")
//    public Map<String, Object> user(OAuth2User principal) {
//        return principal.getAttributes();
//    }
//
//    @Operation(summary = "Sign-up new user", description = "Registers a new user")
//    @PostMapping("/signup")
//    public ResponseEntity<String> signUp(@Valid @RequestBody UserDTO userDTO) {
//        try {
//            userValidator.validateUserDoesNotExist(userDTO);
//
//            User user = userDTOToUserConverter.convert(userDTO);
//            userService.registerUser(user);
//            return ResponseEntity.ok("User registered successfully. Please check your email for verification instructions.");
//        } catch (UserAlreadyExistsException e) {
//            log.error("Sign-up failed for user: {}. Error: {}", userDTO.getUsername(), e.getMessage());
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Sign-up failed: " + e.getMessage());
//        }
//    }
//
//    @Operation(summary = "Verify user email", description = "Verifies user email using the verification token provided in the email")
//    @GetMapping("/verify")
//    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
//        User user = userService.findByVerificationToken(token);
//        if (user == null) {
//            return ResponseEntity.badRequest().body("Invalid verification token.");
//        }
//        userService.verifyUser(user);
//        return ResponseEntity.ok("Email verified successfully.");
//    }
//
//    @Operation(summary = "Login user", description = "Logs in a user")
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
//        try {
//            log.info("Attempting login for user: {}", loginRequest.getUsername());
//
//            // Authentication
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//            log.info("Authentication successful for user: {}", loginRequest.getUsername());
//
//            // Validate user password matches
//            User user = userService.findByUsernameOrEmail(loginRequest.getUsername());
//            if (user == null) {
//                log.error("User not found: {}", loginRequest.getUsername());
//                throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
//            }
//
//            log.debug("User found: {}", user);
//
//            userValidator.validateUserPasswordMatches(user, loginRequest.getPassword());
//            log.info("Password validation successful for user: {}", loginRequest.getUsername());
//
//            // Convert user to UserDetails
//            UserDetails userDetails = userConverter.convert(user);
//            String jwtToken = jwtUtil.generateToken(userDetails);
//
//            log.info("Login successful for user: {}", loginRequest.getUsername());
//            return ResponseEntity.ok(new LoginResponse(user.getIsEmailVerified(), jwtToken, "Login successful"));
//        } catch (AuthenticationException | UserNotFoundException e) {
//            log.error("Login failed for user: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
//        }
//    }

}

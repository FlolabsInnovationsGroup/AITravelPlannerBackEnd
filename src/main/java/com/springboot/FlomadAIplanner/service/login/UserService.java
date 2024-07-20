package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.entities.login.VerificationToken;
import com.springboot.FlomadAIplanner.model.repository.login.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;

    @SuppressWarnings("all")
    @Transactional
    public User registerUser(User user) {
        log.debug("Registering user with username: {}", user.getUsername());

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Use the injected PasswordEncoder
        user.setIsEmailVerified(false);


        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenService.saveVerificationToken(verificationToken);
        log.info("Verification token created for user with ID: {}", savedUser.getId());

        sendVerificationEmail(savedUser);
        log.debug("Verification email sent to user with ID: {}", savedUser.getId());

        return savedUser;
    }

    private void sendVerificationEmail(User user) {
        String subject = "Email Verification";
        String verificationToken = verificationTokenService.getVerificationTokenByUserId(user.getId()).getId();

        String text = "To verify your email, please click the following link: " +
                "http://localhost:8080/api/auth/verify?token=" + verificationToken;

        emailService.sendSimpleMessage(user.getEmail(), subject, text);
        log.info("Verification email sent to the following email: {}", user.getEmail());
    }

    public User findByVerificationToken(String token) {
        log.debug("Finding user by verification token.");
        VerificationToken verificationToken = verificationTokenService.getVerificationTokenById(token);
        return verificationToken != null ? verificationToken.getUser() : null;
    }

    @Transactional
    public void verifyUser(User user) {
        log.debug("Verifying user with ID: {}", user.getId());
        // update the user fields

        user.setIsEmailVerified(true);
        userRepository.save(user);
        // delete the verification token after successful verification
        verificationTokenService.deleteVerificationTokenByUserId(user.getId());
        log.info("User verified successfully with ID: {}", user.getId());
    }

    public User findUserByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found with username: {}", username);
        } else {
            log.debug("User found: {}", user);
        }
        return user;
    }

    @SuppressWarnings("unused")
    public User findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public User findByUsernameOrEmail(String identifier) {
        log.debug("Finding user by username or email: {}", identifier);
        return userRepository.findByUsernameOrEmail(identifier);
    }

    public boolean existsByUsername(String username) {
        log.debug("Checking if user exists by username: {}", username);
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }
}


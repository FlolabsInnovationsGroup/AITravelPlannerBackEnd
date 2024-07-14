package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.entities.login.VerificationToken;
import com.springboot.FlomadAIplanner.model.repository.login.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    public UserService(UserRepository userRepository, EmailService emailService, VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
    }

    @SuppressWarnings("all")
    @Transactional
    public User registerUser(User user) {
        log.debug("Registering user with username: {}", user.getUsername());

        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setIsEmailVerified(false);
        user.setEnabled(false); // Set the default value for enabled field
        user.setVerified(false); // Set the default value for isVerified field

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
        log.info("Verification email sent to user with username: {}", user.getUsername());
    }

    public User findByVerificationToken(String token) {
        log.debug("Finding user by verification token.");
        return userRepository.findUserByVerificationToken(token);
    }

    public void verifyUser(User user) {
        log.debug("Verifying user with ID: {}", user.getId());
        user.setVerified(true);
        user.setIsEmailVerified(true);
        userRepository.save(user);
        verificationTokenService.deleteVerificationTokenByUserId(user.getId());
        log.info("User verified successfully with ID: {}", user.getId());
    }

    public User findUserByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }
}

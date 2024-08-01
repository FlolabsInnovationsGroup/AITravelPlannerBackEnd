package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.entities.login.VerificationToken;
import com.springboot.FlomadAIplanner.model.repository.login.UserRepository;
import com.springboot.FlomadAIplanner.service.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements CrudService<User, String> {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsEmailVerified(false);
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with ID: {}", savedUser.getId());

        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenService.save(verificationToken);
        log.info("Verification token created for user with ID: {}", savedUser.getId());

        sendVerificationEmail(savedUser);
        log.debug("Verification email sent to user with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User findById(String id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    @Override
    public User update(String id, User updatedUser) {
        log.debug("Updating user with ID: {}", id);
        updatedUser.setId(id);
        return userRepository.save(updatedUser);
    }

    @Override
    public void delete(String id) {
        log.debug("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    @Transactional
    @SuppressWarnings("all")
    public User registerUser(User user) {
        log.debug("Registering user with username: {}", user.getUsername());
        return save(user);
    }

    private void sendVerificationEmail(User user) {
        String subject = "Email Verification";
        String verificationToken = verificationTokenService.getVerificationTokenByUserId(user.getId()).getId();
        String text = "To verify your email, please click the following link: " +
                "https://aitravelplannerbackend-production.up.railway.app/api/auth/verify?token=" + verificationToken;
        emailService.sendSimpleMessage(user.getEmail(), subject, text);
        log.info("Verification email sent to: {}", user.getEmail());
    }

    public User findByVerificationToken(String token) {
        log.debug("Finding user by verification token.");
        VerificationToken verificationToken = verificationTokenService.getVerificationTokenById(token);
        return verificationToken != null ? verificationToken.getUser() : null;
    }

    @Transactional
    public void verifyUser(User user) {
        log.debug("Verifying user with ID: {}", user.getId());
        user.setIsEmailVerified(true);
        userRepository.save(user);
        verificationTokenService.deleteVerificationTokenByUserId(user.getId());
        log.info("User verified successfully with ID: {}", user.getId());
    }

    public User findUserByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
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

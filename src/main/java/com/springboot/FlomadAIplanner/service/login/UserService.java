package com.springboot.FlomadAIplanner.service.login;

import com.google.api.client.util.Value;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.repository.login.UserRepository;
import com.springboot.FlomadAIplanner.service.login.EmailService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${verification.url}")
    private String verificationUrl;

    public void registerUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEnabled(false);
        userRepository.save(user);

        String verificationLink = verificationUrl + "?token=" + user.getVerificationToken();
        emailService.sendSimpleMessage(user.getEmail(), "Email Verification", "Please click on the link to verify your email: " + verificationLink);
    }

    public User findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    public void verifyUser(User user) {
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public UserDetails loadUserByUsername(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadUserByUsername'");
    }
}

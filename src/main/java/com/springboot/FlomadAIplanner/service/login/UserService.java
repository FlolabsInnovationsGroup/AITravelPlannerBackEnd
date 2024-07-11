package com.springboot.FlomadAIplanner.service.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.repository.login.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerUser(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setIsEmailVerified(false);
        user.setEnabled(false); // Set the default value for enabled field
        user.setIsVerified(false); // Set the default value for isVerified field
        
        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);

        return savedUser;
    }

    private void sendVerificationEmail(User user) {
        String subject = "Email Verification";
        String text = "To verify your email, please click the following link: " +
                "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken();
        emailService.sendSimpleMessage(user.getEmail(), subject, text);
    }

    public User findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    public void verifyUser(User user) {
        user.setIsVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public UserDetails loadUserByUsername(String username) {
        return (UserDetails) userRepository.findByUsername(username);
    }
}

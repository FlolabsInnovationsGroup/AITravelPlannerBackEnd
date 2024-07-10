package com.springboot.FlomadAIplanner.controller.login;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.login.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody User user) {
        try {
            userService.registerUser(user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully. Please check your email for verification instructions.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyUser(@RequestParam("token") String token) {
        Map<String, String> response = new HashMap<>();
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            response.put("message", "Invalid verification token.");
            return ResponseEntity.badRequest().body(response);
        }
        userService.verifyUser(user);
        response.put("message", "Email verified successfully.");
        return ResponseEntity.ok(response);
    }
}

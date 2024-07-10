package com.springboot.FlomadAIplanner.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    @GetMapping("/signup")
    public String signupForm() {
        return "signup"; // Return the view name for the signup form
    }

    @PostMapping("/signup")
    public String signupSubmit() {
        // Handle the signup form submission
        return "verify"; // Redirect to login page after successful signup
    }
    
}

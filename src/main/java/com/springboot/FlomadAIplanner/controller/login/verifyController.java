package com.springboot.FlomadAIplanner.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
public class verifyController {
    @GetMapping("/verify")
    public String verify() {
        return "verification.html"; // matches verification.html in templates
    }
}


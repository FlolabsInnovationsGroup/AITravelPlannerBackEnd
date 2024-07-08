package com.springboot.FlomadAIplanner.controller.login;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // This should match the name of your login view template (e.g., login.html or login.jsp)
    }
}

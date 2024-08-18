package com.springboot.FlomadAIplanner.handler.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${application.front-end.url}")
    private String frontUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception) throws IOException {
        log.info("OAuth2 Error, redirect to /login-error");
        response.sendRedirect(frontUrl + "/login-error");
    }
}
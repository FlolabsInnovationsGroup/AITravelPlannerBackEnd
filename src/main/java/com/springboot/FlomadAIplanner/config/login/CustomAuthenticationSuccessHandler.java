package com.springboot.FlomadAIplanner.config.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.login.AuthService;
import com.springboot.FlomadAIplanner.service.login.OAuth2Service;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;

    @Value("${application.front-end.url}")
    private String frontUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauthUser = oauthToken.getPrincipal();
            String username = oauthUser.getAttribute("name");
            String email = oauthUser.getAttribute("email");
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            UserDTO userDTO = new UserDTO(username, "", email, registrationId);
            String token = oauth2Service.handleOAuthLogin(userDTO);

            response.sendRedirect(frontUrl + "/auth-redirect?token=" + token);

//            log.info("registrationId: {}", registrationId);
        } else {
            throw new IllegalArgumentException("Authentication is not an instance of OAuth2AuthenticationToken");
        }
    }
}
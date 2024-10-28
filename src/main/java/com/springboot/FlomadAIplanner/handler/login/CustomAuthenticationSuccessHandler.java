package com.springboot.FlomadAIplanner.handler.login;

import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.service.login.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
            log.info("OAuth User: {} try login with: {}", username, registrationId);

            UserAuthDTO userAuthDTO = new UserAuthDTO(username, "", email, registrationId);
            log.debug("User: {}", userAuthDTO);

            String token = oauth2Service.handleOAuthLogin(userAuthDTO);
            log.debug("OAuth User {} Login Success! Token: {}", userAuthDTO.getUsername(), token);

            response.sendRedirect(frontUrl + "/auth-redirect?token=" + token);

        } else {
            throw new IllegalArgumentException("Authentication is not an instance of OAuth2AuthenticationToken");
        }
    }
}
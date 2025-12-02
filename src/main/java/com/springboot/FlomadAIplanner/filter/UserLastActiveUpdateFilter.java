package com.springboot.FlomadAIplanner.filter;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLastActiveUpdateFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Update user's last active timestamp
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        if (user != null) {
            userService.updateLastActive(user);
            log.debug("Updated last active timestamp for user: {}", user.getUsername());
        }

        // Proceed with the next filter in the chain
        chain.doFilter(request, response);
    }
}

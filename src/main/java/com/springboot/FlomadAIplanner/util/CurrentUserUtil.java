package com.springboot.FlomadAIplanner.util;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserUtil {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public CurrentUserUtil(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @SuppressWarnings("unused")
    public User getCurrentUsernameFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            return userService.findUserByUsername(username);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public User getCurrentUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userService.findUserByUsername(username);
        }
        return null;
    }
}

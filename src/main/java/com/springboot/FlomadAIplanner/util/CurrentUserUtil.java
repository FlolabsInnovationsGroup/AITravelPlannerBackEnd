package com.springboot.FlomadAIplanner.util;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrentUserUtil {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public CurrentUserUtil(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    public User getCurrentUsernameFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            return userService.findByUsernameOrEmail(username);
        }
        return null;
    }

    public User getCurrentUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            log.warn("No authentication found in security context");
            return null;
        }
        
        log.info("Authentication found: {}", authentication.getName());
        log.info("Principal type: {}", authentication.getPrincipal().getClass().getSimpleName());
        
        if (authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            log.info("Extracting username from UserDetails: {}", username);
            User user = userService.findByUsernameOrEmail(username);
            if (user != null) {
                log.info("User found in database: {}", user.getUsername());
            } else {
                log.warn("User not found in database for username: {}", username);
            }
            return user;
        }
        
        log.warn("Principal is not UserDetails: {}", authentication.getPrincipal());
        return null;
    }
}

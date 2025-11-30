package com.springboot.FlomadAIplanner.util;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrentUserUtil {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public CurrentUserUtil(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public User getCurrentUserFromRequest(HttpServletRequest request) {
        // First try JWT authentication
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            User user = userService.findByUsernameOrEmail(username);
            if (user != null) {
                return user;
            }
        }
        
        // Fallback to session-based authentication
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute("userId");
            if (userId != null) {
                return userService.findById(userId);
            }
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

    public String getCurrentUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("userId");
        }
        return null;
    }

    public String getCurrentUsernameFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("username");
        }
        return null;
    }
}

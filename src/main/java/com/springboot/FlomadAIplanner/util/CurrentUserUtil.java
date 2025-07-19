package com.springboot.FlomadAIplanner.util;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserUtil {

    private final UserService userService;

    public CurrentUserUtil(UserService userService) {
        this.userService = userService;
    }

    @SuppressWarnings("unused")
    public User getCurrentUserFromRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute("userId");
            if (userId != null) {
                return userService.findById(userId);
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public User getCurrentUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userService.findByUsernameOrEmail(username);
        }
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

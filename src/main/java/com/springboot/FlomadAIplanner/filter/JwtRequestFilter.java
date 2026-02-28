package com.springboot.FlomadAIplanner.filter;

import com.springboot.FlomadAIplanner.service.security.CustomUserDetailsService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    // List of public endpoints that should skip JWT validation
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/signup",
            "/api/auth/signin",
            "/api/auth/verify",
            "/api/auth/guest-token",
            "/api/chat/call",
            "/api/chat/stream",
            "/api/chat/call/entity",
            "/api/audio/transcribe",
            "/api/locations",
            "/ws",
            "/oauth2",
            "/login/oauth2"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Check if path matches any public endpoint
        boolean isPublic = PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> path.startsWith(endpoint));
        
        if (isPublic) {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
        }
        
        return isPublic;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException {

        try {

            final String authorizationHeader = request.getHeader("Authorization");
            log.info("authorizationHeader:{}", authorizationHeader);

            String username = null;
            String jwt = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails;
                if (username.startsWith("guest_")) {
                    // Guest tokens: no DB user; create synthetic UserDetails with ROLE_GUEST so validation and auth succeed
                    userDetails = User.withUsername(username)
                            .password("")
                            .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                            .build();
                } else {
                    userDetails = this.customUserDetailsService.loadUserByUsername(username);
                }

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            log.info("Filtering request:{}", request);
            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired");
        } catch (Exception e) {
            log.error("Exception during JWT authentication: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        }
    }
}

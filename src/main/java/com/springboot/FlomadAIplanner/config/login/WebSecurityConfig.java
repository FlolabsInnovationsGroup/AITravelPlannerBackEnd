package com.springboot.FlomadAIplanner.config.login;

import com.springboot.FlomadAIplanner.filter.JwtRequestFilter;
import com.springboot.FlomadAIplanner.filter.UserLastActiveUpdateFilter;
import com.springboot.FlomadAIplanner.handler.login.CustomAuthenticationFailureHandler;
import com.springboot.FlomadAIplanner.handler.login.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${application.cors.allowed.origins}")
    private String[] allowedOrigins;

    private final UserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final UserLastActiveUpdateFilter userLastActiveUpdateFilter;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public WebSecurityConfig(UserDetailsService userDetailsService,
                             JwtRequestFilter jwtRequestFilter,
                             UserLastActiveUpdateFilter userLastActiveUpdateFilter,
                             PasswordEncoder passwordEncoder,
                             CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                             CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.userLastActiveUpdateFilter = userLastActiveUpdateFilter;
        this.passwordEncoder = passwordEncoder;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    private static final List<String> PUBLIC_URLS = List.of(
            "/api/health",
            "/api/audio/transcribe",
            "/api/auth/signup",
            "/api/auth/signin",
            "/api/auth/verify",
            "/api/auth/session-state",
            "/api/chat/call",
            "/api/chat/stream",
            "/api/chat/call/entity",
            "/api/locations/**",
            "/users/current",
            "/sessions/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/ws/**"
    );

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList(allowedOrigins));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "Cookie"));
                    config.setExposedHeaders(List.of("Set-Cookie"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))

                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/api/auth/session-state")
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_URLS.toArray(new String[0])).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/actuator/health",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers("/api/auth/session-state").permitAll()
                        .requestMatchers("/api/auth/guest-token").permitAll()
                        .requestMatchers("/users/current").authenticated()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        // ⚠️ Fix: Change to exact paths to avoid conflicting with PUBLIC_URLS /api/chat/call/entity
                        .requestMatchers("/api/chat", "/api/stream", "/api/chat/history/**").hasAnyRole("ADMIN", "USER", "GUEST")

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization.baseUri("/oauth2/authorization"))
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // For API-style requests, return JSON 401 instead of redirecting (avoids "too many redirects" in browser)
                            String requestURI = request.getRequestURI();
                            boolean isApiOrDataRequest = requestURI.startsWith("/api/")
                                    || requestURI.startsWith("/users/")
                                    || requestURI.startsWith("/sessions");
                            boolean isLoginPage = "/login".equals(requestURI) || requestURI.startsWith("/login?");
                            if (isApiOrDataRequest || isLoginPage) {
                                // Return 401 JSON to avoid redirect loops (browser following redirect to /login repeatedly)
                                response.setContentType("application/json");
                                response.setStatus(401);
                                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(userLastActiveUpdateFilter, JwtRequestFilter.class);

        return http.build();
    }
}

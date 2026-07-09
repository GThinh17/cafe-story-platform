package com.cafestory.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cafestory.until.security.JwtAuthenticationFilter;
import com.cafestory.until.security.RagHmacAuthFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RagHmacAuthFilter ragHmacAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RagHmacAuthFilter ragHmacAuthFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.ragHmacAuthFilter = ragHmacAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response,
                        authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/usernames/suggestions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/regions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/stripe/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/stripe-connect/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/vnpay/return", "/api/payments/vnpay/ipn")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/assistant/tools/**").permitAll()
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reviewers/payouts/**", "/api/reviewers/badges/**").hasRole("ADMIN")
                        .requestMatchers("/api/reviewers/connect/**").hasRole("REVIEWER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(ragHmacAuthFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:3636",
                "http://127.0.0.1:3636",
                "http://localhost:3001",
                "http://127.0.0.1:3001",
                "http://localhost:8081",
                "http://127.0.0.1:8081"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

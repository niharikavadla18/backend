package com.example.pharmacy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> {})

            .formLogin(form -> form.disable())

            .httpBasic(basic -> basic.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Allow CORS preflight requests
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // Register and login are public
                .requestMatchers(
                    "/user/register",
                    "/user/login"
                ).permitAll()

                // Public medicine GET APIs
                .requestMatchers(
                    HttpMethod.GET,
                    "/medicine/all",
                    "/medicine/search",
                    "/medicine/{id}"
                ).permitAll()

                // Only ADMIN can add medicines
                .requestMatchers(
                    HttpMethod.POST,
                    "/medicine/add"
                ).hasRole("ADMIN")

                // Only ADMIN can update medicines
                .requestMatchers(
                    HttpMethod.PUT,
                    "/medicine/update/**"
                ).hasRole("ADMIN")

                // Only ADMIN can delete medicines
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/medicine/delete/**"
                ).hasRole("ADMIN")

                // Only ADMIN can view all orders
                .requestMatchers(
                    HttpMethod.GET,
                    "/order/all"
                ).hasRole("ADMIN")

                // Only ADMIN can update order status
                .requestMatchers(
                    HttpMethod.PUT,
                    "/order/status/**"
                ).hasRole("ADMIN")

                // Logged-in users can place/view orders
                .requestMatchers(
                    "/order/place",
                    "/order/user/**",
                    "/order-item/**"
                ).authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
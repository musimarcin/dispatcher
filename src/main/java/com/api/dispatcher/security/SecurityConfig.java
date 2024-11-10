package com.api.dispatcher.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailService userDetailService;
    private final JWTAuthEntryPoint authEntryPoint;

    @Autowired
    public SecurityConfig(CustomUserDetailService userDetailService, JWTAuthEntryPoint authEntryPoint) {
        this.userDetailService = userDetailService;
        this.authEntryPoint = authEntryPoint;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())  // attack that tricks a user’s browser into making unwanted requests to a different website (no need in REST API)
//                .cors(httpSecurityCorsConfigurer -> )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/vehicles").authenticated(); // need authentication to use this endpoint
                    auth.requestMatchers("/api/admin/**").hasRole("ADMIN"); // need ADMIN role to use this endpoint
                })
                .httpBasic(httpBasic -> {});
        httpSecurity.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

}

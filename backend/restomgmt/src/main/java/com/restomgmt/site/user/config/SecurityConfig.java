package com.restomgmt.site.user.config;

import com.restomgmt.site.user.filter.JwtRequestFilter;
import com.restomgmt.site.user.security.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
@Profile("!uat")
public class SecurityConfig {
    
    private final AuthenticationService authenticationService;

    private final JwtRequestFilter jwtAuthFilter;

    private final PasswordEncoder passwordEncoder;

    //decoupled property extractions from application.properties to avoid hardcoding
    @Value("${security.public-urls}")
    private String[] publicUrls;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigin;

    @Value("${security.role-hierarchy}")
    private String rawRoleHierarchy;

    @Bean 
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
            authenticationManagerBuilder
                .userDetailsService(authenticationService)
                .passwordEncoder(passwordEncoder);

            return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("SecurityConfig filterChain loading");
        log.debug("PUBLIC URLS: {}", java.util.Arrays.toString(publicUrls));
        http
            .authorizeHttpRequests(auth->auth
                .requestMatchers(publicUrls).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session->session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            //.csrf(AbstractHttpConfigurer::disable)
            .cors(cors->cors.configurationSource(corsConfigurationSource()));
            
            log.debug("PUBLIC URLS: {}", java.util.Arrays.toString(publicUrls));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(allowedOrigin));//see .env
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    //has to do with the permissions stuff, might be moved later
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(rawRoleHierarchy);
    }
}


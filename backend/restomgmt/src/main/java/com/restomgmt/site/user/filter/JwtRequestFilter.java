package com.restomgmt.site.user.filter;

import com.restomgmt.site.user.security.AuthenticationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.restomgmt.site.user.util.JwtUtil;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;

    private final AuthenticationService authenticationService;

    @Override 
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
              throws ServletException,
                     IOException {
                final String authorizationHeader = request.getHeader("Authorization");

                String username = null;
                String jwt = null;

                log.debug("Filtering request {} {}", request.getMethod(), request.getRequestURI());

                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    jwt = authorizationHeader.substring(7);
                    username = jwtUtil.extractUsername(jwt);
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // 1. Validate the token first before doing any heavy lifting or context setting
                    if (jwtUtil.validateToken(jwt, username)) {
                        
                        // 2. Extract roles directly from the JWT claims (Stateless approach)
                        List<GrantedAuthority> authorities = jwtUtil.extractRoles(jwt);
                        
                        // 3. Create the authentication token using the username string directly
                        UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                                
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 4. Set the security context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Set security context for user={}", username);
                        
                    } else {
                        log.debug("Invalid JWT for user={}", username);
                    }
                }

                // Always ensure the filter chain continues outside the block
                chain.doFilter(request, response);
              }
}

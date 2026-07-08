package com.restomgmt.site.user.filter;

import com.restomgmt.site.user.security.AuthenticationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.restomgmt.site.user.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
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

                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    jwt = authorizationHeader.substring(7);
                    username = jwtUtil.extractUsername(jwt);
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.authenticationService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);        
                    }
                }
                chain.doFilter(request, response);
              }
}

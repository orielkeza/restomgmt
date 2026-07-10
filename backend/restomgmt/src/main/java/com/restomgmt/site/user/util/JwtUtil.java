package com.restomgmt.site.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtil {
    //this is the signing key, should not be hardcoded used env vars
    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-ms}")
    private long expirationMs;


    //generic helper function that decodes the token into a claims object the applies what extrator was passed by type, <T> means it works for nay return type
   public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //verify token's signature with secret and returns all the claims
     private Claims extractAllClaims(String token) {
        //return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        //the old way of parsing and validating the jwt, used to cuase worry over state capture and unexpected behaviour becuase it was mutable and not always thread safe
        //this is the updated way (immutable, thread-safe, enhanced security, improved readability)
        //covert string secret to secure SecretKey object
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        //parsing the token
        return Jwts.parser()
              .verifyWith(secretKey)
              .build()
              .parseSignedClaims(token)
              .getPayload();  
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        log.debug("Generating token for user={}", username);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        String token = createToken(claims, username);
        log.trace("Token created (truncated) for user={}", username);
        return token;
    }

    public List<GrantedAuthority> extractRoles(String token) {
    Claims claims = extractAllClaims(token);
    List<String> roles = claims.get("roles", List.class);
    if (roles == null) return List.of();
    return roles.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
}

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().claims(claims)
                             .subject(subject)
                             .issuedAt(new Date(System.currentTimeMillis()))
                             .expiration(new Date(System.currentTimeMillis() + expirationMs))
                             //.signWith(SignatureAlgorithm.HS256, secret)
                             .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                             .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        boolean valid = (extractedUsername.equals(username) && !isTokenExpired(token));
        log.debug("Validating token for user={} -> {}", username, valid);
        return valid;
    }
}
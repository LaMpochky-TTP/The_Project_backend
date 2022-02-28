package com.lampocky.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String bearerTokenPrefix = "Bearer_";
    private final String secret;
    private final Long tokenExpiredInMillis;
    private final UserDetailsService service;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.token.secret}") String secret,
                            @Value("${jwt.token.expired}") Long tokenExpiredInMillis,
                            UserDetailsService service) {
        this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
        this.tokenExpiredInMillis = tokenExpiredInMillis;
        this.service = service;
    }

    public String createToken(String email){
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenExpiredInMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith(bearerTokenPrefix)){
            return bearerToken.substring(bearerTokenPrefix.length());
        } else {
            return null;
        }
    }

    public Authentication getAuthentication(String token){
        UserDetails userDetails = service.loadUserByUsername(getEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getEmail(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValid(String token){
        try {
            Date expiration = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration();
            return !expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
//            throw new JwtAuthenticationException("JWT token is expired or invalid");
        }
    }
}

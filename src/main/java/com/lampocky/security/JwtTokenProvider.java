package com.lampocky.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final Logger log = LogManager.getLogger(JwtTokenProvider.class);
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
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenExpiredInMillis);

        return createToken(email, now, expiration);
    }

    public String createToken(String email, Date from, Date to) {
        Claims claims = Jwts.claims().setSubject(email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(from)
                .setNotBefore(from)
                .setExpiration(to)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith(bearerTokenPrefix)){
            log.debug("Token resolving success");
            return bearerToken.substring(bearerTokenPrefix.length());
        } else {
            log.debug("Token resolving fail");
            return null;
        }
    }

    public Authentication getAuthentication(String token) throws UsernameNotFoundException {
        try{
            UserDetails userDetails = service.loadUserByUsername(getEmail(token));
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        } catch (UsernameNotFoundException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    public String getEmail(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValid(String token){
        try {
            Date expiration = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration();
            return !expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }
}

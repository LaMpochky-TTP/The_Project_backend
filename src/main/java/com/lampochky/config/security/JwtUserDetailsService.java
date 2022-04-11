package com.lampochky.config.security;

import com.lampochky.database.entity.User;
import com.lampochky.database.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final Logger log = LogManager.getLogger(JwtUserDetailsService.class);
    private UserService userService;

    @Autowired
    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userService.findByEmail(email);
        if(user.isPresent()) {
            return new UserSecurity(user.get());
        } else {
            throw new UsernameNotFoundException("No user with email " + email + " found");
        }
    }
}

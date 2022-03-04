package com.lampocky.controller;

import com.lampocky.database.entity.User;
import com.lampocky.database.service.UserService;
import com.lampocky.dto.request.LoginRequestDto;
import com.lampocky.dto.request.RegisterRequestDto;
import com.lampocky.dto.response.LoginResponseDto;
import com.lampocky.dto.response.RegisterResponseDto;
import com.lampocky.security.JwtTokenProvider;
import com.lampocky.validation.UserValidation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final Logger log = LogManager.getLogger(AuthenticationController.class);
    private final UserValidation userValidation;
    private final UserService userService;
    private final JwtTokenProvider provider;
    private final AuthenticationManager authManager;

    @Autowired
    public AuthenticationController(UserService userService, JwtTokenProvider provider,
                                    AuthenticationManager authManager) {
        this.userService = userService;
        this.provider = provider;
        this.authManager = authManager;
        this.userValidation = new UserValidation(userService);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request){
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword())); // checks if user exists and password is correct
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("User authenticated by email {}, but were not found", request.getEmail());
                        return new UsernameNotFoundException("No user were found via email " + request.getEmail());
                    });
            String token = provider.createToken(user.getEmail());

            return ResponseEntity.ok(LoginResponseDto.success(user.getUsername(), user.getEmail(), token));
        } catch (AuthenticationException ex){
            return ResponseEntity.ok(LoginResponseDto.fail(request.getEmail(), "wrong email or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto request){
        if(userValidation.validate(request)){
            User user = new User(request.getUsername(), request.getPassword(), request.getEmail());
            user = userService.save(user);
            String token = provider.createToken(user.getEmail());

            return ResponseEntity.ok(RegisterResponseDto
                    .success(user.getUsername(), user.getEmail(), token));
        } else {
            return ResponseEntity.ok(RegisterResponseDto
                    .fail(request.getUsername(), request.getEmail(), userValidation.getErrors()));
        }
    }

    // temporal endpoint for login testing only
    // TODO remove this endpoint
    @GetMapping("/login-test")
    public ResponseEntity loginTest(){
        List<LoginResponseDto> result = userService.findAll()
                .stream().map(user -> LoginResponseDto
                        .success(user.getUsername(), user.getEmail(), null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}

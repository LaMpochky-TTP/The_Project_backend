package com.lampochky.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lampochky.database.entity.User;
import com.lampochky.database.service.UserService;
import com.lampochky.dto.request.auth.LoginRequestDto;
import com.lampochky.dto.request.auth.RegisterRequestDto;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.dto.response.aurh.LoginResponseDto;
import com.lampochky.dto.response.aurh.RegisterResponseDto;
import com.lampochky.dto.response.user.UserDto;
import com.lampochky.config.security.JwtTokenProvider;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.validation.Error;
import com.lampochky.validation.UserValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Iterator;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final Logger log = LogManager.getLogger(AuthenticationController.class);
    private final UserValidator userValidation;
    private final UserService userService;
    private final JwtTokenProvider provider;
    private final AuthenticationManager authManager;

    @Autowired
    public AuthenticationController(UserService userService, JwtTokenProvider provider,
                                    AuthenticationManager authManager) {
        this.userService = userService;
        this.provider = provider;
        this.authManager = authManager;
        this.userValidation = new UserValidator(userService);
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
            System.out.println(ex.getMessage());
            return ResponseEntity.ok(LoginResponseDto.fail(request.getEmail(), Error.LOGIN_FAIL));
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

    @RequestMapping("/access_denied")
    public ResponseEntity<ResponseDto> accessDenied() {
        System.out.println("ACCESS_DENIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseDto(Collections.singletonList(Error.AUTHENTICATION_FAIL)));
    }

    // temporal endpoint for login testing only
    // TODO remove this endpoint
    @GetMapping("/login-test")
    public ResponseEntity<UserDto> loginTest(@AuthenticationPrincipal UserSecurity userSecurity){
        return ResponseEntity.ok(DtoBuilder.buildUserDto(userSecurity.getUser()));
    }

    @PostMapping("/test")
    public ResponseEntity<UserDto> test(@AuthenticationPrincipal UserSecurity userSecurity,
                                        @RequestBody ObjectNode node){
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String field = it.next();
            System.out.println(field + " = " + node.get(field));
        }
        return ResponseEntity.ok(DtoBuilder.buildUserDto(userSecurity.getUser()));
    }
}

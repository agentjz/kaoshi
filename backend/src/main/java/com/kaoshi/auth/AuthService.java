package com.kaoshi.auth;

import com.kaoshi.auth.dto.CurrentUserResponse;
import com.kaoshi.auth.dto.LoginRequest;
import com.kaoshi.auth.dto.LoginResponse;
import com.kaoshi.security.AuthUser;
import com.kaoshi.security.JwtProperties;
import com.kaoshi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return new LoginResponse(
                jwtService.issue(user),
                "Bearer",
                jwtProperties.accessTokenMinutes() * 60,
                toCurrentUser(user)
        );
    }

    public CurrentUserResponse toCurrentUser(AuthUser user) {
        return new CurrentUserResponse(user.id(), user.username(), user.displayName(), user.roles(), user.permissions());
    }
}


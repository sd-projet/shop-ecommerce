package com.shop.presentation.controller;

import com.shop.application.usecase.auth.AuthResult;
import com.shop.application.usecase.auth.LoginUseCase;
import com.shop.application.usecase.auth.RegisterUserUseCase;
import com.shop.presentation.dto.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase register;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase register, LoginUseCase login) {
        this.register = register;
        this.login = login;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return toResponse(register.execute(new RegisterUserUseCase.Command(
                request.email(), request.password(), request.firstName(), request.lastName())));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(login.execute(new LoginUseCase.Command(request.email(), request.password())));
    }

    private AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.token(), UserResponse.from(result.user()));
    }
}

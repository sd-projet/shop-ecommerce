package com.shop.presentation.controller;

import com.shop.application.usecase.auth.ProfileUseCase;
import com.shop.infrastructure.security.AuthenticatedUser;
import com.shop.presentation.dto.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final ProfileUseCase profile;

    public UserController(ProfileUseCase profile) {
        this.profile = profile;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return UserResponse.from(profile.get(principal.id()));
    }

    @PutMapping
    public UserResponse update(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody UpdateProfileRequest request) {
        return UserResponse.from(profile.update(principal.id(), request.firstName(), request.lastName()));
    }
}

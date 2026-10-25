package com.shop.infrastructure.security;

import com.shop.domain.model.Role;

import java.util.UUID;

/** Principal stored in the Spring Security context for an authenticated request. */
public record AuthenticatedUser(UUID id, String email, Role role) {
    public boolean isAdmin() {
        return role == Role.ROLE_ADMIN;
    }
}

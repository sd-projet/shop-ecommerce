package com.shop.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String email;
    private final String passwordHash;
    private String firstName;
    private String lastName;
    private final Role role;
    private final Instant createdAt;

    public User(UUID id, String email, String passwordHash, String firstName, String lastName, Role role, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = Objects.requireNonNull(role);
        this.createdAt = createdAt;
    }

    public static User create(String email, String passwordHash, String firstName, String lastName, Role role) {
        return new User(UUID.randomUUID(), email.trim().toLowerCase(), passwordHash, firstName, lastName, role, Instant.now());
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean isAdmin() {
        return role == Role.ROLE_ADMIN;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}

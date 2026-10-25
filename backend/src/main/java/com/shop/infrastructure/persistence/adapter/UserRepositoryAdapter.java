package com.shop.infrastructure.persistence.adapter;

import com.shop.domain.model.Role;
import com.shop.domain.model.User;
import com.shop.domain.repository.UserRepository;
import com.shop.infrastructure.persistence.entity.UserEntity;
import com.shop.infrastructure.persistence.jpa.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User u) {
        return toDomain(jpa.save(new UserEntity(u.getId(), u.getEmail(), u.getPasswordHash(), u.getFirstName(),
                u.getLastName(), u.getRole(), u.getCreatedAt())));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public long countByRole(Role role) {
        return jpa.countByRole(role);
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPasswordHash(), e.getFirstName(), e.getLastName(), e.getRole(), e.getCreatedAt());
    }
}

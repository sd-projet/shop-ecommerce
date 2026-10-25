package com.shop.application.usecase.auth;

import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.User;
import com.shop.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileUseCase {

    private final UserRepository users;

    public ProfileUseCase(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public User get(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User update(UUID userId, String firstName, String lastName) {
        User user = get(userId);
        user.updateProfile(firstName, lastName);
        return users.save(user);
    }
}

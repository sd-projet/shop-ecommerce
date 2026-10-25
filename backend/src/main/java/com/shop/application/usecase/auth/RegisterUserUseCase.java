package com.shop.application.usecase.auth;

import com.shop.application.port.PasswordHasher;
import com.shop.application.port.TokenIssuer;
import com.shop.domain.exception.EmailAlreadyUsedException;
import com.shop.domain.model.Role;
import com.shop.domain.model.User;
import com.shop.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

    public record Command(String email, String password, String firstName, String lastName) {}

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokens;

    public RegisterUserUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResult execute(Command command) {
        String email = command.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("An account already exists for " + email);
        }
        User user = User.create(email, hasher.hash(command.password()), command.firstName(), command.lastName(), Role.ROLE_CUSTOMER);
        User saved = users.save(user);
        return new AuthResult(tokens.issue(saved), saved);
    }
}

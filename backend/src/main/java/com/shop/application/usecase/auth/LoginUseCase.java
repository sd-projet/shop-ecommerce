package com.shop.application.usecase.auth;

import com.shop.application.port.PasswordHasher;
import com.shop.application.port.TokenIssuer;
import com.shop.domain.exception.InvalidCredentialsException;
import com.shop.domain.model.User;
import com.shop.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    public record Command(String email, String password) {}

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokens;

    public LoginUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public AuthResult execute(Command command) {
        User user = users.findByEmail(command.email().trim().toLowerCase())
                .filter(u -> hasher.matches(command.password(), u.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        return new AuthResult(tokens.issue(user), user);
    }
}

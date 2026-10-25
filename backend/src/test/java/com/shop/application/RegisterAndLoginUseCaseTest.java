package com.shop.application;

import com.shop.application.port.PasswordHasher;
import com.shop.application.port.TokenIssuer;
import com.shop.application.usecase.auth.LoginUseCase;
import com.shop.application.usecase.auth.RegisterUserUseCase;
import com.shop.domain.exception.EmailAlreadyUsedException;
import com.shop.domain.exception.InvalidCredentialsException;
import com.shop.domain.model.Role;
import com.shop.domain.model.User;
import com.shop.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterAndLoginUseCaseTest {

    @Mock UserRepository users;
    @Mock PasswordHasher hasher;
    @Mock TokenIssuer tokens;

    @Test
    void registerHashesPasswordAndCreatesCustomer() {
        when(users.existsByEmail("jane@example.com")).thenReturn(false);
        when(hasher.hash("secret123")).thenReturn("HASH");
        when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokens.issue(any())).thenReturn("jwt");

        var result = new RegisterUserUseCase(users, hasher, tokens)
                .execute(new RegisterUserUseCase.Command(" Jane@Example.com ", "secret123", "Jane", "Doe"));

        assertThat(result.token()).isEqualTo("jwt");
        assertThat(result.user().getEmail()).isEqualTo("jane@example.com");
        assertThat(result.user().getPasswordHash()).isEqualTo("HASH");
        assertThat(result.user().getRole()).isEqualTo(Role.ROLE_CUSTOMER);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> new RegisterUserUseCase(users, hasher, tokens)
                .execute(new RegisterUserUseCase.Command("jane@example.com", "secret123", "Jane", "Doe")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(users, never()).save(any());
    }

    @Test
    void loginFailsWithWrongPassword() {
        User user = User.create("jane@example.com", "HASH", "Jane", "Doe", Role.ROLE_CUSTOMER);
        when(users.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(hasher.matches("bad", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> new LoginUseCase(users, hasher, tokens)
                .execute(new LoginUseCase.Command("jane@example.com", "bad")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        User user = User.create("jane@example.com", "HASH", "Jane", "Doe", Role.ROLE_CUSTOMER);
        when(users.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(hasher.matches("secret123", "HASH")).thenReturn(true);
        when(tokens.issue(user)).thenReturn("jwt");

        var result = new LoginUseCase(users, hasher, tokens).execute(new LoginUseCase.Command("jane@example.com", "secret123"));

        assertThat(result.token()).isEqualTo("jwt");
    }
}

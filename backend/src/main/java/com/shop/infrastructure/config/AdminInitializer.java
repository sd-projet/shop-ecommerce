package com.shop.infrastructure.config;

import com.shop.application.port.PasswordHasher;
import com.shop.domain.model.Role;
import com.shop.domain.model.User;
import com.shop.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Creates the default administrator on first start. Override credentials through environment variables. */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final String email;
    private final String password;

    public AdminInitializer(UserRepository users, PasswordHasher hasher,
                            @Value("${app.admin.email}") String email,
                            @Value("${app.admin.password}") String password) {
        this.users = users;
        this.hasher = hasher;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!users.existsByEmail(email)) {
            users.save(User.create(email, hasher.hash(password), "Admin", "Shop", Role.ROLE_ADMIN));
            log.info("Default administrator created: {}", email);
        }
    }
}

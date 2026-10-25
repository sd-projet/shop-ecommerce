package com.shop.application.usecase.auth;

import com.shop.domain.model.User;

public record AuthResult(String token, User user) {
}

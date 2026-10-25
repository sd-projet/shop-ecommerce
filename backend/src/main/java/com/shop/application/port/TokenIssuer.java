package com.shop.application.port;

import com.shop.domain.model.User;

public interface TokenIssuer {
    String issue(User user);
}

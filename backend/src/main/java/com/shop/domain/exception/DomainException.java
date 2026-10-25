package com.shop.domain.exception;

/** Base type for every business-rule violation raised by the domain or application layers. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

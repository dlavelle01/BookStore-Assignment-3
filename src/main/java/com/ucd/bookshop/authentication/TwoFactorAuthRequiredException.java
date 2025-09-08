package com.ucd.bookshop.authentication;

import org.springframework.security.core.AuthenticationException;

public class TwoFactorAuthRequiredException extends AuthenticationException {
    public TwoFactorAuthRequiredException(String msg) { super(msg); }
}

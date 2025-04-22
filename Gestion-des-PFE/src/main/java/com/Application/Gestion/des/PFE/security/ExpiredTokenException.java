package com.Application.Gestion.des.PFE.security;

// ExpiredTokenException.java
public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}


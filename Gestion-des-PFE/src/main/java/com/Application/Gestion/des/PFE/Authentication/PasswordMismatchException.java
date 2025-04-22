package com.Application.Gestion.des.PFE.Authentication;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}

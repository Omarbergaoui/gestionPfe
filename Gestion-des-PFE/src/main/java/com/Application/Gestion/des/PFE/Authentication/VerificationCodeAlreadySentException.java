package com.Application.Gestion.des.PFE.Authentication;

public class VerificationCodeAlreadySentException extends RuntimeException {
    public VerificationCodeAlreadySentException(String message) {
        super(message);
    }
}
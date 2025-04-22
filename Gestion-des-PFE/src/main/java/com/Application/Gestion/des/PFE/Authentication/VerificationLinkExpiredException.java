package com.Application.Gestion.des.PFE.Authentication;

public class VerificationLinkExpiredException extends RuntimeException {
    public VerificationLinkExpiredException(String message) {
        super(message);
    }
}

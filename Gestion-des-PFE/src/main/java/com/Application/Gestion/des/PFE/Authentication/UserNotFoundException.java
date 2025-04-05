package com.Application.Gestion.des.PFE.Authentication;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}


package com.Application.Gestion.des.PFE.enseignant;

public class EnseignantAlreadyExistsException extends RuntimeException {
    public EnseignantAlreadyExistsException(String message) {
        super(message);
    }
}


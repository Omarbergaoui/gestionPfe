package com.Application.Gestion.des.PFE.enseignant;

public class EnseignantNotFoundException extends RuntimeException {
    public EnseignantNotFoundException(String id) {
        super("Enseignant avec l'ID " + id + " non trouvé.");
    }
}

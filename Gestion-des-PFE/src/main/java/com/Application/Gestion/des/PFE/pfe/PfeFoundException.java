package com.Application.Gestion.des.PFE.pfe;

public class PfeFoundException extends RuntimeException {
    public PfeFoundException(String id) {
        super("Enseignant avec l'ID " + id + " non trouvé.");
    }
}

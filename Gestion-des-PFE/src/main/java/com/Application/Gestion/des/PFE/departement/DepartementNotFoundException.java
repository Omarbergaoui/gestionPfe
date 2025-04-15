package com.Application.Gestion.des.PFE.departement;

public class DepartementNotFoundException extends RuntimeException{
    public DepartementNotFoundException(String id) {
        super("Département avec l'ID " + id + " non trouvé.");
    }
}

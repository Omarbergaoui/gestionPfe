package com.Application.Gestion.des.PFE.chefdepartement;

public class ChefDepartementNotFoundException extends RuntimeException {
    public ChefDepartementNotFoundException(String idDepartement) {
        super("Aucun chef de département trouvé pour l'ID : " + idDepartement);
    }
}

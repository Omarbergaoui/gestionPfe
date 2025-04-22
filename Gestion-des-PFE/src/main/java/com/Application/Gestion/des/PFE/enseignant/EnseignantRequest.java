package com.Application.Gestion.des.PFE.enseignant;

public record EnseignantRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String matiere,
        String DepartementName
) {
}

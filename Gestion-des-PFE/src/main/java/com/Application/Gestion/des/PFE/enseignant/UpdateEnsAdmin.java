package com.Application.Gestion.des.PFE.enseignant;

// Add targetId
public record UpdateEnsAdmin(
        String targetId, // ID of the Enseignant to update
        String firstName,
        String lastName,
        String matiere, // Keep case consistent with Enseignant entity (matiere)
        String role,    // Role name as String (e.g., "ENSEIGNANT", "CHEFDEPARTEMENT")
        String DepartementId // ID of the new department
) {
}
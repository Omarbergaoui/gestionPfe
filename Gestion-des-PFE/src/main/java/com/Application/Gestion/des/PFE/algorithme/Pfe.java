package com.Application.Gestion.des.PFE.algorithme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Class representing a PFE (Projet de Fin d'Études) entry
// Made mutable for salle and dateHeure which are assigned by the GA
public class Pfe {
    private final String emailetudiant;
    private final String titre;
    private final String encadrantId;
    private final String rapporteurId;
    private final String presidentId;
    private String salle; // Mutable
    private LocalDateTime dateHeure; // Mutable

    public Pfe(String emailetudiant, String titre, String encadrantId, String rapporteurId, String presidentId, String salle, LocalDateTime dateHeure) {
        this.emailetudiant = emailetudiant;
        this.titre = titre;
        this.encadrantId = encadrantId;
        this.rapporteurId = rapporteurId;
        this.presidentId = presidentId;
        this.salle = salle;
        this.dateHeure = dateHeure;
    }

    // Copy constructor for creating deep copies within the GA
    public Pfe(Pfe original) {
        this.emailetudiant = original.emailetudiant;
        this.titre = original.titre;
        this.encadrantId = original.encadrantId;
        this.rapporteurId = original.rapporteurId;
        this.presidentId = original.presidentId;
        this.salle = original.salle;
        this.dateHeure = original.dateHeure; // LocalDateTime is immutable, so direct assignment is fine
    }

    // Getters
    public String getEmailetudiant() { return emailetudiant; }
    public String getTitre() { return titre; }
    public String getEncadrantId() { return encadrantId; }
    public String getRapporteurId() { return rapporteurId; }
    public String getPresidentId() { return presidentId; }
    public String getSalle() { return salle; }
    public LocalDateTime getDateHeure() { return dateHeure; }

    // Setters for mutable fields
    public void setSalle(String salle) { this.salle = salle; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    // Helper to get all involved teacher IDs, filtering nulls/blanks
    public List<String> getInvolvedTeachers() {
        return Stream.of(encadrantId, rapporteurId, presidentId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Pfe{" +
                "id='" + emailetudiant + '\'' +
                ", salle='" + salle + '\'' +
                ", dateHeure=" + (dateHeure != null ? dateHeure.toString() : "null") +
                ", encadrantId='" + encadrantId + '\'' +
                // ... add other fields if needed for debugging
                '}';
    }
}

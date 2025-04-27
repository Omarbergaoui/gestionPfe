package com.Application.Gestion.des.PFE.departement;

import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonManagedReference; // <-- Import this
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef; // Keep DBRef for MongoDB linking
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Departement")
@Builder
public class Departement {
    @Id
    private String id;
    private String nom;

    @DBRef(lazy = true)
    @JsonManagedReference("dept-chef")
    private Enseignant chefdepartement;
}
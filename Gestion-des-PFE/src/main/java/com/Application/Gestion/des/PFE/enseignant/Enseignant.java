package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference; // <-- Import this
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef; // Keep DBRef
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document(collection = "users")
public class Enseignant extends UserEntity {
    private List<LocalDateTime> disponibilite = new ArrayList<>();
    private String matiere;

    @DBRef(lazy = true) // Indicates to MongoDB how to link/load this
    @JsonBackReference("dept-chef") // Tells Jackson this is the 'back' part (use same name)
    private Departement departementId;
}
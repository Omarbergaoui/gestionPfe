package com.Application.Gestion.des.PFE.departement;


import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import lombok.*;
import org.springframework.data.annotation.Id;
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
    private Enseignant chefdepartement;
}

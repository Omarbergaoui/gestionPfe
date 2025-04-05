package com.Application.Gestion.des.PFE.departement;


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
}

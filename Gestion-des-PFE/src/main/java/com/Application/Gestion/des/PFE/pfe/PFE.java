package com.Application.Gestion.des.PFE.pfe;

import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.planning.Planning;
import com.Application.Gestion.des.PFE.salle.Salle;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "PFE")
public class PFE {
    @Id
    private String id;
    private String etudiantemail;
    private String titrerapport;
    private Enseignant encadreur;
    private Enseignant president;
    private Enseignant rapporteur;
    private LocalDateTime dateheure;
    private Salle salle;
    private Planning planningid;
}

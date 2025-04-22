package com.Application.Gestion.des.PFE.planning;


import com.Application.Gestion.des.PFE.salle.Salle;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Planning")
public class Planning {
    @Id
    private String id;
    private List<Salle> salles;
    private LocalDate datedebut;
    private LocalDate datefin;
    private String anneeuniversitaire;
}

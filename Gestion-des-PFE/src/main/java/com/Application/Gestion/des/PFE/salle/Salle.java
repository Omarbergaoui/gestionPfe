package com.Application.Gestion.des.PFE.salle;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "salle")
public class Salle {
    @Id
    private String id;
    private String nom;
    private List<LocalDateTime> disponibilite;
}

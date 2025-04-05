package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.user.UserEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
    @DBRef
    private Departement departementId;
}

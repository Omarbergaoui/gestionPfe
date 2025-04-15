package com.Application.Gestion.des.PFE.Dtos;

import com.Application.Gestion.des.PFE.departement.Departement;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantDto extends UserDto {
    @Builder.Default
    private List<LocalDateTime> disponibilite = new ArrayList<>();
    private String matiere;
    private Departement departementId;
}

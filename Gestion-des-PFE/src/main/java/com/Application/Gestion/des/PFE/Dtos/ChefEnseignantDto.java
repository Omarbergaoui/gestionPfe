package com.Application.Gestion.des.PFE.Dtos;

import com.Application.Gestion.des.PFE.departement.Departement;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChefEnseignantDto extends EnseignantDto {
    private Departement chefDepartementId;
}

package com.Application.Gestion.des.PFE.Dtos;

import com.Application.Gestion.des.PFE.Dtos.UserDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true) // Important for inheritance
@SuperBuilder // Inherits builder methods from UserDto
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantDto extends UserDto {
    @Builder.Default
    private List<LocalDateTime> disponibilite = new ArrayList<>();
    private String matiere;

    private String departementId;
}
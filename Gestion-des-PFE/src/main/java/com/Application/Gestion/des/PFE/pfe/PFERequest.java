package com.Application.Gestion.des.PFE.pfe;

import java.time.LocalDateTime;

public record PFERequest(
        String emailetudiant,
        String nomderapport,
        String encadrant,
        String president,
        String rapporteur,
        LocalDateTime dateTime,
        String Salle
) {
}

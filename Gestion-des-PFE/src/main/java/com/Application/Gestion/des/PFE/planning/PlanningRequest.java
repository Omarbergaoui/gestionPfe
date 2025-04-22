package com.Application.Gestion.des.PFE.planning;

import com.Application.Gestion.des.PFE.salle.Salle;

import java.time.LocalDate;
import java.util.List;

public record PlanningRequest(
        LocalDate dateDebut,
        LocalDate dateFin,
        List<String> salleids
) {
}

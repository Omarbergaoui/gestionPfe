package com.Application.Gestion.des.PFE.planning;

import java.time.LocalDate;

public record PlanningStartEndDate(
        LocalDate start,
        LocalDate end
) {
}

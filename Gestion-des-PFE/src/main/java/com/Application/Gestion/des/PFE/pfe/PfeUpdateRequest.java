package com.Application.Gestion.des.PFE.pfe;

public record PfeUpdateRequest(
        String id,
        String nomderapport,
        String president,
        String rapporteur,
        String dateTime,
        String salle
) {
}

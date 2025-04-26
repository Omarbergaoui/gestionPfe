package com.Application.Gestion.des.PFE.algorithme;


import com.Application.Gestion.des.PFE.departement.DepartementRepository;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.salle.Salle;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Algorithme {

    private final SalleRepository salleRepository;
    private final EnseignantRepository enseignantRepository;
    private final DepartementRepository departementRepository;


    



}

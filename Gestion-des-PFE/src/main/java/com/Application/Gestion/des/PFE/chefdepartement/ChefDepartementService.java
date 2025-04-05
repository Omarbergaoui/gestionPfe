package com.Application.Gestion.des.PFE.chefdepartement;

import com.Application.Gestion.des.PFE.departement.DepartementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChefDepartementService {
    private final ChefDepartementRepository chefDepartementRepository;
    private final DepartementService departementService;

    /*public ChefDepartement getChefDepartementById(DepartementRequest departementRequest) {
        if()
        return chefDepartementRepository.findByDepartementId(departementRequest.id()).orElse(null);
    }*/
}

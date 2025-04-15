package com.Application.Gestion.des.PFE.chefdepartement;

import com.Application.Gestion.des.PFE.departement.DepartementService;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChefDepartementService {
    private final ChefDepartementRepository chefDepartementRepository;
    private final DepartementService departementService;


    public List<ChefDepartement> getAllChefs(){
        return chefDepartementRepository.findAll();
    }

    public ChefDepartement getChefDepartementById(RequestIdChef requestIdChef){
        if(chefDepartementRepository.findById(requestIdChef.id()).isPresent()){
            throw new ChefDepartementNotFoundException(requestIdChef.id());
        }
        return chefDepartementRepository.findById(requestIdChef.id()).get();
    }

    public ChefDepartement getChefDepartementByEmail(RequestNameChef requestNameChef){
        if(chefDepartementRepository.findByEmail(requestNameChef.Email()).isPresent()){
            throw new ChefDepartementNotFoundException(requestNameChef.Email());
        }
        return chefDepartementRepository.findByEmail(requestNameChef.Email()).get();
    }

    public ChefDepartement UpdateChefDepartementById(RequestIdChef requestIdChef,ChefDepartementRequestUpdate chefDepartementRequestUpdate){
        ChefDepartement chefDepartement=getChefDepartementById(requestIdChef);
        chefDepartement.setMatiere(chefDepartementRequestUpdate.Matiere());
        chefDepartement.setFirstname(chefDepartementRequestUpdate.firstName());
        chefDepartement.setLastname(chefDepartementRequestUpdate.lastName());
        return chefDepartementRepository.save(chefDepartement);
    }


    public ChefDepartement UpdateChefDepartementByEmail(RequestNameChef requestNameChef,ChefDepartementRequestUpdate chefDepartementRequestUpdate){
        ChefDepartement chefDepartement=getChefDepartementByEmail(requestNameChef);
        chefDepartement.setMatiere(chefDepartementRequestUpdate.Matiere());
        chefDepartement.setFirstname(chefDepartementRequestUpdate.firstName());
        chefDepartement.setLastname(chefDepartementRequestUpdate.lastName());
        return chefDepartementRepository.save(chefDepartement);
    }

    public String DeleteChefDepartementByEmail(RequestNameChef requestNameChef){
        ChefDepartement chefDepartement = getChefDepartementByEmail(requestNameChef);
        chefDepartementRepository.delete(chefDepartement);
        return "Chef Departement supprimé Avec Succées";
    }

    public String DeleteChefDepartementById(RequestIdChef requestIdChef){
        ChefDepartement chefDepartement = getChefDepartementById(requestIdChef);
        chefDepartementRepository.delete(chefDepartement);
        return "Chef Departement supprimé Avec Succées";
    }
}

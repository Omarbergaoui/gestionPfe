package com.Application.Gestion.des.PFE.departement;
import com.Application.Gestion.des.PFE.chefdepartement.ChefDepartementRepository;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartementService {
    private final DepartementRepository departementRepository;
    private final ChefDepartementRepository chefDepartementRepository;
    private final EnseignantRepository enseignantRepository;

    public Departement createDepartement(DepartementReq departement) {
        if (departementRepository.findByNom(departement.Name()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le département existe déjà.");
            }
            return departementRepository.save(Departement.builder().nom(departement.Name()).build());
    }

    public Departement getDepartementById(DepartementRequest req) {
        Optional<Departement> departement = departementRepository.findById(req.id());
        if (departement.isPresent()) {
            return departement.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé.");
        }
    }

    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    public Departement getDepartementByNom(DepartementReq req) {
        Optional<Departement> departement = departementRepository.findByNom(req.Name());
        if (departement.isPresent()) {
            return departement.get();
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"le départemnt n'existe pas");
        }
    }


    public Departement updateDepartementById(DepartementRequest request, DepartementReq departement) {
        var b=departementRepository.findById(request.id());
        if(b.isPresent()) {
            b.get().setNom(departement.Name());
            return departementRepository.save(b.get());
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé.");
        }
    }

    /*public void deleteDepartement(DepartementRequest departementRequest) {
        try {
            List<Enseignant> enseignantsList = getAllEnseignants(departementRequest);
            enseignantsList.forEach(enseignant -> enseignant.setDepartementId(null));
            enseignantRepository.saveAll(enseignantsList);
            ChefDepartement currentChef = getChefDepartementById(id);
            if (currentChef != null) {
                Enseignant ens = Enseignant.builder()
                        .id(currentChef.getId())
                        .firstname(currentChef.getFirstname())
                        .lastname(currentChef.getLastname())
                        .email(currentChef.getEmail())
                        .password(currentChef.getPassword())
                        .role(Role.ENSEIGNANT)
                        .disponibilite(currentChef.getDisponibilite())
                        .matiere(currentChef.getMatiere())
                        .departementId(null)
                        .build();

                enseignantRepository.deleteById(currentChef.getId());
                ensRepository.save(ens);
                departementRepository.deleteById(id);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé.");
        }
    }

    /*
    public String affecterChefDepartement(String idEnseignant, String idDepartement) {
        if (!departementRepository.existsById(idDepartement)) {
            return "Département non trouvé.";
        }

        var departementOpt = departementRepository.findById(idDepartement);
        if (!ensRepository.existsById(idEnseignant)) {
            return "Enseignant non trouvé.";
        }

        var enseignantOpt = ensRepository.findById(idEnseignant);
        Enseignant enseignant = enseignantOpt.get();

        if (!enseignant.getDepartementId().getId().equals(idDepartement)) {
            return "L'enseignant n'appartient pas à ce département.";
        }

        ChefDepartement currentChef = getChefDepartementById(idDepartement);
        if (currentChef == null) {

            ChefDepartement newChef =ChefDepartement.builder()
                    .id(enseignant.getId())
                    .firstname(enseignant.getFirstname())
                    .lastname(enseignant.getLastname())
                    .email(enseignant.getEmail())
                    .departementId(enseignant.getDepartementId())
                    .password(enseignant.getPassword())
                    .role(Role.CHEFDEPARTEMENT)
                    .disponibilite(enseignant.getDisponibilite())
                    .matiere(enseignant.getMatiere())
                    .ChefDepartementId(departementOpt.get()).build();
            ensRepository.deleteById(enseignant.getId());
            enseignantRepository.save(newChef);
            return "Chef de département affecté avec succès.";
        } else {

            Enseignant oldChef = Enseignant.builder()
                    .id(currentChef.getId())
                    .firstname(currentChef.getFirstname())
                    .lastname(currentChef.getLastname())
                    .email(currentChef.getEmail())
                    .departementId(currentChef.getDepartementId())
                    .password(currentChef.getPassword())
                    .role(Role.ENSEIGNANT)
                    .disponibilite(currentChef.getDisponibilite())
                    .matiere(currentChef.getMatiere())
                    .build();
            enseignantRepository.deleteById(oldChef.getId());
            ensRepository.save(oldChef);

            ChefDepartement newChef =ChefDepartement.builder()
                    .id(enseignant.getId())
                    .firstname(enseignant.getFirstname())
                    .lastname(enseignant.getLastname())
                    .email(enseignant.getEmail())
                    .departementId(enseignant.getDepartementId())
                    .password(enseignant.getPassword())
                    .role(Role.CHEFDEPARTEMENT)
                    .disponibilite(enseignant.getDisponibilite())
                    .matiere(enseignant.getMatiere())
                    .ChefDepartementId(departementOpt.get()).build();
            ensRepository.deleteById(enseignant.getId());
            enseignantRepository.save(newChef);
            return "Le chef de département a été changé avec succés.";
        }
    }

*/
    public List<Enseignant> getAllEnseignants(DepartementRequest departementReq) {
        return enseignantRepository.findByDepartementId(getDepartementById(departementReq));
    }
/*

    public List<ChefDepartement> getAllChefDepartement(){
        return enseignantRepository.findAll().stream().filter(en -> en.getRole() == Role.CHEFDEPARTEMENT).collect(Collectors.toList());
    }
    */

/*
    public String deleteChefDepartement(String idDepartement) {
        ChefDepartement currentChef = getChefDepartementById(idDepartement);
        if (currentChef != null) {
            Enseignant ens = Enseignant.builder()
                    .id(currentChef.getId())
                    .firstname(currentChef.getFirstname())
                    .lastname(currentChef.getLastname())
                    .email(currentChef.getEmail())
                    .password(currentChef.getPassword())
                    .role(Role.ENSEIGNANT)
                    .disponibilite(currentChef.getDisponibilite())
                    .matiere(currentChef.getMatiere())
                    .departementId(null)
                    .build();

            enseignantRepository.deleteById(currentChef.getId());
            ensRepository.save(ens);
            return "chef département supprimé avec succée";
        }
        else{
            return "le chef département n'existe pas";
        }

    }

    public List<Departement> getDepartementssanschef() {
        Set<Departement> departementsAvecChef = new HashSet<>();

        ensRepository.findAll().forEach(ens -> {
            if (ens.getDepartementId() != null) {
                departementsAvecChef.add(ens.getDepartementId());
            }
        });
        return getAllDepartements().stream()
                .filter(departement -> !departementsAvecChef.contains(departement.getId()))
                .collect(Collectors.toList());
    }


    public List<Departement> getDepartementsavecchef(){
        List<Departement> departements = new ArrayList<>();
        ensRepository.findAll().forEach(ens -> {
            if (ens.getDepartementId() != null) {
                departements.add(ens.getDepartementId());
            }
        });
        return departements;
    }*/
}

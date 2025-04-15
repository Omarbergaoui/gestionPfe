package com.Application.Gestion.des.PFE.departement;
import com.Application.Gestion.des.PFE.chefdepartement.ChefDepartement;
import com.Application.Gestion.des.PFE.chefdepartement.ChefDepartementNotFoundException;
import com.Application.Gestion.des.PFE.chefdepartement.ChefDepartementRepository;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantNotFoundException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.enumeration.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

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

    public void deleteDepartement(DepartementRequest departementRequest) {
        try {
            List<Enseignant> enseignantsList = getAllEnseignants(departementRequest);
            enseignantsList.forEach(enseignant -> enseignant.setDepartementId(null));
            enseignantRepository.saveAll(enseignantsList);
            ChefDepartement currentChef = getChefDepartementById(departementRequest.id());
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

                chefDepartementRepository.deleteById(currentChef.getId());
                enseignantRepository.save(ens);
                departementRepository.deleteById(departementRequest.id());
            }
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé.");
        }
    }


    public String affecterChefDepartement(String idEnseignant, String idDepartement) {
        try {
            var departement = departementRepository.findById(idDepartement)
                    .orElseThrow(() -> new DepartementNotFoundException(idDepartement));

            var enseignant = enseignantRepository.findById(idEnseignant)
                    .orElseThrow(() -> new EnseignantNotFoundException(idEnseignant));


            if (!enseignant.getDepartementId().getId().equals(idDepartement)) {
                return "L'enseignant n'appartient pas à ce département.";
            }

            ChefDepartement currentChef = getChefDepartementById(idDepartement);

            if (currentChef == null) {
                ChefDepartement newChef = ChefDepartement.builder()
                        .id(enseignant.getId())
                        .firstname(enseignant.getFirstname())
                        .lastname(enseignant.getLastname())
                        .email(enseignant.getEmail())
                        .departementId(enseignant.getDepartementId())
                        .password(enseignant.getPassword())
                        .role(Role.CHEFDEPARTEMENT)
                        .disponibilite(enseignant.getDisponibilite())
                        .matiere(enseignant.getMatiere())
                        .ChefDepartementId(departement)
                        .build();

                enseignantRepository.deleteById(enseignant.getId());
                chefDepartementRepository.save(newChef);

                return "Chef de département affecté avec succès.";
            }
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

            chefDepartementRepository.deleteById(oldChef.getId());
            enseignantRepository.save(oldChef);

            ChefDepartement newChef = ChefDepartement.builder()
                    .id(enseignant.getId())
                    .firstname(enseignant.getFirstname())
                    .lastname(enseignant.getLastname())
                    .email(enseignant.getEmail())
                    .departementId(enseignant.getDepartementId())
                    .password(enseignant.getPassword())
                    .role(Role.CHEFDEPARTEMENT)
                    .disponibilite(enseignant.getDisponibilite())
                    .matiere(enseignant.getMatiere())
                    .ChefDepartementId(departement)
                    .build();

            enseignantRepository.deleteById(enseignant.getId());
            chefDepartementRepository.save(newChef);

            return "Le chef de département a été changé avec succès.";

        } catch (DepartementNotFoundException | EnseignantNotFoundException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Erreur lors de l'affectation du chef de département : " + e.getMessage();
        }
    }



    public List<Enseignant> getAllEnseignants(DepartementRequest departementReq) {
        return enseignantRepository.findByDepartementId(getDepartementById(departementReq));
    }

    public String deleteChefDepartement(String idDepartement) {
        try {
            ChefDepartement currentChef = chefDepartementRepository
                    .findByDepartementId(idDepartement)
                    .orElseThrow(() -> new ChefDepartementNotFoundException(idDepartement));
            Enseignant ens = Enseignant.builder()
                    .id(currentChef.getId())
                    .firstname(currentChef.getFirstname())
                    .lastname(currentChef.getLastname())
                    .email(currentChef.getEmail())
                    .password(currentChef.getPassword())
                    .role(Role.ENSEIGNANT)
                    .disponibilite(currentChef.getDisponibilite())
                    .matiere(currentChef.getMatiere())
                    .departementId(currentChef.getDepartementId())
                    .build();
            chefDepartementRepository.deleteById(currentChef.getId());
            enseignantRepository.save(ens);
            return "Chef de département supprimé avec succès.";
        } catch (ChefDepartementNotFoundException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Erreur lors de la suppression du chef de département : " + e.getMessage();
        }
    }


    public ChefDepartement getChefDepartementById(String idDepartement) {
        return chefDepartementRepository.findByDepartementId(idDepartement)
                .orElseThrow(() -> new RuntimeException("Aucun chef de département trouvé pour l'ID : " + idDepartement));
    }


    public List<Departement> getDepartementssanschef() {
        Set<Departement> departementsAvecChef = new HashSet<>();

        chefDepartementRepository.findAll().forEach(ens -> {
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
        chefDepartementRepository.findAll().forEach(ens -> {
            if (ens.getDepartementId() != null) {
                departements.add(ens.getDepartementId());
            }
        });
        return departements;
    }
}

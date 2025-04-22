package com.Application.Gestion.des.PFE.departement;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantNotFoundException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.enumeration.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartementService {
    private final DepartementRepository departementRepository;
    private final EnseignantRepository enseignantRepository;

    public Departement createDepartement(DepartementReq departement) {
        if (departementRepository.findByNom(departement.Name()).isPresent()) {
                throw new DepartmentNotFoundException("A department with this name already exists.");
        }
        if(enseignantRepository.findById(departement.Chefdepartementid()).isEmpty()){
                throw new EnseignantNotFoundException("User not found");
        }
        Enseignant enseignant = enseignantRepository.findById(departement.Chefdepartementid()).get();
        if(enseignant.getRole().equals(Role.CHEFDEPARTEMENT)|| enseignant.getRole().equals(Role.ADMIN)){
            throw new EnseignantNotFoundException("User not found");
        }
        Departement departement1= departementRepository.save(
                Departement.builder()
                        .nom(departement.Name())
                        .chefdepartement(enseignantRepository.findById(departement.Chefdepartementid()).get())
                        .build()
        );
        enseignant.setRole(Role.CHEFDEPARTEMENT);
        enseignant.setDepartementId(departement1);
        enseignantRepository.save(enseignant);
        return departement1;
    }

    public Departement getDepartementById(DepartementRequest req) {
        Optional<Departement> departement = departementRepository.findById(req.id());
        if (departement.isPresent()) {
            return departement.get();
        } else {
            throw new DepartmentNotFoundException("The specified department does not exist.");
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
            throw new DepartmentNotFoundException("The specified department does not exist.");
        }
    }


    public Departement updateDepartementById(DepartementRequest request, DepartementName departement) {
        var b=departementRepository.findById(request.id());
        if(b.isPresent()) {
            if (departementRepository.findByNom(departement.Name()).isPresent()) {
                throw new DepartmentNotFoundException("A department with this name already exists.");
            }
            b.get().setNom(departement.Name());
            return departementRepository.save(b.get());
        }
        else{
            throw new DepartmentNotFoundException("The specified department does not exist.");
        }
    }

    public Departement updateDepartementChefById(DepartementRequest request,RequestIdChef requestIdChef){
        Departement departement = getDepartementById(request);
        if(enseignantRepository.findById(requestIdChef.id()).isEmpty()){
            throw new EnseignantNotFoundException("User not found");
        }
        Enseignant nouveauenseignant = enseignantRepository.findById(requestIdChef.id()).get();
        if(nouveauenseignant.getRole().equals(Role.ADMIN) || nouveauenseignant.getRole().equals(Role.CHEFDEPARTEMENT)){
            throw new EnseignantNotFoundException("User not found");
        }
        if(enseignantRepository.findById(departement.getChefdepartement().getId()).isEmpty()){
            throw new EnseignantNotFoundException("User not found");
        }
        Enseignant ancienenseignant = enseignantRepository.findById(departement.getChefdepartement().getId()).get();
        ancienenseignant.setRole(Role.ENSEIGNANT);
        enseignantRepository.save(ancienenseignant);
        nouveauenseignant.setRole(Role.CHEFDEPARTEMENT);
        nouveauenseignant.setDepartementId(departement);
        departement.setChefdepartement(nouveauenseignant);
        enseignantRepository.save(nouveauenseignant);
        return departementRepository.save(departement);
    }


    public String deleteDepartement(DepartementRequest departementToDeleteRequest, DepartementRequest reassignmentDepartementRequest) {
        Departement departementToDelete = getDepartementById(departementToDeleteRequest);
        Departement reassignmentDepartement = getDepartementById(reassignmentDepartementRequest);
        List<Enseignant> enseignants = enseignantRepository.findByDepartementId(departementToDelete);
        enseignants.forEach(enseignant -> {
            enseignant.setRole(Role.ENSEIGNANT);
            enseignant.setDepartementId(reassignmentDepartement);
        });
        enseignantRepository.saveAll(enseignants);
        departementRepository.delete(departementToDelete);
        return "Department successfully deleted and enseignants reassigned.";
    }



    public List<EnseignantDto> getAllEnseignants(DepartementRequest departementReq) {
        return enseignantRepository.findByDepartementId(getDepartementById(departementReq)).stream()
                .map(enseignant -> {
                    return EnseignantDto.builder()
                            .id(enseignant.getId())
                            .firstName(enseignant.getFirstname())
                            .lastName(enseignant.getLastname())
                            .role(enseignant.getRole())
                            .email(enseignant.getEmail())
                            .matiere(enseignant.getMatiere())
                            .departementId(enseignant.getDepartementId())
                            .disponibilite(enseignant.getDisponibilite())
                            .build();
                })
                .collect(Collectors.toList());
    }


    public EnseignantDto getChefDepartementById(DepartementRequest departementRequest) {
        Departement departement =  getDepartementById(departementRequest);
        if(enseignantRepository.findById(departement.getChefdepartement().getId()).isEmpty()){
            throw new EnseignantNotFoundException("User not found");
        }
        Enseignant enseignant = enseignantRepository.findById(departement.getChefdepartement().getId()).get();
        return EnseignantDto.builder()
                .id(enseignant.getId())
                .firstName(enseignant.getFirstname())
                .lastName(enseignant.getLastname())
                .role(enseignant.getRole())
                .email(enseignant.getEmail())
                .matiere(enseignant.getMatiere())
                .departementId(enseignant.getDepartementId())
                .disponibilite(enseignant.getDisponibilite())
                .build();
    }

    public List<EnseignantDto> getAllChefs(){
        return departementRepository.findAll()
                .stream()
                .map(departement -> {
                    if(enseignantRepository.findById(departement.getChefdepartement().getId()).isEmpty()){
                        throw new EnseignantNotFoundException("User not found");
                    }
                    Enseignant enseignant = enseignantRepository.findById(departement.getChefdepartement().getId()).get();
                    return  EnseignantDto.builder()
                            .id(enseignant.getId())
                            .firstName(enseignant.getFirstname())
                            .lastName(enseignant.getLastname())
                            .role(enseignant.getRole())
                            .email(enseignant.getEmail())
                            .matiere(enseignant.getMatiere())
                            .departementId(enseignant.getDepartementId())
                            .disponibilite(enseignant.getDisponibilite())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

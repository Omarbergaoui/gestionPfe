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

    public Departement createDepartement(DepartementReq departementReq) {
        if (departementRepository.findByNom(departementReq.Name()).isPresent()) {
                throw new DepartmentNotFoundException("A department with this name already exists.");
        }
        return departementRepository.save(
                Departement.builder()
                        .nom(departementReq.Name())
                        .chefdepartement(null)
                        .build()
        );
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
        if(nouveauenseignant.getRole().equals(Role.ADMIN) || nouveauenseignant.getRole().equals(Role.CHEFDEPARTEMENT) || !nouveauenseignant.getDepartementId().equals(departement)){
            throw new EnseignantNotFoundException("User not found");
        }
        if(enseignantRepository.findById(departement.getChefdepartement().getId()).isEmpty()){
            throw new EnseignantNotFoundException("User not found");
        }
        Enseignant ancienenseignant = enseignantRepository.findById(departement.getChefdepartement().getId()).get();
        ancienenseignant.setRole(Role.ENSEIGNANT);
        enseignantRepository.save(ancienenseignant);
        nouveauenseignant.setRole(Role.CHEFDEPARTEMENT);
        departement.setChefdepartement(nouveauenseignant);
        enseignantRepository.save(nouveauenseignant);
        return departementRepository.save(departement);
    }

    public Departement AffecterChefDepartment(DepartementRequest request,RequestIdChef requestIdChef){
        Departement departement = getDepartementById(request);
        if(departement.getChefdepartement()!=null){
            throw new ChefDepartementFound("This department already has a designated head.");
        }
        if(enseignantRepository.findById(requestIdChef.id()).isEmpty()){
            throw new EnseignantNotFoundException("User not found");
        }
        Enseignant nouveauenseignant = enseignantRepository.findById(requestIdChef.id()).get();
        if(nouveauenseignant.getRole().equals(Role.ADMIN) || nouveauenseignant.getRole().equals(Role.CHEFDEPARTEMENT) || !nouveauenseignant.getDepartementId().equals(departement)){
            throw new EnseignantNotFoundException("User not found");
        }
        departement.setChefdepartement(nouveauenseignant);
        nouveauenseignant.setRole(Role.CHEFDEPARTEMENT);
        enseignantRepository.save(nouveauenseignant);
        return departementRepository.save(departement);
    }

    public Departement RemoveChefDepartement(DepartementRequest request){
        Departement departement = getDepartementById(request);
        if(enseignantRepository.findById(departement.getChefdepartement().getId()).isEmpty()){
            throw new EnseignantNotFoundException("User not found");
        }
        Enseignant ancienenseignant = enseignantRepository.findById(departement.getChefdepartement().getId()).get();
        ancienenseignant.setRole(Role.ENSEIGNANT);
        enseignantRepository.save(ancienenseignant);
        departement.setChefdepartement(null);
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
        if(departement.getChefdepartement()==null){
            throw new EnseignantNotFoundException("This department does not have a head assigned yet");
        }
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

    public List<EnseignantDto> getAllChefs() {
        return departementRepository.findAll().stream()
                .map(Departement::getChefdepartement)
                .filter(Objects::nonNull)
                .map(chef -> enseignantRepository.findById(chef.getId())
                        .orElseThrow(() -> new EnseignantNotFoundException("User not found")))
                .map(enseignant -> EnseignantDto.builder()
                        .id(enseignant.getId())
                        .firstName(enseignant.getFirstname())
                        .lastName(enseignant.getLastname())
                        .role(enseignant.getRole())
                        .email(enseignant.getEmail())
                        .matiere(enseignant.getMatiere())
                        .departementId(enseignant.getDepartementId())
                        .disponibilite(enseignant.getDisponibilite())
                        .build())
                .collect(Collectors.toList());
    }

}

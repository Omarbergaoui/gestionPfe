package com.Application.Gestion.des.PFE.planning;


import com.Application.Gestion.des.PFE.disponibilte.DisponibiliteRequest;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateFormatException;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.pfe.PFE;
import com.Application.Gestion.des.PFE.pfe.PfeRepository;
import com.Application.Gestion.des.PFE.salle.Salle;
import com.Application.Gestion.des.PFE.salle.SalleNotFoundException;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import com.Application.Gestion.des.PFE.salle.SalleRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlanningService {
    private final PlanningRepository planningRepository;
    private final PfeRepository pfeRepository;
    private final SalleRepository salleRepository;
    private final EnseignantRepository enseignantRepository;


    private String getAnneeUniversitaire() {
        LocalDate date = LocalDate.now();
        int year = date.getYear();
        Month month = date.getMonth();
        if (month.getValue() >= 9) {
            return year + "/" + (year + 1);
        } else {
            return (year - 1) + "/" + year;
        }
    }

    private void validateAnneeUniversitaireFormat(String annee) {
        if (annee == null || !annee.matches("\\d{4}/\\d{4}")) {
            throw new InvalidDateFormatException("Invalid format. Expected format: yyyy/yyyy+1");
        }

        String[] parts = annee.split("/");
        int year1 = Integer.parseInt(parts[0]);
        int year2 = Integer.parseInt(parts[1]);

        if (year2 != year1 + 1) {
            throw new InvalidDateFormatException("Second year must be exactly one greater than the first.");
        }
    }

    private void validatePlanningDates(LocalDate Start, LocalDate End) {
        if (Start.isAfter(End)) {
            throw new RuntimeException("Start date cannot be after end date.");
        }

        if (Start.isEqual(End)) {
            throw new RuntimeException("Start date and end date cannot be the same.");
        }

        if (Start.isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past.");
        }
    }


    public String createPlanning(PlanningRequest Request) {
        String anneeuniversitaire = getAnneeUniversitaire();
        if (!planningRepository.findByAnneeuniversitaire(anneeuniversitaire).isEmpty()) {
            throw new RuntimeException("Planning Found");
        }
        validatePlanningDates(Request.dateDebut(), Request.dateFin());
        List<Salle> foundSalles = salleRepository.findAllById(Request.salleids());
        if (foundSalles.size() != Request.salleids().size()) {
            throw new SalleNotFoundException("One or more salle IDs are invalid.");
        }
        Planning planning = Planning.builder()
                .salles(foundSalles)
                .datedebut(Request.dateDebut())
                .datefin(Request.dateFin())
                .anneeuniversitaire(anneeuniversitaire)
                .build();
        planningRepository.save(planning);
        return "Planning created successfully";
    }

    public List<Planning> getAll() {
        return planningRepository.findAll();
    }

    public Planning GetPlanningById(PlanningIdRequest planningIdRequest) {
        if (planningRepository.findById(planningIdRequest.id()).isEmpty()) {
            throw new RuntimeException("Planning not found");
        }
        return planningRepository.findById(planningIdRequest.id()).get();
    }

    public Planning GetPlanningByAnneeUniversitaire(PlanningAnneeUniversitaireRequest planningAnneeUniversitaireRequest) {
        validateAnneeUniversitaireFormat(planningAnneeUniversitaireRequest.anneeuniversitaire());
        if (planningRepository.findByAnneeuniversitaire(planningAnneeUniversitaireRequest.anneeuniversitaire()).isEmpty()) {
            throw new RuntimeException("Planning not found");
        }
        return planningRepository.findByAnneeuniversitaire(planningAnneeUniversitaireRequest.anneeuniversitaire()).get();
    }

    public List<PFE> GetPlanningPfeById(PlanningIdRequest planningIdRequest) {
        Optional<Planning> planning = planningRepository.findById(planningIdRequest.id());
        return planning.map(pfeRepository::findByPlanningid).orElse(null);
    }

    public List<PFE> GetPlanningPfeByAnneeUniversitaire(PlanningAnneeUniversitaireRequest planningAnneeUniversitaireRequest) {
        return pfeRepository.findByPlanningid(GetPlanningByAnneeUniversitaire(planningAnneeUniversitaireRequest));
    }

    public String deletePlanning(PlanningIdRequest planningIdRequest) {
        Planning planning = GetPlanningById(planningIdRequest);
        List<PFE> pfes = pfeRepository.findByPlanningid(planning);
        System.out.println(pfes);
        if (!pfes.isEmpty()) {
            throw new RuntimeException("Planning can't be deleted: Existing Pfes related to this planning");
        } else {
//            if (pfeRepository.findFirstByPlanningidOrderByDateheureAsc(planning).getDateheure().isBefore(LocalDateTime.now())) {
//                throw new RuntimeException("Planning can't be deleted: cannot delete expired planning");
//            }
            pfes.forEach(pfe -> {
                Enseignant encadreur = pfe.getEncadreur();
                Enseignant rapporteur = pfe.getRapporteur();
                Enseignant president = pfe.getPresident();
                Salle salle = pfe.getSalle();

                encadreur.getDisponibilite().remove(pfe.getDateheure());
                rapporteur.getDisponibilite().remove(pfe.getDateheure());
                president.getDisponibilite().remove(pfe.getDateheure());
                salle.getDisponibilite().remove(pfe.getDateheure());

                enseignantRepository.save(encadreur);
                enseignantRepository.save(rapporteur);
                enseignantRepository.save(president);
                salleRepository.save(salle);
            });

            pfeRepository.deleteAll(pfes);
            planningRepository.delete(planning);

            return "Planning deleted successfully";

        }
    }

    /*public String deletePlanningByAnneeUniversitaire(PlanningAnneeUniversitaireRequest planningAnneeUniversitaireRequest){
        validateAnneeUniversitaireFormat(planningAnneeUniversitaireRequest.anneeuniversitaire());
        Planning planning = GetPlanningByAnneeUniversitaire(planningAnneeUniversitaireRequest);
        if(!pfeRepository.findByPlanningid(planning.getId()).isEmpty() && pfeRepository.findFirstByPlanningidOrderByDateheureAsc(planning.getId()).getDateheure().isBefore(LocalDateTime.now())){
            throw new PlanningException("Planning can't be deleted");
        }
        else{
            List<PFE> pfes= pfeRepository.findByPlanningid(planning.getId());
            pfeRepository.deleteAll(pfes);
            planningRepository.delete(planning);
            return "Planning deleted successfully";
        }
    }*/

    public Planning update(PlanningIdRequest planningIdRequest, PlanningStartEndDate planningStartEndDate, SallesRequest sallesRequest) {
        Planning planning = GetPlanningById(planningIdRequest);
        List<PFE> pfes = pfeRepository.findByPlanningid(planning);
        System.out.println(pfes);
        if (!pfes.isEmpty()) {
            throw new RuntimeException("Planning can't be modified: Existing Pfes related to this planning");
        }
        List<Salle> foundSalles = salleRepository.findAllById(sallesRequest.salleids());
        if (foundSalles.size() != sallesRequest.salleids().size() || foundSalles.isEmpty()) {
            throw new SalleNotFoundException("Salle can't be empty Or One or more salle IDs are invalid.");
        }
        planning.setSalles(foundSalles);
        validatePlanningDates(planningStartEndDate.start(), planningStartEndDate.end());
        planning.setDatedebut(planningStartEndDate.start());
        planning.setDatefin(planningStartEndDate.end());
        return planningRepository.save(planning);
    }

}
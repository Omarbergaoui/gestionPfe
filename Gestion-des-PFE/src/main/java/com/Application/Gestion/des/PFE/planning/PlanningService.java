package com.Application.Gestion.des.PFE.planning;


import com.Application.Gestion.des.PFE.disponibilte.DisponibiliteRequest;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateFormatException;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.pfe.PFE;
import com.Application.Gestion.des.PFE.pfe.PfeRepository;
import com.Application.Gestion.des.PFE.salle.DisponibilityReq;
import com.Application.Gestion.des.PFE.salle.Salle;
import com.Application.Gestion.des.PFE.salle.SalleNotFoundException;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlanningService {
    private final PlanningRepository planningRepository;
    private final PfeRepository pfeRepository;
    private final SalleRepository salleRepository;
    private final EnseignantRepository enseignantRepository;


    private String getAnneeUniversitaire() {
        LocalDate date=LocalDate.now();
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

    private void validatePlanningDates(LocalDate Start,LocalDate End) {
        if (Start.isAfter(End)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        if (Start.isEqual(End)) {
            throw new IllegalArgumentException("Start date and end date cannot be the same.");
        }

        if (Start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
    }


    public String createPlanning(PlanningRequest Request){
        String anneeuniversitaire = getAnneeUniversitaire();
        if(planningRepository.findByAnneeuniversitaire(anneeuniversitaire).isEmpty()){
            throw new PlanningFound("Planning Found");
        }
        validatePlanningDates(Request.dateDebut(),Request.dateFin());
        List<Salle> foundSalles = salleRepository.findAllById(Request.salleids());
        if (foundSalles.size() != Request.salleids().size()) {
            throw new SalleNotFoundException("One or more salle IDs are invalid.");
        }
        Planning planning= Planning.builder()
                .salles(foundSalles)
                .datedebut(Request.dateDebut())
                .datefin(Request.dateFin())
                .anneeuniversitaire(anneeuniversitaire)
                .build();
        planningRepository.save(planning);
        return "Planning created successfully";
    }

    public Planning GetPlanningById(PlanningIdRequest planningIdRequest){
        if(planningRepository.findById(planningIdRequest.id()).isEmpty()){
            throw new PlanningNotFound("Planning not found");
        }
        return planningRepository.findById(planningIdRequest.id()).get();
    }

    public Planning GetPlanningByAnneeUniversitaire(PlanningAnneeUniversitaireRequest planningAnneeUniversitaireRequest){
        validateAnneeUniversitaireFormat(planningAnneeUniversitaireRequest.anneeuniversitaire());
        if(planningRepository.findByAnneeuniversitaire(planningAnneeUniversitaireRequest.anneeuniversitaire()).isEmpty()){
            throw new PlanningNotFound("Planning not found");
        }
        return planningRepository.findByAnneeuniversitaire(planningAnneeUniversitaireRequest.anneeuniversitaire()).get();
    }

    public List<PFE> GetPlanningPfeById(PlanningIdRequest planningIdRequest){
        return pfeRepository.findByPlanningid(planningIdRequest.id());
    }

    public List<PFE> GetPlanningPfeByAnneeUniversitaire(PlanningAnneeUniversitaireRequest planningAnneeUniversitaireRequest){
        return pfeRepository.findByPlanningid(GetPlanningByAnneeUniversitaire(planningAnneeUniversitaireRequest).getId());
    }

    public String deletePlanning(PlanningIdRequest planningIdRequest){
        Planning planning=GetPlanningById(planningIdRequest);
        if(!pfeRepository.findByPlanningid(planning.getId()).isEmpty() && pfeRepository.findFirstByPlanningidOrderByDateheureAsc(planning.getId()).getDateheure().isBefore(LocalDateTime.now())){
            throw new PlanningException("Planning can't be deleted");
        }
        else{
            List<PFE> pfes = pfeRepository.findByPlanningid(planning.getId());

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

    public Planning AjouterSalles(SallesRequest sallesRequest,PlanningIdRequest planningIdRequest){
        Planning planning = GetPlanningById(planningIdRequest);
        if(!pfeRepository.findByPlanningid(planning.getId()).isEmpty() && pfeRepository.findFirstByPlanningidOrderByDateheureAsc(planning.getId()).getDateheure().isBefore(LocalDateTime.now())){
            throw new PlanningException("Planning can't be modified");
        }
        List<Salle> foundSalles = salleRepository.findAllById(sallesRequest.salleids());
        if (foundSalles.size() != sallesRequest.salleids().size()) {
            throw new SalleNotFoundException("One or more salle IDs are invalid.");
        }
        List<Salle> notInPlanning = foundSalles.stream()
                .filter(salle -> !planning.getSalles().contains(salle))
                .toList();
        planning.getSalles().addAll(notInPlanning);
        return planningRepository.save(planning);
    }

    public List<Salle> SallePourAffecter(PlanningIdRequest planningIdRequest){
        Planning planning=GetPlanningById(planningIdRequest);
        return salleRepository.findByIdNotIn(planning.getSalles().stream().map(Salle::getId)
                        .toList()
        );
    }

    public List<Salle> SalleDejaAffecte(PlanningIdRequest planningIdRequest){
        Planning planning=GetPlanningById(planningIdRequest);
        return salleRepository.findAllByIdIn(planning.getSalles().stream().map(Salle::getId)
                .toList());
    }

    public Planning modifierStartEndDate(PlanningIdRequest planningIdRequest,PlanningStartEndDate planningStartEndDate){
        Planning planning = GetPlanningById(planningIdRequest);
        if(!pfeRepository.findByPlanningid(planning.getId()).isEmpty()){
            throw new PlanningException("Planning can't be modified");
        }
        else{
            validatePlanningDates(planningStartEndDate.start(),planningStartEndDate.end());
            planning.setDatedebut(planningStartEndDate.start());
            planning.setDatefin(planningStartEndDate.end());
            return planningRepository.save(planning);
        }
    }

    public List<Salle> SalleParmiSallePlanningDisponible(PlanningIdRequest planningIdRequest,DisponibiliteRequest disponibiliteRequest) {
        Planning planning = GetPlanningById(planningIdRequest);
        return planning.getSalles()
                .stream()
                .filter(salle -> !salle.getDisponibilite().contains(disponibiliteRequest.date()))
                .toList();
    }


    public List<Salle> SalleParmiSallePlanningIndisponible(PlanningIdRequest planningIdRequest,DisponibiliteRequest disponibiliteRequest) {
        Planning planning = GetPlanningById(planningIdRequest);
        return planning.getSalles()
                .stream()
                .filter(salle -> salle.getDisponibilite().contains(disponibiliteRequest.date()))
                .toList();
    }



}

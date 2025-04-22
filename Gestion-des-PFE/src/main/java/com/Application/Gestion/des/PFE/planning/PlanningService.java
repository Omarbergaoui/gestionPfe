package com.Application.Gestion.des.PFE.planning;


import com.Application.Gestion.des.PFE.disponibilte.InvalidDateFormatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;

@Service
@AllArgsConstructor
public class PlanningService {
    private final PlanningRepository planningRepository;

    private String getAnneeUniversitaire(LocalDate date) {
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


    /*public Planning createPlanning(PlanningRequest Request){
        String anneeuniversitaire = getAnneeUniversitaire(LocalDate.now());
        if(planningRepository.findByAnneeuniversitaire(anneeuniversitaire).isEmpty()){
            throw new PlanningFound("Planning Found");
        }
    }*/

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





}

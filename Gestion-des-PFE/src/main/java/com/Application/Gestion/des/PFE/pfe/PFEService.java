package com.Application.Gestion.des.PFE.pfe;


import com.Application.Gestion.des.PFE.disponibilte.InvalidDateException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantNotFoundException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.planning.Planning;
import com.Application.Gestion.des.PFE.planning.PlanningFound;
import com.Application.Gestion.des.PFE.planning.PlanningNotFound;
import com.Application.Gestion.des.PFE.planning.PlanningRepository;
import com.Application.Gestion.des.PFE.salle.Salle;
import com.Application.Gestion.des.PFE.salle.SalleNotFoundException;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PFEService {
    private final PfeRepository pfeRepository;
    private final PlanningRepository planningRepository;
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
    public void validatePfeDateTime(LocalDateTime dateTime) {
        if (dateTime.getMinute() != 0) {
            throw new InvalidDateException("Minutes must be 00");
        }
        List<Integer> allowedHours = List.of(8, 9, 10, 11, 13, 14, 15, 16);
        if (!allowedHours.contains(dateTime.getHour())) {
            throw new InvalidDateException("Hour must be one of: 08:00, 09:00, 10:00, 11:00, 13:00, 14:00, 15:00, 16:00");
        }
        DayOfWeek day = dateTime.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new InvalidDateException("PFE cannot be scheduled on Saturday or Sunday");
        }
    }

    public PFE createPfe(PFERequest pfeRequest) {
        validatePfeDateTime(pfeRequest.dateTime());
        Optional<Planning> planningOpt = planningRepository.findById(pfeRequest.idPlanning());
        if (planningOpt.isEmpty()) {
            throw new PlanningNotFound("Planning not found");
        }
        Planning planning = planningOpt.get();

        if (!planning.getAnneeuniversitaire().equals(getAnneeUniversitaire())) {
            throw new PlanningNotFound("Planning not found for the current academic year");
        }

        if (pfeRequest.dateTime().toLocalDate().isAfter(planning.getDatefin()) ||
                pfeRequest.dateTime().toLocalDate().isBefore(planning.getDatedebut())) {
            throw new InvalidDateException("Invalid date for the PFE");
        }

        Enseignant encadrant = enseignantRepository.findById(pfeRequest.encadrant())
                .orElseThrow(() -> new EnseignantNotFoundException("Encadrant not found"));

        Enseignant president = enseignantRepository.findById(pfeRequest.president())
                .orElseThrow(() -> new EnseignantNotFoundException("President not found"));

        Enseignant rapporteur = enseignantRepository.findById(pfeRequest.rapporteur())
                .orElseThrow(() -> new EnseignantNotFoundException("Rapporteur not found"));

        LocalDateTime dateTime = pfeRequest.dateTime();
        if (encadrant.getDisponibilite().contains(dateTime)) {
            throw new InvalidDateException("Encadrant unavailable at that date");
        }
        if (president.getDisponibilite().contains(dateTime)) {
            throw new InvalidDateException("President unavailable at that date");
        }
        if (rapporteur.getDisponibilite().contains(dateTime)) {
            throw new InvalidDateException("Rapporteur unavailable at that date");
        }

        if (encadrant.equals(president) || encadrant.equals(rapporteur) || president.equals(rapporteur)) {
            throw new IllegalArgumentException("Encadrant, President, and Rapporteur must be different");
        }

        Salle salle = salleRepository.findById(pfeRequest.Salle())
                .orElseThrow(() -> new SalleNotFoundException("Salle not found"));

        if (salle.getDisponibilite().contains(dateTime)) {
            throw new InvalidDateException("Salle is unavailable at that date");
        }
        if (pfeRepository.findByPlanningidAndEtudiantemail(planning.getId(), pfeRequest.emailetudiant()).isPresent()) {
            throw new PfeFoundException("A PFE already exists for this student in the selected planning");
        }

        encadrant.getDisponibilite().add(dateTime);
        rapporteur.getDisponibilite().add(dateTime);
        president.getDisponibilite().add(dateTime);
        salle.getDisponibilite().add(dateTime);
        enseignantRepository.save(encadrant);
        enseignantRepository.save(rapporteur);
        enseignantRepository.save(president);
        salleRepository.save(salle);
        PFE pfe = new PFE();
        pfe.setPlanningid(planning);
        pfe.setDateheure(dateTime);
        pfe.setEncadreur(encadrant);
        pfe.setPresident(president);
        pfe.setRapporteur(rapporteur);
        pfe.setSalle(salle);
        pfe.setTitrerapport(pfeRequest.nomderapport());
        pfe.setEtudiantemail(pfe.getEtudiantemail());
        return pfeRepository.save(pfe);
    }

    public PFE getPfe(PfeRequestId pfeRequestId){
        if(pfeRepository.findById(pfeRequestId.Id()).isEmpty()){
            throw new PfeFoundException("Pfe not found");
        }
        return pfeRepository.findById(pfeRequestId.Id()).get();
    }

    public List<PFE> getPfepardate(LocalDateTime localDateTime){
        return pfeRepository.findByDateheure(localDateTime);
    }

    public List<PFE> getPfeparJour(LocalDate date) {
        return pfeRepository.findByDateheureBetween(date.atTime(8, 0), date.atTime(16, 0));
    }

    public String DeletePfe(PfeRequestId pfeRequestId){
        PFE pfe=getPfe(pfeRequestId);
        Enseignant encadreur =  pfe.getEncadreur();
        Enseignant president =  pfe.getPresident();
        Enseignant rapporteur =  pfe.getRapporteur();
        Salle salle = pfe.getSalle();
        encadreur.getDisponibilite().remove(pfe.getDateheure());
        president.getDisponibilite().remove(pfe.getDateheure());
        rapporteur.getDisponibilite().remove(pfe.getDateheure());
        salle.getDisponibilite().remove(pfe.getDateheure());
        enseignantRepository.save(encadreur);
        enseignantRepository.save(president);
        enseignantRepository.save(rapporteur);
        salleRepository.save(salle);
        return "Pfe Deleted succefully";
    }


    public void processExcelFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                StringBuilder rowContent = new StringBuilder("Row: ");
                for (int i = 0; i < 9; i++) {
                    Cell cell = row.getCell(i);
                    String value = cell != null ? cell.toString() : "";
                    rowContent.append(value);
                    if (i < 8) {
                        rowContent.append(" | ");
                    }
                }
                System.out.println(rowContent);
            }

            workbook.close();
        }
    }

}

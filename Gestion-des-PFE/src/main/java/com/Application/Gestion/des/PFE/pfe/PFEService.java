package com.Application.Gestion.des.PFE.pfe;
import com.Application.Gestion.des.PFE.algorithme.Algorithme;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateException;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateFormatException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantNotFoundException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.planning.Planning;
import com.Application.Gestion.des.PFE.planning.PlanningNotFound;
import com.Application.Gestion.des.PFE.planning.PlanningRepository;
import com.Application.Gestion.des.PFE.salle.Salle;
import com.Application.Gestion.des.PFE.salle.SalleNotFoundException;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.Application.Gestion.des.PFE.algorithme.Algorithme.generer;
import static com.Application.Gestion.des.PFE.algorithme.GeneticSchedulerRT.evoluer;

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
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private LocalDateTime parseAndValidateDate(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Invalid date format. Use yyyy/MM/dd HH:mm");
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
        if (day == DayOfWeek.SUNDAY) {
            throw new InvalidDateException("PFE cannot be scheduled on Sunday");
        }
    }

    public PFE createPfe(PFERequest pfeRequest) {
        LocalDateTime dateTime=parseAndValidateDate(pfeRequest.dateTime());
        validatePfeDateTime(dateTime);
        Optional<Planning> planningOpt = planningRepository.findByAnneeuniversitaire(getAnneeUniversitaire());
        if (planningOpt.isEmpty()) {
            throw new PlanningNotFound("Planning not found");
        }
        Planning planning = planningOpt.get();

        if (dateTime.toLocalDate().isAfter(planning.getDatefin()) ||
                dateTime.toLocalDate().isBefore(planning.getDatedebut())) {
            throw new InvalidDateException("Invalid date for the PFE");
        }

        Enseignant encadrant = enseignantRepository.findByEmail(pfeRequest.encadrant());
        if (encadrant == null) {
            throw new EnseignantNotFoundException("Encadrant not found");
        }

        Enseignant president = enseignantRepository.findByEmail(pfeRequest.president());
        if (president == null) {
            throw new EnseignantNotFoundException("President not found");
        }

        Enseignant rapporteur = enseignantRepository.findByEmail(pfeRequest.rapporteur());
        if (rapporteur == null) {
            throw new EnseignantNotFoundException("Rapporteur not found");
        }

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

//    public PFE updatePfe(PfeRequestId pfeRequestId, PFERequest pfeRequest) {
//        PFE pfe = getPfe(pfeRequestId);
//
//        Optional<Planning> planningOpt = planningRepository.findByAnneeuniversitaire(getAnneeUniversitaire());
//        if (planningOpt.isEmpty()) {
//            throw new PlanningNotFound("Planning not found");
//        }
//        Planning planning = planningOpt.get();
//
//        LocalDateTime dateTime = pfeRequest.dateTime();
//        if (dateTime != null) {
//            validatePfeDateTime(dateTime);
//
//            if (dateTime.toLocalDate().isAfter(planning.getDatefin()) || dateTime.toLocalDate().isBefore(planning.getDatedebut())) {
//                throw new InvalidDateException("Invalid date for the PFE");
//            }
//
//            Enseignant encadrant = enseignantRepository.findById(pfe.getEncadreur().getEmail())
//                    .orElseThrow(() -> new EnseignantNotFoundException("Encadrant not found"));
//
//            if (encadrant.getDisponibilite().contains(dateTime)) {
//                throw new InvalidDateException("Encadrant unavailable at that date");
//            }
//            if (pfe.getPresident() != null && pfe.getPresident().getDisponibilite().contains(dateTime)) {
//                throw new InvalidDateException("President unavailable at that date");
//            }
//            if (pfe.getRapporteur() != null && pfe.getRapporteur().getDisponibilite().contains(dateTime)) {
//                throw new InvalidDateException("Rapporteur unavailable at that date");
//            }
//            if (pfe.getSalle() != null && pfe.getSalle().getDisponibilite().contains(dateTime)) {
//                throw new InvalidDateException("Salle unavailable at that date");
//            }
//
//            pfe.setDateheure(dateTime);
//        }
//
//        if (pfeRequest.president() != null) {
//            Enseignant president = enseignantRepository.findById(pfeRequest.president())
//                    .orElseThrow(() -> new EnseignantNotFoundException("President not found"));
//
//            if (pfe.getEncadreur().equals(president) || (pfe.getRapporteur() != null && pfe.getRapporteur().equals(president))) {
//                throw new IllegalArgumentException("President must be different from Encadrant and Rapporteur");
//            }
//
//            pfe.setPresident(president);
//        }
//
//        if (pfeRequest.rapporteur() != null) {
//            Enseignant rapporteur = enseignantRepository.findById(pfeRequest.rapporteur())
//                    .orElseThrow(() -> new EnseignantNotFoundException("Rapporteur not found"));
//
//            if (pfe.getEncadreur().equals(rapporteur) || (pfe.getPresident() != null && pfe.getPresident().equals(rapporteur))) {
//                throw new IllegalArgumentException("Rapporteur must be different from Encadrant and President");
//            }
//
//            pfe.setRapporteur(rapporteur);
//        }
//
//        if (pfeRequest.Salle() != null) {
//            Salle salle = salleRepository.findById(pfeRequest.Salle())
//                    .orElseThrow(() -> new SalleNotFoundException("Salle not found"));
//
//            pfe.setSalle(salle);
//        }
//
//
//
//        if (pfeRequest.nomderapport() != null) {
//            pfe.setTitrerapport(pfeRequest.nomderapport());
//        }
//
//        return pfeRepository.save(pfe);
//    }


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
        return "Pfe Deleted successfully";
    }


    public void processExcelFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported.");
        }

        List<Algorithme.PFE> pfes = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                String studentEmail = getStringCellValue(row.getCell(0));
                String title = getStringCellValue(row.getCell(1));
                if(title==null){
                    continue;
                }
                String supervisor = getStringCellValue(row.getCell(2));
                if(enseignantRepository.findByEmail(supervisor)==null){
                    supervisor = null;
                }
                String president = getStringCellValue(row.getCell(3));
                if(enseignantRepository.findByEmail(president)==null){
                    president = null;
                }
                String reporter = getStringCellValue(row.getCell(4));
                if(enseignantRepository.findByEmail(reporter)==null){
                    reporter = null;
                }
                String room = getStringCellValue(row.getCell(7));
                LocalDate date=parseDate(getStringCellValue(row.getCell(5)));
                LocalTime time=parseTime(getStringCellValue(row.getCell(6)));
                LocalDateTime dateTime=combineDateAndTime(date,time);

                Algorithme.PFE pfe=new Algorithme.PFE(studentEmail,title,supervisor,reporter,president,room,dateTime);
                pfes.add(pfe);
            }
        }
        List<String> usernames = enseignantRepository.findAll()
                .stream()
                .map(Enseignant::getUsername)
                .collect(Collectors.toList());

        evoluer(generer(pfes,usernames));
    }

    public LocalTime parseTime(String timeString) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        try {
            return LocalTime.parse(timeString, timeFormat);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    public LocalDateTime combineDateAndTime(LocalDate date, LocalTime time) {
        if (date != null && time != null) {
            return date.atTime(time);
        }
        return null;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        } else {
            return cell.toString().trim();
        }
    }
    public LocalDate parseDate(String dateString) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            return LocalDate.parse(dateString, dateFormat);
        } catch (DateTimeParseException e) {
            return null;
        }
    }



}

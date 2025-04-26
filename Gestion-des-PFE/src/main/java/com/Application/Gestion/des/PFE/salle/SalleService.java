package com.Application.Gestion.des.PFE.salle;
import com.Application.Gestion.des.PFE.disponibilte.AvailableDateException;
import com.Application.Gestion.des.PFE.disponibilte.DisponibilityNotFoundException;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateException;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalleService {
    private final SalleRepository salleRepository;

    private boolean isValidSalleName(String name) {
        return name != null && (name.matches("^[A-Z]\\d{2}$") || name.matches("^Amphi\\s\\w+$"));
    }

    public Salle createSalle(SalleReq req) {
        if (!isValidSalleName(req.Name())) {
            throw new SalleException("The room name must start with a capital letter followed by two digits, or start with 'Amphi' followed by any word.");
        }
        if (salleRepository.findByNom(req.Name()).isPresent()) {
            throw new SalleNotFoundException("The room already exists.");
        }
        return salleRepository.save(Salle.builder()
                .nom(req.Name())
                .disponibilite(new ArrayList<>())
                .build());
    }


    public String deleteSalleByNom(String nom) {
        Salle salle = salleRepository.findByNom(nom)
                .orElseThrow(() -> new SalleNotFoundException("Room not found"));
        salleRepository.delete(salle);
        return "Room successfully deleted.";
    }


    public String deleteSalleById(SalleRequest request) {
        Salle salle = salleRepository.findById(request.id())
                .orElseThrow(() -> new SalleNotFoundException("Room not found"));
        salleRepository.delete(salle);
        return "Room successfully deleted.";
    }


    public Salle getSalleById(SalleRequest request) {
        return salleRepository.findById(request.id())
                .orElseThrow(() -> new SalleNotFoundException("Room not found with id: " + request.id()));
    }

    public Salle getSalleByNom(SalleReq req) {
        return salleRepository.findByNom(req.Name())
                .orElseThrow(() -> new SalleNotFoundException("Room not found with name: " + req.Name()));
    }


    public List<Salle> getAllSalle(){
        LocalDateTime now = LocalDateTime.now();
        List<Salle> salles = salleRepository.findAll();

        for (Salle salle : salles) {
            if (salle.getDisponibilite() != null) {
                List<LocalDateTime> disposFutures = salle.getDisponibilite().stream()
                        .filter(d -> d.isAfter(now))
                        .collect(Collectors.toList());
                salle.setDisponibilite(disposFutures);
            }
        }
        return salles;
    }


    public Salle updateSalleById(SalleRequest request, SalleReq req) {
        Salle salle = getSalleById(request);
        salle.setNom(req.Name());
        return salleRepository.save(salle);
    }


    public Salle updateSalleByNom(SalleReq requ, SalleReq req) {
        Salle salle = getSalleByNom(requ);
        salle.setNom(req.Name());
        return salleRepository.save(salle);
    }


    public Salle addDisponibility(SalleRequest req, LocalDateTime dateTime) {
        if (!isValidDateTime(dateTime)) {
            throw new InvalidDateException("Date is invalid or in the past.");
        }

        Salle salle = getSalleById(req);
        if (salle.getDisponibilite().contains(dateTime)) {
            throw new AvailableDateException("This date is already available.");
        }

        salle.getDisponibilite().add(dateTime);
        return salleRepository.save(salle);
    }

    public Salle removeDisponibility(SalleRequest req, LocalDateTime dateTimeToRemove) {
        if (!isValidDateTime(dateTimeToRemove)) {
            throw new InvalidDateException("Date is invalid or in the past.");
        }

        Salle salle = getSalleById(req);
        if (!salle.getDisponibilite().contains(dateTimeToRemove)) {
            throw new DisponibilityNotFoundException("This date is not available for removal.");
        }

        salle.getDisponibilite().remove(dateTimeToRemove);
        return salleRepository.save(salle);
    }


    private boolean isValidDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now)) return false;
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(18, 0));
    }

    public List<Salle> getSallesDisponibles(LocalDateTime date) {
        return salleRepository.findByDisponibiliteNotContaining(date);
    }

    public List<Salle> getSallesIndisponibles(LocalDateTime date) {
        return salleRepository.findByDisponibiliteContaining(date);
    }
}

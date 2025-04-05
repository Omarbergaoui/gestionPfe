package com.Application.Gestion.des.PFE.salle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom de la salle doit commencer par une lettre majuscule suivie de deux chiffres ou commencer par 'Amphi' suivi de n'importe quel mot.");
        }
        if (salleRepository.findByNom(req.Name()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La Salle Existe Déjà.");
        }
        return salleRepository.save(Salle.builder().nom(req.Name()).disponibilite(new ArrayList<>()).build());
    }

    public String deleteSalleByNom(String nom) {
        Salle salle = salleRepository.findByNom(nom)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle non trouvée"));
        salleRepository.delete(salle);
        return "Salle Supprimée Avec Succées";
    }

    public String deleteSalleById(SalleRequest request) {
        Salle salle = salleRepository.findById(request.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle non trouvée"));
        salleRepository.delete(salle);
        return "Salle Supprimée Avec Succées";
    }


    public Salle getSalleById(SalleRequest request) {
        return salleRepository.findById(request.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle non trouvée"));
    }


    public Salle getSalleByNom(SalleReq req) {
        return salleRepository.findByNom(req.Name())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle non trouvée"));
    }

    public List<Salle> getAllSalle(){
        return salleRepository.findAll();
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date invalide ou dans le passé.");
        }
        Salle salle = getSalleById(req);
        salle.getDisponibilite().add(dateTime);
        return salleRepository.save(salle);
    }

    public Salle removeDisponibility(SalleRequest req, LocalDateTime dateTimeToRemove) {
        if (!isValidDateTime(dateTimeToRemove)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date invalide ou dans le passé.");
        }
        Salle salle = getSalleById(req);
        salle.getDisponibilite().removeIf(date -> date.equals(dateTimeToRemove));
        return salleRepository.save(salle);
    }

    private boolean isValidDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now)) return false;
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(18, 0));
    }

    public List<Salle> getSallesDisponibles(LocalDateTime date) {
        return salleRepository.findAll().stream()
                .filter(salle -> !salle.getDisponibilite().contains(date))
                .collect(Collectors.toList());
    }

    public List<Salle> getSallesIndisponibles(LocalDateTime date) {
        return salleRepository.findAll().stream()
                .filter(salle -> salle.getDisponibilite().contains(date))
                .collect(Collectors.toList());
    }
}

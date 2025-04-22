package com.Application.Gestion.des.PFE.salle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/Salles")
@RequiredArgsConstructor
public class SalleController {

    private final SalleService salleService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private LocalDateTime parseAndValidateDate(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format de date invalide. Utilisez yyyy/MM/dd HH:mm");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Salle> createSalle(@RequestBody SalleReq req) {
        return ResponseEntity.ok(salleService.createSalle(req));
    }

    @DeleteMapping("/delete/nom")
    public ResponseEntity<String> deleteSalleByNom(@RequestBody SalleReq salleReq) {
        String response = salleService.deleteSalleByNom(salleReq.Name());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/id")
    public ResponseEntity<String> deleteSalleById(@RequestParam String id) {
        String response = salleService.deleteSalleById(new SalleRequest(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id")
    public ResponseEntity<Salle> getSalleById(@RequestParam String id) {
        Salle salle = salleService.getSalleById(new SalleRequest(id));
        return ResponseEntity.ok(salle);
    }

    @GetMapping("/nom")
    public ResponseEntity<Salle> getSalleByNom(@RequestBody SalleReq req) {
        Salle salle = salleService.getSalleByNom(req);
        return ResponseEntity.ok(salle);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Salle>> getAllSalle() {
        List<Salle> salles = salleService.getAllSalle();
        return ResponseEntity.ok(salles);
    }

    @PutMapping("/update/id")
    public ResponseEntity<Salle> updateSalleById(@RequestParam String id,@RequestBody SalleReq salleReq) {
        Salle updatedSalle = salleService.updateSalleById(new SalleRequest(id), salleReq);
        return ResponseEntity.ok(updatedSalle);
    }

    @PutMapping("/update/nom")
    public ResponseEntity<Salle> updateSalleByNom(@RequestBody SalleUpdateByName req) {
        Salle updatedSalle = salleService.updateSalleByNom(req.req(), req.request());
        return ResponseEntity.ok(updatedSalle);
    }

    @PostMapping("/disponibility/add")
    public ResponseEntity<Salle> addDisponibility(@RequestParam String id, @RequestBody DisponibilityReq req) {
        LocalDateTime dateTime = parseAndValidateDate(req.dateTime());
        Salle updatedSalle = salleService.addDisponibility(new SalleRequest(id), dateTime);
        return ResponseEntity.ok(updatedSalle);
    }

    @PostMapping("/disponibility/remove")
    public ResponseEntity<Salle> removeDisponibility(@RequestParam String id, @RequestBody DisponibilityReq req) {
        LocalDateTime dateTime = parseAndValidateDate(req.dateTime());
        Salle updatedSalle = salleService.removeDisponibility(new SalleRequest(id), dateTime);
        return ResponseEntity.ok(updatedSalle);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Salle>> getSallesDisponibles(@RequestBody DisponibilityReq disponibilityReq) {
        LocalDateTime date = parseAndValidateDate(disponibilityReq.dateTime());
        List<Salle> sallesDisponibles = salleService.getSallesDisponibles(date);
        return ResponseEntity.ok(sallesDisponibles);
    }

    @GetMapping("/indisponibles")
    public ResponseEntity<List<Salle>> getSallesIndisponibles(@RequestBody DisponibilityReq disponibilityReq) {
        LocalDateTime date = parseAndValidateDate(disponibilityReq.dateTime());
        List<Salle> sallesIndisponibles = salleService.getSallesIndisponibles(date);
        return ResponseEntity.ok(sallesIndisponibles);
    }


}

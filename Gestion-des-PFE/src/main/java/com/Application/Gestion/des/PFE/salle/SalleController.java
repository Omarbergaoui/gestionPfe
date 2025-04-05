package com.Application.Gestion.des.PFE.salle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public Salle createSalle(@RequestBody SalleReq req) {
        return salleService.createSalle(req);
    }

    @DeleteMapping("/delete/nom/{nom}")
    public String deleteSalleByNom(@RequestBody SalleReq req) {
        return salleService.deleteSalleByNom(req.Name());
    }

    @DeleteMapping("/delete/id")
    public String deleteSalleById(@RequestBody SalleRequest request) {
        return salleService.deleteSalleById(request);
    }

    @GetMapping("/id")
    public Salle getSalleById(@RequestBody SalleRequest request) {
        return salleService.getSalleById(new SalleRequest(request.id()));
    }

    @GetMapping("/nom")
    public Salle getSalleByNom(@RequestBody SalleReq req) {
        return salleService.getSalleByNom(req);
    }

    @GetMapping("/all")
    public List<Salle> getAllSalle() {
        return salleService.getAllSalle();
    }

    @PutMapping("/update/id")
    public Salle updateSalleById(@RequestBody SalleUpdateById salleUpdateById) {
        return salleService.updateSalleById(salleUpdateById.request(),salleUpdateById.req());
    }

    @PutMapping("/update/nom")
    public Salle updateSalleByNom(@RequestBody SalleUpdateByName req) {
        return salleService.updateSalleByNom(req.req(),req.request());
    }

    @PostMapping("/{id}/disponibility/add")
    public Salle addDisponibility(@PathVariable String id, @RequestBody DisponibilityReq req) {
        LocalDateTime dateTime = parseAndValidateDate(req.dateTime());
        return salleService.addDisponibility(new SalleRequest(id), dateTime);
    }

    @PostMapping("/{id}/disponibility/remove")
    public Salle removeDisponibility(@PathVariable String id, @RequestBody DisponibilityReq req) {
        LocalDateTime dateTime = parseAndValidateDate(req.dateTime());
        return salleService.removeDisponibility(new SalleRequest(id), dateTime);
    }

    @GetMapping("/disponibles")
    public List<Salle> getSallesDisponibles(@RequestParam String dateTime) {
        LocalDateTime date = parseAndValidateDate(dateTime);
        return salleService.getSallesDisponibles(date);
    }

    @GetMapping("/indisponibles")
    public List<Salle> getSallesIndisponibles(@RequestParam String dateTime) {
        LocalDateTime date = parseAndValidateDate(dateTime);
        return salleService.getSallesIndisponibles(date);
    }

}

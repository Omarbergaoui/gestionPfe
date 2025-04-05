package com.Application.Gestion.des.PFE.departement;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Departements")
@RequiredArgsConstructor
public class DepartementController {

    @Autowired
    private final DepartementService departementService;

    @PostMapping
    public ResponseEntity<Departement> createDepartement(@RequestBody DepartementReq departement) {
        return ResponseEntity.ok(departementService.createDepartement(departement));
    }

    @GetMapping("/id")
    public ResponseEntity<Departement> getDepartementById(@RequestBody DepartementRequest departementRequest) {
        return ResponseEntity.ok(departementService.getDepartementById(departementRequest));
    }

    @GetMapping
    public ResponseEntity<List<Departement>> getAllDepartements() {
        return ResponseEntity.ok(departementService.getAllDepartements());
    }

    @GetMapping("/name")
    public ResponseEntity<Departement> getDepartementByName(@RequestBody DepartementReq departementReq) {
        return ResponseEntity.ok(departementService.getDepartementByNom(departementReq));
    }

    @PutMapping("/id")
    public ResponseEntity<Departement> updateDepartement(@RequestBody DepartmentUpdateRequest departmentUpdateRequest) {
        return ResponseEntity.ok(departementService.updateDepartementById(departmentUpdateRequest.departementRequest(),departmentUpdateRequest.departementReq()));
    }

    /*@DeleteMapping("/id")
    public ResponseEntity<String> deleteDepartement(@RequestBody DepartementRequest departementReq) {
        departementService.deleteDepartement(departementReq.id());
        return ResponseEntity.ok("Département supprimé avec succès.");
    }

    @GetMapping("/chefs")
    public ResponseEntity<List<ChefDepartement>> getAllChefDepartements() {
        return ResponseEntity.ok(departementService.getAllChefDepartement());
    }

    @DeleteMapping("/chef/{id}")
    public ResponseEntity<String> deleteChefDepartement(@PathVariable String id) {
        return ResponseEntity.ok(departementService.deleteChefDepartement(id));
    }

    @PostMapping("/affecter-chef/{idEnseignant}/{idDepartement}")
    public ResponseEntity<String> affecterChefDepartement(@PathVariable String idEnseignant, @PathVariable String idDepartement) {
        return ResponseEntity.ok(departementService.affecterChefDepartement(idEnseignant, idDepartement));
    }

    @PostMapping("/affecter-enseignant/{idEnseignant}/{idDepartement}")
    public ResponseEntity<String> affecterEnseigantDepartement(@PathVariable String idEnseignant, @PathVariable String idDepartement) {
        return ResponseEntity.ok(departementService.affecterEnsignantDepartement(idEnseignant, idDepartement));
    }

    @GetMapping("/sans-chef")
    public ResponseEntity<List<Departement>> getDepartementsSansChef() {
        return ResponseEntity.ok(departementService.getDepartementssanschef());
    }

    @GetMapping("/avec-chef")
    public ResponseEntity<List<Departement>> getDepartementsAvecChef() {
        return ResponseEntity.ok(departementService.getDepartementsavecchef());
    }*/
}

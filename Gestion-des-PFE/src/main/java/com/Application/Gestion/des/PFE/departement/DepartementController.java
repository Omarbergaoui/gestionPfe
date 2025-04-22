package com.Application.Gestion.des.PFE.departement;

import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
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
    public ResponseEntity<Departement> getDepartementById(@RequestParam String id) {
        return ResponseEntity.ok(departementService.getDepartementById(new DepartementRequest(id)));
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
    public ResponseEntity<Departement> updateDepartement(@RequestBody DepartementName departementName,@RequestParam String id) {
        return ResponseEntity.ok(departementService.updateDepartementById(new DepartementRequest(id),departementName));
    }

    // 9bal matfasakh l departement lazem l enseignants ili fi westou yemchiw l departement ekher
    @DeleteMapping("/id")
    public ResponseEntity<String> deleteDepartement(@RequestParam String idDeparttodelete,@RequestParam String idDeparttoreassign) {
        return ResponseEntity.ok(departementService.deleteDepartement(new DepartementRequest(idDeparttodelete),new DepartementRequest(idDeparttoreassign)));
    }

    @GetMapping("/chefs")
    public ResponseEntity<List<EnseignantDto>> getAllChefDepartements() {
        return ResponseEntity.ok(departementService.getAllChefs());
    }

    @PutMapping("/Update/Chef")
    public ResponseEntity<Departement> changerChefs(@RequestParam String idDepartement,@RequestParam String idChef){
        return ResponseEntity.ok(departementService.updateDepartementChefById(new DepartementRequest(idDepartement),new RequestIdChef()));
    }

    @GetMapping("/enseignants")
    public ResponseEntity<List<EnseignantDto>> getEnseignants(@RequestParam String id){
        return ResponseEntity.ok(departementService.getAllEnseignants(new DepartementRequest(id)));
    }

    @GetMapping("/ChefDepartement")
    public ResponseEntity<EnseignantDto> getChefDepartement(@RequestParam String id){
        return ResponseEntity.ok(departementService.getChefDepartementById(new DepartementRequest(id)));
    }
}

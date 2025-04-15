package com.Application.Gestion.des.PFE.enseignant;
import com.Application.Gestion.des.PFE.departement.DepartementRequest;
import com.Application.Gestion.des.PFE.salle.DisponibilityReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Enseignants")
@RequiredArgsConstructor
public class EnseignantController {
    private final EnseignantService enseignantService;

    @PostMapping("/create")
    public ResponseEntity<Enseignant> createEnseignant(@RequestBody EnseignantRequest request) {
        return ResponseEntity.ok(enseignantService.createEnseignant(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Enseignant>> getAll() {
        return ResponseEntity.ok(enseignantService.getAllEnseignants());
    }

    @GetMapping("/get-by-email")
    public ResponseEntity<Enseignant> getByEmail(@RequestBody EnseignantRequestName request) {
        return ResponseEntity.ok(enseignantService.getEnseigantByEmail(request));
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<Enseignant> getById(@RequestBody EnseignantRequestId request) {
        return ResponseEntity.ok(enseignantService.getEnseigantById(request));
    }

    @PutMapping("/update/by-id")
    public ResponseEntity<Enseignant> updateById(
            @RequestBody EnseignantRequestUpdate update,
            @RequestParam String id) {
        return ResponseEntity.ok(enseignantService.UpdateEnseignantById(new EnseignantRequestId(id), update));
    }

    @PutMapping("/update/by-email")
    public ResponseEntity<Enseignant> updateByEmail(
            @RequestBody EnseignantRequestUpdate update,
            @RequestParam String email) {
        return ResponseEntity.ok(enseignantService.UpdateEnseignantByName(new EnseignantRequestName(email), update));
    }

    @DeleteMapping("/delete/by-id")
    public ResponseEntity<String> deleteById(@RequestParam String id) {
        return ResponseEntity.ok(enseignantService.DeleteEnseignantById(new EnseignantRequestId(id)));
    }

    @DeleteMapping("/delete/by-email")
    public ResponseEntity<String> deleteByEmail(@RequestParam String email) {
        return ResponseEntity.ok(enseignantService.DeleteEnseignantByName(new EnseignantRequestName(email)));
    }

    @PutMapping("/remove-department")
    public ResponseEntity<Enseignant> removeFromDepartment(@RequestParam String id) {
        return ResponseEntity.ok(enseignantService.RemoveEnseignantfromDepartment(new EnseignantRequestId(id)));
    }

    @PutMapping("/assign-department")
    public ResponseEntity<Enseignant> assignToDepartment(
            @RequestParam String id,
            @RequestBody DepartementRequest request) {
        return ResponseEntity.ok(enseignantService.AffecterEnseignantDepartement(new EnseignantRequestId(id), request));
    }

    @PutMapping("/disponibilite/add")
    public ResponseEntity<Enseignant> addDisponibilite(
            @RequestParam String id,
            @RequestBody DisponibilityReq request) {
        return ResponseEntity.ok(enseignantService.ajouterdisponibilite(request, new EnseignantRequestId(id)));
    }

    @PutMapping("/disponibilite/delete")
    public ResponseEntity<Enseignant> removeDisponibilite(
            @RequestParam String id,
            @RequestBody DisponibilityReq request) {
        return ResponseEntity.ok(enseignantService.deletedisponibilite(request, new EnseignantRequestId(id)));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Enseignant>> getDisponibles(@RequestBody DisponibilityReq request) {
        return ResponseEntity.ok(enseignantService.getEnseignantDisponible(request));
    }

    @GetMapping("/indisponibles")
    public ResponseEntity<List<Enseignant>> getIndisponibles(@RequestBody DisponibilityReq request) {
        return ResponseEntity.ok(enseignantService.getEnseignantIndisponible(request));
    }
}


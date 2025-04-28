package com.Application.Gestion.des.PFE.enseignant;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
import com.Application.Gestion.des.PFE.departement.DepartementRequest;
import com.Application.Gestion.des.PFE.salle.DisponibilityReq;
import com.Application.Gestion.des.PFE.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Enseignants")
@RequiredArgsConstructor
public class EnseignantController {
    private final EnseignantService enseignantService;

    @PostMapping("/create")
    public ResponseEntity<EnseignantDto> createEnseignant(@RequestBody EnseignantRequest request) {
        return ResponseEntity.ok(enseignantService.createEnseignant(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EnseignantDto>> getAll() {
        return ResponseEntity.ok(enseignantService.getAllEnseignants());
    }

    @GetMapping("/get-by-email")
    public ResponseEntity<EnseignantDto> getByEmail(@RequestBody EnseignantRequestName request) {
        return ResponseEntity.ok(enseignantService.getEnseignantByEmail(request));
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<EnseignantDto> getById(@RequestParam String id) {
        return ResponseEntity.ok(enseignantService.getEnseignantById(new EnseignantRequestId(id)));
    }
    @GetMapping("/id")
    public ResponseEntity<EnseignantDto> getByIdWithoutPfe(@RequestParam String id) {
        return ResponseEntity.ok(enseignantService.getEnseignantByIdWithoutPfe(new EnseignantRequestId(id)));
    }

    @PutMapping("/update")
    public ResponseEntity<Object> updateById(
            @RequestBody EnseignantRequestUpdate update,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(enseignantService.UpdateEnseignantById(user, update));
    }

    @PutMapping("/update-by-admin")
    public ResponseEntity<EnseignantDto> updateAdmin(
            @RequestBody UpdateEnsAdmin update,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(enseignantService.UpdateEnseignantAdmin(user, update));
    }

    @DeleteMapping("/delete/by-id")
    public ResponseEntity<String> deleteById(@RequestParam String id) {
        return ResponseEntity.ok(enseignantService.DeleteEnseignantById(new EnseignantRequestId(id)));
    }

    @DeleteMapping("/delete/by-email")
    public ResponseEntity<String> deleteByEmail(@RequestParam String email) {
        return ResponseEntity.ok(enseignantService.DeleteEnseignantByName(new EnseignantRequestName(email)));
    }


    @PutMapping("/assign-department")
    public ResponseEntity<EnseignantDto> assignToDepartment(
            @RequestParam String id,
            @RequestBody DepartementRequest request) {
        return ResponseEntity.ok(enseignantService.UpdateEnseignantDepartement(new EnseignantRequestId(id), request));
    }

    @PutMapping("/disponibilite/add")
    public ResponseEntity<EnseignantDto> addDisponibilite(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody Disponibility request) {
        return ResponseEntity.ok(enseignantService.ajouterdisponibilite(request, user));
    }

    @PutMapping("/disponibilite/delete")
    public ResponseEntity<EnseignantDto> removeDisponibilite(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody Disponibility request) {
        return ResponseEntity.ok(enseignantService.deletedisponibilite(request, user));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<EnseignantDto>> getDisponibles(@RequestBody DisponibilityReq request) {
        return ResponseEntity.ok(enseignantService.getEnseignantDisponible(request));
    }

    @GetMapping("/indisponibles")
    public ResponseEntity<List<EnseignantDto>> getIndisponibles(@RequestBody DisponibilityReq request) {
        return ResponseEntity.ok(enseignantService.getEnseignantIndisponible(request));
    }
}


package com.Application.Gestion.des.PFE.chefdepartement;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ChefDepartement")
@AllArgsConstructor
public class ChefDepartementController {

    private final ChefDepartementService chefDepartementService;
    @GetMapping
    public ResponseEntity<List<ChefDepartement>> getAllChefs() {
        return ResponseEntity.ok(chefDepartementService.getAllChefs());
    }

    @GetMapping("/by-id")
    public ResponseEntity<ChefDepartement> getChefById(@RequestBody RequestIdChef requestIdChef) {
        return ResponseEntity.ok(chefDepartementService.getChefDepartementById(requestIdChef));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ChefDepartement> getChefByEmail(@RequestBody RequestNameChef requestNameChef) {
        return ResponseEntity.ok(chefDepartementService.getChefDepartementByEmail(requestNameChef));
    }

    @PutMapping("/update/by-id")
    public ResponseEntity<ChefDepartement> updateChefById(
            @RequestBody ChefDepartementRequestUpdate request,
            @RequestParam String id
    ) {
        RequestIdChef requestIdChef = new RequestIdChef(id);
        return ResponseEntity.ok(chefDepartementService.UpdateChefDepartementById(requestIdChef, request));
    }

    @PutMapping("/update/by-email")
    public ResponseEntity<ChefDepartement> updateChefByEmail(
            @RequestBody ChefDepartementRequestUpdate request,
            @RequestParam String email
    ) {
        RequestNameChef requestNameChef = new RequestNameChef(email);
        return ResponseEntity.ok(chefDepartementService.UpdateChefDepartementByEmail(requestNameChef, request));
    }

    @DeleteMapping("/delete/by-id")
    public ResponseEntity<String> deleteChefById(@RequestParam String id) {
        RequestIdChef requestIdChef = new RequestIdChef(id);
        return ResponseEntity.ok(chefDepartementService.DeleteChefDepartementById(requestIdChef));
    }

    @DeleteMapping("/delete/by-email")
    public ResponseEntity<String> deleteChefByEmail(@RequestParam String email) {
        RequestNameChef requestNameChef = new RequestNameChef(email);
        return ResponseEntity.ok(chefDepartementService.DeleteChefDepartementByEmail(requestNameChef));
    }
}

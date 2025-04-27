package com.Application.Gestion.des.PFE.planning;


import com.Application.Gestion.des.PFE.pfe.PFE;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Planning")
@AllArgsConstructor
public class PlanningController {
    private final PlanningService planningService;


    @PostMapping("/create")
    public ResponseEntity<String> createPlanning(@RequestBody PlanningRequest request) {
        return ResponseEntity.ok(planningService.createPlanning(request));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Planning> getPlanningById(@PathVariable("id") String id) {
        Planning planning = planningService.GetPlanningById(new PlanningIdRequest(id));
        return ResponseEntity.ok(planning);
    }

    @GetMapping("/getByAnneeUniversitaire")
    public ResponseEntity<Planning> getPlanningByAnneeUniversitaire(@RequestBody PlanningAnneeUniversitaireRequest request) {
        Planning planning = planningService.GetPlanningByAnneeUniversitaire(request);
        return ResponseEntity.ok(planning);
    }

//    @GetMapping("/getPfeById/{id}")
//    public ResponseEntity<PFE> getPlanningPfeById(@PathVariable("id") Long id) {
//        List<PFE> pfes = planningService.GetPlanningPfeById(new PlanningIdRequest(id));
//        return ResponseEntity.ok(pfes);
//    }
//
//    @GetMapping("/getPfeByAnneeUniversitaire")
//    public ResponseEntity<List<PFE>> getPlanningPfeByAnneeUniversitaire(@RequestBody PlanningAnneeUniversitaireRequest request) {
//        List<PFE> pfes = planningService.GetPlanningPfeByAnneeUniversitaire(request);
//        return ResponseEntity.ok(pfes);
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<String> deletePlanning(@PathVariable("id") Long id) {
//        try {
//            String response = pfeService.deletePlanning(new PlanningIdRequest(id));
//            return ResponseEntity.ok(response);
//        } catch (PlanningException e) {
//            return ResponseEntity.status(400).body(e.getMessage());  // 400 Bad Request
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("An error occurred while deleting the planning");  // 500 Internal Server Error
//        }
//    }
    }



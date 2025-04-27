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


    @GetMapping("/getPfesByAnneeUniversitaire")
    public ResponseEntity<List<PFE>> getPlanningPfeByAnneeUniversitaire(@RequestBody PlanningAnneeUniversitaireRequest request) {
        List<PFE> pfes = planningService.GetPlanningPfeByAnneeUniversitaire(request);
        return ResponseEntity.ok(pfes);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePlanning(@PathVariable("id") String id) {
        String response = planningService.deletePlanning(new PlanningIdRequest(id));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Planning> UpdatePlanning(@PathVariable("id") String id,@RequestBody PlanningRequest planningRequest) {
        return ResponseEntity.ok(planningService.update(new PlanningIdRequest(id),new PlanningStartEndDate(planningRequest.dateDebut(),planningRequest.dateFin()),new SallesRequest(planningRequest.salleids())));
    }
    @GetMapping("/all")
    public ResponseEntity<List<Planning>> getAll(){
        return ResponseEntity.ok(planningService.getAll());
    }
}



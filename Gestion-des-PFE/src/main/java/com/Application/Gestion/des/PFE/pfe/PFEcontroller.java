package com.Application.Gestion.des.PFE.pfe;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/PFE")
@AllArgsConstructor
public class PFEcontroller {
    private final PFEService pfeService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
            pfeService.processExcelFile(file);
            return ResponseEntity.ok("Excel file processed successfully.");
    }

    @PostMapping("/create")
    public ResponseEntity<PFE> createPfe(@RequestBody PFERequest pfeRequest) {
        return ResponseEntity.ok(pfeService.createPfe(pfeRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PFE> getPfe(@PathVariable("id") PfeRequestId pfeRequestId) {
        return ResponseEntity.ok(pfeService.getPfe(pfeRequestId));
    }

//    @GetMapping("/bydate/{date}")
//    public ResponseEntity<List<PFE>> getPfepardate(@PathVariable("date") String localDateTime) {
//        try {
//            LocalDateTime date = LocalDateTime.parse(localDateTime);
//            List<PFE> pfes = pfeService.getPfepardate(date);
//            return ResponseEntity.ok(pfes);
//        } catch (Exception e) {
//            return ResponseEntity.status(400).body(null); // Format de date incorrect
//        }
//    }
//
//    @GetMapping("/byjour/{date}")
//    public ResponseEntity<List<PFE>> getPfeparJour(@PathVariable("date") String date) {
//        try {
//            LocalDate localDate = LocalDate.parse(date);
//            List<PFE> pfes = pfeService.getPfeparJour(localDate);
//            return ResponseEntity.ok(pfes);
//        } catch (Exception e) {
//            return ResponseEntity.status(400).body(null); // Format de date incorrect
//        }
//    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePfe(@PathVariable("id") PfeRequestId pfeRequestId) {
        return ResponseEntity.ok(pfeService.DeletePfe(pfeRequestId));
    }


}

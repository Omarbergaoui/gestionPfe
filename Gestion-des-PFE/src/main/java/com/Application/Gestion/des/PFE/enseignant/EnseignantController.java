package com.Application.Gestion.des.PFE.enseignant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Enseignants")
@RequiredArgsConstructor
public class EnseignantController {
    private final EnseignantService enseignantService;



}


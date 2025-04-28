package com.Application.Gestion.des.PFE.databaseseed;

import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.departement.DepartementRepository;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.enumeration.Role;
import com.Application.Gestion.des.PFE.salle.Salle;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
public class DatabaseSeeder {

    private final SalleRepository salleRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnseignantRepository enseignantRepository;



    @PostConstruct
    public void seed() {
        if (salleRepository.count() == 0) { // avoid reseeding
            List<Salle> salles = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < 40; i++) {

                char letter = (char) ('A' + random.nextInt(26)); // A to Z
                int number = random.nextInt(100); // 00 to 99

                String nom = String.format("%c%02d", letter, number); // ex: A09, Z87

                List<LocalDateTime> disponibilites = new ArrayList<>();
                for (int j = 0; j < 5; j++) { // 5 available times per salle

                    int day = random.nextInt(30) + 1; // Day between 1 and 30

                    int hour = random.nextInt(9) + 8;

                    // Generate the date in June 2025
                    LocalDateTime date = LocalDateTime.of(2025, 6, day, hour, 0);

                    disponibilites.add(date);
                }

                Salle salle = Salle.builder()
                        .nom(nom)
                        .disponibilite(disponibilites)
                        .build();
                salles.add(salle);
            }

            salleRepository.saveAll(salles);
            System.out.println("✅ 40 salles inserted with 5 random dates in June 2025!");
        }
        if (departementRepository.count() == 0) { // avoid reseeding departments
            List<Departement> departements = new ArrayList<>();

            // Predefined department names
            String[] departmentNames = {
                    "Électronique", "Mécanique", "Informatique", "Génie Civil", "Télécommunications",
                    "Chimie", "Biologie", "Mathématiques", "Physique", "Architecture"
            };

            for (String departmentName : departmentNames) {
                // Create the department with chefdepartement as null for now
                Departement departement = Departement.builder()
                        .nom(departmentName)
                        .chefdepartement(null)
                        .build(); // chefdepartement is null initially
                departements.add(departement);
            }

            departementRepository.saveAll(departements);
            System.out.println("✅ 10 predefined departments inserted with chefdepartement as null!");
        }
        if (enseignantRepository.count() == 0) {
            List<Enseignant> enseignants = new ArrayList<>();
            Random random = new Random();

            // Predefined list of subjects (matieres)
            String[] matieres = {"Mathématiques", "Informatique", "Physique", "Chimie", "Biologie", "Génie Civil"};

            // Predefined department IDs (assuming you already have these departments created)
            List<Departement> departements = departementRepository.findAll();

            // Create 50 enseignants
            for (int i = 0; i < 50; i++) {
                String firstname = "Firstname" + (i + 1);
                String lastname = "Lastname" + (i + 1);
                String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@university.com";
                String password = passwordEncoder.encode("password"); // Encode the password

                // Randomly assign a matiere
                String matiere = matieres[random.nextInt(matieres.length)];

                // Randomly assign a department from the list
                Departement departement = departements.get(random.nextInt(departements.size()));

                // Generate 20 random indisponibilites
                List<LocalDateTime> indisponibilites = new ArrayList<>();
                for (int j = 0; j < 20; j++) {
                    // Generate random date in the future (for June 2025, for example)
                    int day = random.nextInt(30) + 1;  // Day between 1 and 30
                    int hour = random.nextInt(9) + 8; // Hour between 8 and 18
                    LocalDateTime date = LocalDateTime.of(2025, 6, day, hour, 0); // June 2025
                    indisponibilites.add(date);
                }

                Enseignant enseignant = Enseignant.builder()
                        .firstname(firstname)
                        .lastname(lastname)
                        .email(email)
                        .role(Role.ENSEIGNANT)
                        .password(password)
                        .matiere(matiere)
                        .departementId(departement)
                        .disponibilite(indisponibilites)
                        .enable(true)
                        .accountLocked(false)
                        .build();
                enseignants.add(enseignant);
            }
            enseignantRepository.saveAll(enseignants);
            System.out.println("✅ 50 enseignants created successfully with 20 indisponibilités each!");
        }
        Random random = new Random();
        for(Departement departement: departementRepository.findAll()){
            List<Enseignant> enseignant = enseignantRepository.findByDepartementId(departement);
            if(!enseignant.isEmpty()){
                Enseignant chef = enseignant.get(random.nextInt(enseignant.size()));
                chef.setRole(Role.CHEFDEPARTEMENT);
                enseignantRepository.save(chef);
                departement.setChefdepartement(chef);
                departementRepository.save(departement);
            }
        }
    }
}


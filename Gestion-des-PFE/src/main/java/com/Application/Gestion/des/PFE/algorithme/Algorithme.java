package com.Application.Gestion.des.PFE.algorithme;


import com.Application.Gestion.des.PFE.departement.DepartementRepository;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.salle.SalleRepository;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@AllArgsConstructor
public class Algorithme {

    private final SalleRepository salleRepository;
    private final EnseignantRepository enseignantRepository;
    private final DepartementRepository departementRepository;

    public static List<PFE> assignRandomEncadrants(List<PFE> pfes, List<String> teachers, int maxPerTeacher) {
        Map<String, Integer> counts = new HashMap<>();
        for (String teacher : teachers) {
            counts.put(teacher, 0);
        }
        for (PFE pfe : pfes) {
            if (pfe.getEncadrant() != null) {
                counts.put(pfe.getEncadrant(), counts.get(pfe.getEncadrant()) + 1);
            }
        }

        List<PFE> toFill = new ArrayList<>();
        for (PFE pfe : pfes) {
            if (pfe.getEncadrant() == null) {
                toFill.add(pfe);
            }
        }

        Random random = new Random();

        for (PFE pfe : toFill) {
            Set<String> excludedRoles = new HashSet<>();
            if (pfe.getRapporteur() != null) excludedRoles.add(pfe.getRapporteur());
            if (pfe.getPresident() != null)  excludedRoles.add(pfe.getPresident());

            List<String> available = new ArrayList<>();
            for (String teacher : teachers) {
                if (counts.get(teacher) < maxPerTeacher && !excludedRoles.contains(teacher)) {
                    available.add(teacher);
                }
            }

            if (available.isEmpty()) {
                throw new RuntimeException(
                        "Plus aucun enseignant disponible pour l'encadrement du PFE \"" + pfe.getRapport() + "\""
                );
            }

            String pick = available.get(random.nextInt(available.size()));
            pfe.setEncadrant(pick);
            counts.put(pick, counts.get(pick) + 1);
        }

        return pfes;
    }
    public static List<Map<String, Object>> computeTeacherStats(List<PFE> pfes) {
        Map<String, Map<String, Object>> statsMap = new HashMap<>();

        for (PFE pfe : pfes) {
            for (String role : Arrays.asList("encadrant", "rapporteur", "president")) {
                String name = null;
                switch (role) {
                    case "encadrant":
                        name = pfe.getEncadrant();
                        break;
                    case "rapporteur":
                        name = pfe.getRapporteur();
                        break;
                    case "president":
                        name = pfe.getPresident();
                        break;
                }

                if (name == null) continue;

                statsMap.putIfAbsent(name, new HashMap<>());
                Map<String, Object> stats = statsMap.get(name);

                stats.putIfAbsent("nom", name);
                stats.putIfAbsent("encadrant", 0);
                stats.putIfAbsent("rapporteur", 0);
                stats.putIfAbsent("president", 0);

                stats.put(role, (Integer) stats.get(role) + 1);
            }
        }

        return new ArrayList<>(statsMap.values());
    }
    public static List<Map<String, Object>> computeEncadAllRolesDiff(List<Map<String, Object>> stats) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> entry : stats) {
            String nom = (String) entry.get("nom");
            int encadrant = (Integer) entry.getOrDefault("encadrant", 0);
            int rapporteur = (Integer) entry.getOrDefault("rapporteur", 0);

            Map<String, Object> map = new HashMap<>();
            map.put("nom", nom);
            map.put("diff", Math.abs(encadrant - rapporteur));

            result.add(map);
        }

        return result;
    }

    public static List<Map<String, Object>> computeEncadAllRoleDiff(List<Map<String, Object>> stats) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> entry : stats) {
            String nom = (String) entry.get("nom");
            int encadrant = (Integer) entry.getOrDefault("encadrant", 0);
            int president = (Integer) entry.getOrDefault("president", 0);

            Map<String, Object> map = new HashMap<>();
            map.put("nom", nom);
            map.put("diff", Math.abs(encadrant - president));

            result.add(map);
        }

        return result;
    }

    // Fonction qui répète les noms selon diff
    public static List<String> expandNamesByDiff(List<Map<String, Object>> stats) {
        List<String> result = new ArrayList<>();

        for (Map<String, Object> entry : stats) {
            String nom = (String) entry.get("nom");
            int diff = (Integer) entry.getOrDefault("diff", 0);

            for (int i = 0; i < diff; i++) {
                result.add(nom);
            }
        }

        return result;
    }
    public static List<PFE> assignRandomRapporteurs(List<PFE> pfes, List<String> listeRap, List<String> teachers, int maxPerTeacher) {
        Map<String, Integer> counts = new HashMap<>();

        // 1) Initialiser les compteurs
        for (String t : teachers) {
            counts.put(t, 0);
        }

        // 2) Compter les rapporteurs déjà attribués
        for (PFE pfe : pfes) {
            if (pfe.getRapporteur() != null) {
                counts.put(pfe.getRapporteur(), counts.getOrDefault(pfe.getRapporteur(), 0) + 1);
            }
        }

        Random random = new Random();

        // 3) Pour chaque PFE sans rapporteur
        for (PFE pfe : pfes) {
            if (pfe.getRapporteur() == null) {
                List<String> excluded = new ArrayList<>();
                if (pfe.getEncadrant() != null) excluded.add(pfe.getEncadrant());
                if (pfe.getPresident() != null) excluded.add(pfe.getPresident());

                // a) Premier pool : listeRap filtrée
                List<String> candidates = new ArrayList<>();
                for (String t : listeRap) {
                    if (!excluded.contains(t) && counts.get(t) < maxPerTeacher) {
                        candidates.add(t);
                    }
                }

                // b) Si vide, fallback sur tous teachers
                if (candidates.isEmpty()) {
                    for (String t : teachers) {
                        if (!excluded.contains(t) && counts.get(t) < maxPerTeacher) {
                            candidates.add(t);
                        }
                    }
                }

                // c) Si toujours vide, erreur
                if (candidates.isEmpty()) {
                    throw new RuntimeException("Aucun enseignant disponible pour être rapporteur (max " + maxPerTeacher + " atteint).");
                }

                // d) Tirage aléatoire
                String pick = candidates.get(random.nextInt(candidates.size()));
                pfe.setRapporteur(pick);
                counts.put(pick, counts.get(pick) + 1);
            }
        }

        return pfes;
    }
    public static List<PFE> assignRandomPresidents(List<PFE> pfes, List<String> listePres, List<String> teachers, int maxPerTeacher) {
        Map<String, Integer> counts = new HashMap<>();

        // 1) Initialiser les compteurs
        for (String t : teachers) {
            counts.put(t, 0);
        }

        // 2) Compter les présidents déjà attribués
        for (PFE pfe : pfes) {
            if (pfe.getPresident() != null) {
                counts.put(pfe.getPresident(), counts.getOrDefault(pfe.getPresident(), 0) + 1);
            }
        }

        Random random = new Random();

        // 3) Pour chaque PFE sans président
        for (PFE pfe : pfes) {
            if (pfe.getPresident() == null) {
                List<String> excluded = new ArrayList<>();
                if (pfe.getEncadrant() != null) excluded.add(pfe.getEncadrant());
                if (pfe.getRapporteur() != null) excluded.add(pfe.getRapporteur());

                // a) Premier pool : listePres filtrée
                List<String> candidates = new ArrayList<>();
                for (String t : listePres) {
                    if (!excluded.contains(t) && counts.get(t) < maxPerTeacher) {
                        candidates.add(t);
                    }
                }

                // b) Si vide, fallback sur tous teachers
                if (candidates.isEmpty()) {
                    for (String t : teachers) {
                        if (!excluded.contains(t) && counts.get(t) < maxPerTeacher) {
                            candidates.add(t);
                        }
                    }
                }

                // c) Si toujours vide, erreur
                if (candidates.isEmpty()) {
                    throw new RuntimeException("Aucun enseignant disponible pour être président (max " + maxPerTeacher + " atteint).");
                }

                // d) Tirage aléatoire
                String pick = candidates.get(random.nextInt(candidates.size()));
                pfe.setPresident(pick);
                counts.put(pick, counts.get(pick) + 1);
            }
        }

        return pfes;
    }
    public static List<Map<String, Object>> classifyPresidentByEncadrant(List<Map<String, Object>> stats) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> stat : stats) {
            String nom = (String) stat.get("nom");
            int encadrant = (int) stat.getOrDefault("encadrant", 0);
            int president = (int) stat.getOrDefault("president", 0);

            int diff = encadrant - president;
            String status;
            if (diff < 0) {
                status = "excès";
            } else if (diff > 0) {
                status = "déficit";
            } else {
                status = "parfait";
            }
            int needed = Math.abs(diff);

            Map<String, Object> map = new HashMap<>();
            map.put("nom", nom);
            map.put("status", status);
            map.put("needed", needed);

            result.add(map);
        }

        return result;
    }

    public static List<Map<String, Object>> classifyRapporteurByEncadrant(List<Map<String, Object>> stats) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> stat : stats) {
            String nom = (String) stat.get("nom");
            int encadrant = (int) stat.getOrDefault("encadrant", 0);
            int rapporteur = (int) stat.getOrDefault("rapporteur", 0);

            int diff = encadrant - rapporteur;
            String status;
            if (diff < 0) {
                status = "excès";
            } else if (diff > 0) {
                status = "déficit";
            } else {
                status = "parfait";
            }
            int needed = Math.abs(diff);

            Map<String, Object> map = new HashMap<>();
            map.put("nom", nom);
            map.put("status", status);
            map.put("needed", needed);

            result.add(map);
        }

        return result;
    }

    public static Map<String, List<String>> buildStatusLists(List<Map<String, Object>> details) {
        List<String> deficitList = new ArrayList<>();
        List<String> excessList = new ArrayList<>();

        for (Map<String, Object> detail : details) {
            String nom = (String) detail.get("nom");
            String status = (String) detail.get("status");
            int needed = (int) detail.get("needed");

            if ("déficit".equals(status) && needed > 0) {
                for (int i = 0; i < needed; i++) {
                    deficitList.add(nom);
                }
            }
            if ("excès".equals(status) && needed > 0) {
                for (int i = 0; i < needed; i++) {
                    excessList.add(nom);
                }
            }
        }

        // Créer un map pour retourner les deux listes
        Map<String, List<String>> result = new HashMap<>();
        result.put("deficitList", deficitList);
        result.put("excessList", excessList);

        return result;
    }
    public static List<PFE> rebalancePresidents(List<PFE> pfes, List<String> deficitList, List<String> excessList) {
        // Faire une copie des listes pour ne pas affecter les listes d'origine
        List<String> def = new ArrayList<>(deficitList);
        List<String> exc = new ArrayList<>(excessList);

        // Parcours des PFEs pour ajuster les présidents
        for (PFE pfe : pfes) {
            String curPresident = pfe.getPresident();
            int idxExc = exc.indexOf(curPresident);

            // Si le président est dans la liste excess et qu'il y a des déficits à combler
            if (idxExc != -1 && !def.isEmpty()) {
                // Exclure l'encadrant et le rapporteur
                Set<String> excluded = new HashSet<>();
                if (pfe.getEncadrant() != null) excluded.add(pfe.getEncadrant());
                if (pfe.getRapporteur() != null) excluded.add(pfe.getRapporteur());

                // Trouver un remplaçant valide dans la liste des déficits
                int replacementIdx = -1;
                for (int i = 0; i < def.size(); i++) {
                    if (!excluded.contains(def.get(i))) {
                        replacementIdx = i;
                        break;
                    }
                }

                // Si un remplaçant est trouvé, on remplace le président
                if (replacementIdx != -1) {
                    String nouveauPresident = def.remove(replacementIdx);
                    exc.remove(idxExc);  // Retirer l'ancien président de la liste excess

                    // Mettre à jour le PFE avec le nouveau président
                    pfe.setPresident(nouveauPresident);
                }
            }
        }

        return pfes;
    }

    public static List<PFE> rebalanceRapporteurs(
            List<PFE> pfes,
            List<String> deficitList,
            List<String> excessList) {
        List<String> def = new ArrayList<>(deficitList);
        List<String> exc = new ArrayList<>(excessList);

        // Parcours des PFEs pour ajuster les rapporteurs
        for (PFE pfe : pfes) {
            String curRapporteur = pfe.getRapporteur();
            int idxExc = exc.indexOf(curRapporteur);

            // Si le rapporteur est dans la liste des excès et qu'il y a des déficits à combler
            if (idxExc != -1 && !def.isEmpty()) {

                // Exclure l'encadrant et le président
                Set<String> excluded = new HashSet<>();
                if (pfe.getEncadrant() != null) excluded.add(pfe.getEncadrant());
                if (pfe.getPresident() != null) excluded.add(pfe.getPresident());

                // Trouver un remplaçant valide dans la liste des déficits
                int replacementIdx = -1;
                for (int i = 0; i < def.size(); i++) {
                    if (!excluded.contains(def.get(i))) {
                        replacementIdx = i;
                        break;
                    }
                }

                // Si un remplaçant est trouvé, on remplace le rapporteur
                if (replacementIdx != -1) {
                    String nouveauRapporteur = def.remove(replacementIdx);
                    exc.remove(idxExc);  // Retirer l'ancien rapporteur de la liste des excès

                    // Mettre à jour le PFE avec le nouveau rapporteur
                    pfe.setRapporteur(nouveauRapporteur);
                }
            }
        }

        return pfes;
    }
    public static List<Pfe> generer(List<PFE> pfes,List<String> teachers){
        assignRandomEncadrants(pfes, teachers, 3);
        List<Map<String, Object>> stats = computeTeacherStats(pfes);
        List<Map<String, Object>> diffsEncadRapporteur = computeEncadAllRolesDiff(stats);
        List<Map<String, Object>> diffsEncadPresident = computeEncadAllRoleDiff(stats);


        List<String> expandedNames = expandNamesByDiff(diffsEncadRapporteur);
        assignRandomRapporteurs(pfes,expandedNames,teachers,3);
        assignRandomPresidents(pfes,expandNamesByDiff(diffsEncadPresident),teachers,3);
        List<Map<String, Object>> statess = computeTeacherStats(pfes);

        List<Map<String, Object>> presStats = classifyPresidentByEncadrant(statess);
        Map<String, List<String>> statusLists = buildStatusLists(presStats);
        List<String> presDeficits = statusLists.get("deficitList");
        List<String> presExcesses = statusLists.get("excessList");


        List<PFE> pfAfterPres = rebalancePresidents(pfes, presDeficits, presExcesses);
        System.out.println("Après rééquilibrage des présidents:");


        List<Map<String, Object>> rapStats = classifyRapporteurByEncadrant(statess);
        Map<String, List<String>> statussLists = buildStatusLists(rapStats);
        List<String> rapDeficits = statussLists.get("deficitList");
        List<String> rapExcesses = statussLists.get("excessList");


        List<PFE> pfAfterRap = rebalanceRapporteurs(pfes, rapDeficits, rapExcesses);

        System.out.println("Après rapporteurs :");
        for (PFE pfe : pfAfterRap) {
            System.out.println(pfe);
        }
        List<Map<String, Object>> stas = computeTeacherStats(pfAfterRap);
        System.out.println("Statistiques des enseignants :");
        stas.forEach(stat -> {
            System.out.println("Enseignant: " + stat.get("nom"));
            System.out.println("Encadrant: " + stat.get("encadrant"));
            System.out.println("Rapporteur: " + stat.get("rapporteur"));
            System.out.println("Président: " + stat.get("president"));
            System.out.println("---------------");
        });
        List<Pfe> pfesData = pfes.stream()
                .map(pfe -> {
                    return new Pfe(pfe.getEmailetudiant(), pfe.getRapport(), pfe.getEncadrant(), pfe.getRapporteur(), pfe.getPresident(), pfe.getSalle(), pfe.getDateHeure());
                })
                .collect(Collectors.toList());
        return pfesData;
    }
    public static void main(String[] args) {
    }

    public static class PFE {
        private String emailetudiant;
        private String rapport;
        private String encadrant;
        private String rapporteur;
        private String president;
        private String salle;
        private LocalDateTime dateHeure;

        public PFE(String emailetudiant,
                   String rapport,
                   String encadrant,
                   String rapporteur,
                   String president,
                   String salle,
                   LocalDateTime dateHeure) {
            this.emailetudiant=emailetudiant;
            this.rapport    = rapport;
            this.encadrant  = encadrant;
            this.rapporteur = rapporteur;
            this.president  = president;
            this.salle      = salle;
            this.dateHeure  = dateHeure;
        }

        public String getEmailetudiant(){return emailetudiant;}
        public String getRapport()    { return rapport; }
        public String getEncadrant()  { return encadrant; }
        public String getRapporteur() { return rapporteur; }
        public String getPresident()  { return president; }
        public String getSalle()      { return salle; }
        public LocalDateTime getDateHeure() { return dateHeure; }

        // Setters (si tu veux modifier après création)
        public void setEncadrant(String encadrant)   { this.encadrant = encadrant; }
        public void setRapporteur(String rapporteur) { this.rapporteur = rapporteur; }
        public void setPresident(String president)   { this.president = president; }

        public void setRapport(String rapport) {
            this.rapport = rapport;
        }
        @Override
        public PFE clone() {
            try {
                // Clonage de l'objet PFE
                PFE cloned = (PFE) super.clone();
                // Si `dateHeure` est un objet Date, le cloner aussi
                return cloned;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void setSalle(String salle) {
            this.salle = salle;
        }

        public void setDateHeure(LocalDateTime dateHeure) {
            this.dateHeure = dateHeure;
        }

        @Override
        public String toString() {
            return "PFE{" +
                    "emailetudiant='" +emailetudiant + '\'' +
                    "rapport='" + rapport + '\'' +
                    ", encadrant='" + encadrant + '\'' +
                    ", rapporteur='" + rapporteur + '\'' +
                    ", president='" + president + '\'' +
                    ", salle='" + salle + '\'' +
                    ", dateHeure=" + dateHeure +
                    '}';
        }
    }
}

package com.Application.Gestion.des.PFE.algorithme;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset; // Needed for converting to milliseconds if required by genererDateHeure logic
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// Data structure for a Time Slot (used in unavailability)
record TimeSlot(LocalDateTime start, LocalDateTime end) {
    // Constructor with validation
    public TimeSlot {
        Objects.requireNonNull(start, "Start time cannot be null");
        Objects.requireNonNull(end, "End time cannot be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be strictly before end time.");
        }
    }
}

// Data structure to hold the analysis results
record AnalysisResult(double fitness, int roomConflicts, int teacherConflicts, int unavailabilityConflicts, double totalTeacherIdleTimeHours, String error) {
    // Convenient constructor for success cases
    AnalysisResult(double fitness, int roomConflicts, int teacherConflicts, int unavailabilityConflicts, double totalTeacherIdleTimeHours) {
        this(fitness, roomConflicts, teacherConflicts, unavailabilityConflicts, totalTeacherIdleTimeHours, null);
    }
    // Convenient constructor for error cases
    AnalysisResult(String error) {
        this(0, -1, -1, -1, -1, error);
    }
}

// Class representing a PFE (Projet de Fin d'Études) entry
// Made mutable for salle and dateHeure which are assigned by the GA
class Pfe {
    private final String id;
    private final String titre;
    private final String encadrantId;
    private final String rapporteurId;
    private final String presidentId;
    private String salle; // Mutable
    private LocalDateTime dateHeure; // Mutable

    public Pfe(String id, String titre, String encadrantId, String rapporteurId, String presidentId, String salle, LocalDateTime dateHeure) {
        this.id = id;
        this.titre = titre;
        this.encadrantId = encadrantId;
        this.rapporteurId = rapporteurId;
        this.presidentId = presidentId;
        this.salle = salle;
        this.dateHeure = dateHeure;
    }

    // Copy constructor for creating deep copies within the GA
    public Pfe(Pfe original) {
        this.id = original.id;
        this.titre = original.titre;
        this.encadrantId = original.encadrantId;
        this.rapporteurId = original.rapporteurId;
        this.presidentId = original.presidentId;
        this.salle = original.salle;
        this.dateHeure = original.dateHeure; // LocalDateTime is immutable, so direct assignment is fine
    }

    // Getters
    public String getId() { return id; }
    public String getTitre() { return titre; }
    public String getEncadrantId() { return encadrantId; }
    public String getRapporteurId() { return rapporteurId; }
    public String getPresidentId() { return presidentId; }
    public String getSalle() { return salle; }
    public LocalDateTime getDateHeure() { return dateHeure; }

    // Setters for mutable fields
    public void setSalle(String salle) { this.salle = salle; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    // Helper to get all involved teacher IDs, filtering nulls/blanks
    public List<String> getInvolvedTeachers() {
        return Stream.of(encadrantId, rapporteurId, presidentId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Pfe{" +
                "id='" + id + '\'' +
                ", salle='" + salle + '\'' +
                ", dateHeure=" + (dateHeure != null ? dateHeure.toString() : "null") +
                ", encadrantId='" + encadrantId + '\'' +
                // ... add other fields if needed for debugging
                '}';
    }
}


public class GeneticSchedulerRT {

    // --- Constants ---
    private static final Duration DEFENSE_DURATION = Duration.ofHours(1);
    private static final double HARD_CONSTRAINT_PENALTY = 10000.0;
    private static final double IDLE_TIME_WEIGHT = 1.0 / (3600.0 * 1000.0); // Penalty per millisecond of idle time

    // Define allowed hours (static for use in genererDateHeure)
    private static final List<Integer> HEURES_AUTORISEES = List.of(8, 9, 10, 11, 13, 14, 15, 16); // 8-16 excluding 12

    // --- Instance Variables ---
    private final List<Pfe> pfesInput; // Original PFE data template
    private final List<String> salles;
    private final LocalDateTime dateDebut;
    private final LocalDateTime dateFin;
    private final Map<String, List<TimeSlot>> teacherUnavailability;
    private final Map<String, List<TimeSlot>> roomUnavailability;

    // GA Parameters
    private final int populationSize;
    private final int generations;
    private final double crossoverRate;
    private final double mutationRate;
    private final int elitismCount;

    // GA State
    private List<List<Pfe>> population;
    private List<Pfe> bestSchedule; // Stores the best schedule found
    private final Random random; // Random number generator


    /**
     * Constructor for the Genetic Scheduler.
     *
     * @param pfesInput             List of PFE templates. salle and dateHeure can be null initially.
     * @param salles                List of available room names.
     * @param dateDebut             Start date/time for scheduling (inclusive).
     * @param dateFin               End date/time for scheduling (exclusive).
     * @param teacherUnavailability Map of teacher ID to their unavailable time slots.
     * @param roomUnavailability    Map of room name to its unavailable time slots.
     * @param populationSize        Number of individuals (schedules) in the population.
     * @param generations           Number of generations to run the algorithm.
     * @param crossoverRate         Probability of crossover occurring.
     * @param mutationRate          Probability of mutation occurring per gene (PFE).
     * @param elitismCount          Number of best individuals to carry over to the next generation.
     */
    public GeneticSchedulerRT(List<Pfe> pfesInput, List<String> salles, LocalDateTime dateDebut, LocalDateTime dateFin,
                              Map<String, List<TimeSlot>> teacherUnavailability, Map<String, List<TimeSlot>> roomUnavailability,
                              int populationSize, int generations, double crossoverRate, double mutationRate, int elitismCount) {

        // --- Input Validation ---
        if (pfesInput == null || pfesInput.isEmpty()) throw new IllegalArgumentException("PFE list cannot be null or empty.");
        if (salles == null || salles.isEmpty()) throw new IllegalArgumentException("Salle list cannot be null or empty.");
        if (dateDebut == null || dateFin == null || !dateDebut.isBefore(dateFin)) throw new IllegalArgumentException("Invalid date range provided.");
        if (populationSize <= 0) throw new IllegalArgumentException("Population size must be positive.");
        if (generations <= 0) throw new IllegalArgumentException("Number of generations must be positive.");
        if (crossoverRate < 0 || crossoverRate > 1) throw new IllegalArgumentException("Crossover rate must be between 0 and 1.");
        if (mutationRate < 0 || mutationRate > 1) throw new IllegalArgumentException("Mutation rate must be between 0 and 1.");
        if (elitismCount < 0 || elitismCount >= populationSize) throw new IllegalArgumentException("Elitism count must be non-negative and less than population size.");

        // --- Initialization ---
        // Store copies to prevent external modification issues? For simplicity, assume inputs are not modified externally.
        this.pfesInput = new ArrayList<>(pfesInput); // Use a copy of the list structure
        this.salles = new ArrayList<>(salles);
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.teacherUnavailability = (teacherUnavailability != null) ? new HashMap<>(teacherUnavailability) : Collections.emptyMap();
        this.roomUnavailability = (roomUnavailability != null) ? new HashMap<>(roomUnavailability) : Collections.emptyMap();

        this.populationSize = populationSize;
        this.generations = generations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitismCount = elitismCount;

        this.population = new ArrayList<>(populationSize);
        this.bestSchedule = null;
        this.random = ThreadLocalRandom.current(); // Efficient random number generator
    }

    // --- Core GA Methods ---

    /**
     * Initializes the population with random schedules.
     * Assigns random valid times and rooms to PFEs if they are initially null.
     */
    private void initializePopulation() {
        this.population.clear(); // Ensure population is empty before initializing

        if (this.pfesInput.isEmpty() || this.salles.isEmpty()) {
            System.err.println("Initialization failed: No PFEs or Salles provided.");
            return;
        }

        for (int i = 0; i < this.populationSize; i++) {
            List<Pfe> schedule = new ArrayList<>(this.pfesInput.size());
            for (Pfe pfeTemplate : this.pfesInput) {
                Pfe newPfe = new Pfe(pfeTemplate); // Create a copy from the template

                // Assign random salle if not pre-defined
                if (newPfe.getSalle() == null || newPfe.getSalle().isBlank()) {
                    newPfe.setSalle(this.salles.get(random.nextInt(this.salles.size())));
                }

                // Assign random date/time if not pre-defined
                if (newPfe.getDateHeure() == null) {
                    newPfe.setDateHeure(genererDateHeure(this.dateDebut, this.dateFin, random));
                }
                schedule.add(newPfe);
            }
            this.population.add(schedule);
        }
        if (this.population.isEmpty() && this.populationSize > 0) {
            System.err.println("Warning: Population initialization resulted in an empty population list.");
        }
    }

    /**
     * Generates a random LocalDateTime within the allowed hours and date range.
     *
     * @param start  The start boundary (inclusive).
     * @param end    The end boundary (exclusive).
     * @param random A Random instance.
     * @return A random LocalDateTime snapped to an allowed hour, or null if no valid date can be generated.
     */
    private static LocalDateTime genererDateHeure(LocalDateTime start, LocalDateTime end, Random random) {
        if (HEURES_AUTORISEES.isEmpty()) {
            System.err.println("Cannot generate date: No allowed hours defined.");
            return null; // Or throw?
        }

        long startEpochSecond = start.toEpochSecond(ZoneOffset.UTC);
        long endEpochSecond = end.toEpochSecond(ZoneOffset.UTC);

        if (startEpochSecond >= endEpochSecond) {
            System.err.println("Cannot generate date: Start time is not before end time.");
            return start; // Return start as a fallback?
        }

        final int MAX_ATTEMPTS = 100; // Prevent infinite loops if range is very constrained
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // 1. Pick a random time point within the overall range
            long randomEpochSecond = startEpochSecond + random.nextLong(endEpochSecond - startEpochSecond);
            LocalDateTime randomDateTime = LocalDateTime.ofEpochSecond(randomEpochSecond, 0, ZoneOffset.UTC);

            // 2. Choose a random allowed hour
            int heure = HEURES_AUTORISEES.get(random.nextInt(HEURES_AUTORISEES.size()));

            // 3. Combine the date part of the random time with the chosen hour
            LocalDateTime generated = randomDateTime.withHour(heure).withMinute(0).withSecond(0).withNano(0);

            // 4. Validate: Ensure the generated time is still within the [start, end) interval
            if (!generated.isBefore(start) && generated.isBefore(end)) {
                return generated;
            }
        }
        System.err.println("Failed to generate a valid date within the allowed range and hours after " + MAX_ATTEMPTS + " attempts.");
        // Fallback: return start time snapped to the first allowed hour (if possible)
        LocalDateTime fallback = start.withMinute(0).withSecond(0).withNano(0);
        int firstAllowedHour = HEURES_AUTORISEES.get(0);
        if (fallback.getHour() > firstAllowedHour) {
            fallback = fallback.plusDays(1).withHour(firstAllowedHour); // Try next day
        } else {
            fallback = fallback.withHour(firstAllowedHour);
        }
        if (!fallback.isBefore(start) && fallback.isBefore(end)) return fallback;

        return start; // Ultimate fallback
    }


    /**
     * Checks if two time intervals [start1, end1) and [start2, end2) overlap.
     */
    private boolean intervalsOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        // Basic null check
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            System.err.println("Error: Null date provided to intervalsOverlap.");
            return false; // Treat nulls as non-overlapping? Or throw?
        }
        // Standard interval overlap check: !(end1 <= start2 || end2 <= start1)
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Computes the fitness of a given schedule. Higher fitness is better (closer to 1).
     */
    private double computeFitness(List<Pfe> schedule) {
        int roomConflicts = 0;
        int teacherConflicts = 0;
        int unavailabilityConflicts = 0;
        double totalTeacherIdleTimeMs = 0; // Use double for precision

        // --- Check for Conflicts ---
        for (int i = 0; i < schedule.size(); i++) {
            Pfe pfe1 = schedule.get(i);
            LocalDateTime pfe1Start = pfe1.getDateHeure();

            // Basic validation for the PFE entry itself
            if (pfe1Start == null || pfe1.getSalle() == null || pfe1.getSalle().isBlank()) {
                unavailabilityConflicts += 100; // Penalize invalid schedule entries
                continue; // Skip checks for this invalid PFE
            }
            LocalDateTime pfe1End = pfe1Start.plus(DEFENSE_DURATION);
            List<String> pfe1Teachers = pfe1.getInvolvedTeachers();

            // --- Check #1: Unavailability Conflicts (for pfe1) ---
            // a) Room Unavailability
            List<TimeSlot> roomSlots = roomUnavailability.getOrDefault(pfe1.getSalle(), Collections.emptyList());
            for (TimeSlot slot : roomSlots) {
                if (intervalsOverlap(pfe1Start, pfe1End, slot.start(), slot.end())) {
                    unavailabilityConflicts++;
                    break; // Count once per PFE room conflict
                }
            }
            // b) Teacher Unavailability
            boolean teacherUnavailable = false;
            for (String teacherId : pfe1Teachers) {
                List<TimeSlot> teacherSlots = teacherUnavailability.getOrDefault(teacherId, Collections.emptyList());
                for (TimeSlot slot : teacherSlots) {
                    if (intervalsOverlap(pfe1Start, pfe1End, slot.start(), slot.end())) {
                        unavailabilityConflicts++;
                        teacherUnavailable = true; // Mark conflict found
                        break; // Conflict found for this teacher
                    }
                }
                if (teacherUnavailable) break; // Conflict found for this PFE, no need to check other teachers
            }


            // --- Check #2 & #3: Overlaps with OTHER PFEs ---
            for (int j = i + 1; j < schedule.size(); j++) {
                Pfe pfe2 = schedule.get(j);
                LocalDateTime pfe2Start = pfe2.getDateHeure();
                if (pfe2Start == null) continue; // Skip comparison with invalid PFE
                LocalDateTime pfe2End = pfe2Start.plus(DEFENSE_DURATION);

                // Check for time overlap first
                if (intervalsOverlap(pfe1Start, pfe1End, pfe2Start, pfe2End)) {
                    // 2. Room Conflict Check
                    if (pfe1.getSalle().equals(pfe2.getSalle())) {
                        roomConflicts++;
                    }

                    // 3. Teacher Conflict Check
                    Set<String> pfe2Teachers = new HashSet<>(pfe2.getInvolvedTeachers());
                    boolean commonTeacherFound = false;
                    for(String teacher1 : pfe1Teachers) {
                        if (pfe2Teachers.contains(teacher1)) {
                            commonTeacherFound = true;
                            break;
                        }
                    }
                    if (commonTeacherFound) {
                        teacherConflicts++;
                    }
                }
            }
        } // End of outer loop (checking each PFE)

        // --- Calculate Teacher Idle Time ---
        Map<String, List<LocalDateTime>> teacherSchedules = new HashMap<>();
        for (Pfe pfe : schedule) {
            if (pfe.getDateHeure() == null) continue; // Skip invalid entries
            for (String teacherId : pfe.getInvolvedTeachers()) {
                teacherSchedules.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(pfe.getDateHeure());
            }
        }

        for (List<LocalDateTime> appointments : teacherSchedules.values()) {
            if (appointments.size() > 1) {
                // Sort appointments by time for this teacher
                appointments.sort(Comparator.naturalOrder());

                // Calculate gaps between consecutive appointments
                for (int i = 1; i < appointments.size(); i++) {
                    LocalDateTime prevEndTime = appointments.get(i - 1).plus(DEFENSE_DURATION);
                    LocalDateTime currentStartTime = appointments.get(i);

                    if (currentStartTime.isAfter(prevEndTime)) { // Only count positive gaps
                        Duration gap = Duration.between(prevEndTime, currentStartTime);
                        totalTeacherIdleTimeMs += gap.toMillis();
                    }
                }
            }
        }

        // --- Combine Penalties into Fitness Score ---
        double penalty = 1.0 + // Add 1 to avoid division by zero
                (roomConflicts * HARD_CONSTRAINT_PENALTY) +
                (teacherConflicts * HARD_CONSTRAINT_PENALTY) +
                (unavailabilityConflicts * HARD_CONSTRAINT_PENALTY) +
                (totalTeacherIdleTimeMs * IDLE_TIME_WEIGHT);

        return 1.0 / penalty; // Higher fitness for lower penalty
    }


    /**
     * Selects a parent using Tournament Selection (k=3).
     */
    private List<Pfe> selectParent() {
        final int k = 3; // Tournament size
        List<Pfe> best = null;
        double bestFitness = -1.0;

        if (population.isEmpty()) {
            System.err.println("Cannot select parent from empty population.");
            return null; // Should not happen if initialized correctly
        }

        for (int i = 0; i < k; i++) {
            List<Pfe> candidate = population.get(random.nextInt(population.size()));
            double candidateFitness = computeFitness(candidate); // Recalculate fitness for selection

            if (best == null || candidateFitness > bestFitness) {
                best = candidate;
                bestFitness = candidateFitness;
            }
        }
        // Return the reference to the selected individual in the population
        // Cloning will happen before crossover/mutation if needed.
        return best;
    }

    /**
     * Performs one-point crossover between two parent schedules.
     */
    private List<List<Pfe>> crossover(List<Pfe> parent1, List<Pfe> parent2) {
        // Create deep copies of parents to work with
        List<Pfe> p1Copy = parent1.stream().map(Pfe::new).collect(Collectors.toList());
        List<Pfe> p2Copy = parent2.stream().map(Pfe::new).collect(Collectors.toList());

        List<List<Pfe>> children = new ArrayList<>(2);

        if (random.nextDouble() > this.crossoverRate || p1Copy.size() <= 1) {
            children.add(p1Copy); // No crossover, return copies
            children.add(p2Copy);
            return children;
        }

        // Choose crossover point (between 1 and size-1)
        int point = random.nextInt(p1Copy.size() - 1) + 1;

        List<Pfe> child1 = new ArrayList<>(p1Copy.size());
        List<Pfe> child2 = new ArrayList<>(p2Copy.size());

        // Create children by swapping segments
        for (int i = 0; i < p1Copy.size(); i++) {
            if (i < point) {
                child1.add(new Pfe(p1Copy.get(i))); // Add copy from parent 1
                child2.add(new Pfe(p2Copy.get(i))); // Add copy from parent 2
            } else {
                child1.add(new Pfe(p2Copy.get(i))); // Add copy from parent 2
                child2.add(new Pfe(p1Copy.get(i))); // Add copy from parent 1
            }
        }
        children.add(child1);
        children.add(child2);
        return children;
    }

    /**
     * Mutates an individual schedule.
     */
    private List<Pfe> mutate(List<Pfe> individual) {
        // Create a deep copy to mutate
        List<Pfe> mutatedIndividual = individual.stream().map(Pfe::new).collect(Collectors.toList());

        if (this.salles.isEmpty()) {
            System.err.println("Mutation warning: No salles available to mutate into.");
            // Continue without salle mutation if list is empty
        }

        for (Pfe pfe : mutatedIndividual) {
            // Mutate Date/Time
            if (random.nextDouble() < this.mutationRate) {
                pfe.setDateHeure(genererDateHeure(this.dateDebut, this.dateFin, random));
            }
            // Mutate Salle (only if salles are available)
            if (!this.salles.isEmpty() && random.nextDouble() < this.mutationRate) {
                pfe.setSalle(this.salles.get(random.nextInt(this.salles.size())));
            }
        }
        return mutatedIndividual; // Return the new mutated copy
    }


    /**
     * Runs the genetic algorithm for the specified number of generations.
     *
     * @return A deep copy of the best schedule found, or null if the process fails.
     */
    public List<Pfe> run() {
        System.out.println("Starting Genetic Algorithm...");
        initializePopulation();
        if (population.isEmpty() && populationSize > 0) {
            System.err.println("GA run cancelled: Population initialization failed.");
            return null;
        }
        if (population.isEmpty()) {
            System.err.println("GA run cancelled: Population is empty after initialization.");
            return null;
        }


        for (int gen = 0; gen < this.generations; gen++) {
            List<List<Pfe>> newPopulation = new ArrayList<>(this.populationSize);

            // --- Elitism ---
            // Sort population by fitness (descending)
            // Need to compute fitness for sorting, consider caching if performance is critical
            Map<List<Pfe>, Double> fitnessScores = new HashMap<>();
            for (List<Pfe> ind : population) {
                fitnessScores.put(ind, computeFitness(ind));
            }

            population.sort((ind1, ind2) -> Double.compare(fitnessScores.get(ind2), fitnessScores.get(ind1)));

            // Add elite individuals (deep copies) to the new population
            for (int i = 0; i < this.elitismCount && i < population.size(); i++) {
                List<Pfe> eliteIndividual = population.get(i);
                // Add a deep copy
                newPopulation.add(eliteIndividual.stream().map(Pfe::new).collect(Collectors.toList()));
            }

            // --- Fill the rest of the population ---
            while (newPopulation.size() < this.populationSize) {
                List<Pfe> parent1 = selectParent();
                List<Pfe> parent2 = selectParent();

                if (parent1 == null || parent2 == null) {
                    System.err.println("Warning: Parent selection failed in generation " + gen + ". Skipping reproduction cycle.");
                    break; // Stop filling if selection fails
                }

                List<List<Pfe>> children = crossover(parent1, parent2); // Crossover already returns deep copies

                List<Pfe> child1 = mutate(children.get(0)); // Mutate the copies
                List<Pfe> child2 = mutate(children.get(1));

                newPopulation.add(child1);
                if (newPopulation.size() < this.populationSize) {
                    newPopulation.add(child2);
                }
            }

            population = newPopulation; // Replace old population

            // --- Logging Progress ---
            if ((gen % 20 == 0) || (gen == this.generations - 1)) {
                if (!population.isEmpty()) {
                    // Find the best individual in the current population for logging
                    Map<List<Pfe>, Double> currentFitness = new HashMap<>();
                    for (List<Pfe> ind : population) {
                        currentFitness.put(ind, computeFitness(ind));
                    }
                    List<Pfe> currentBest = population.stream()
                            .max(Comparator.comparingDouble(currentFitness::get))
                            .orElse(null);

                    if (currentBest != null) {
                        AnalysisResult analysis = analyzeSchedule(currentBest); // Analyze the best of this generation
                        System.out.printf("Generation %d: Best Fitness=%.8f (R:%d, T:%d, U:%d, Idle:%.2fh)%n",
                                gen, analysis.fitness(), analysis.roomConflicts(), analysis.teacherConflicts(),
                                analysis.unavailabilityConflicts(), analysis.totalTeacherIdleTimeHours());
                    } else {
                        System.out.println("Generation " + gen + ": Could not determine best individual for logging.");
                    }
                } else {
                    System.out.println("Generation " + gen + ": Population became empty.");
                }
            }


        } // End of generation loop

        // --- Find and Store the Overall Best ---
        if (population.isEmpty()) {
            System.err.println("GA finished, but the final population is empty.");
            this.bestSchedule = null;
            return null;
        }

        // Find the best individual in the final population
        Map<List<Pfe>, Double> finalFitness = new HashMap<>();
        for (List<Pfe> ind : population) {
            finalFitness.put(ind, computeFitness(ind));
        }
        List<Pfe> finalBest = population.stream()
                .max(Comparator.comparingDouble(finalFitness::get))
                .orElse(null);

        if (finalBest != null) {
            // Store a deep copy as the result
            this.bestSchedule = finalBest.stream().map(Pfe::new).collect(Collectors.toList());
            System.out.println("Genetic Algorithm finished successfully.");
            return this.bestSchedule; // Return the copy
        } else {
            System.err.println("Could not determine the best schedule from the final population.");
            this.bestSchedule = null;
            return null;
        }
    }


    /**
     * Analyzes a given schedule for conflicts and idle time.
     *
     * @param schedule The schedule to analyze. Can be null.
     * @return An AnalysisResult object.
     */
    public AnalysisResult analyzeSchedule(List<Pfe> schedule) {
        if (schedule == null || schedule.isEmpty()) {
            return new AnalysisResult("Invalid or empty schedule provided for analysis.");
        }

        int roomConflicts = 0;
        int teacherConflicts = 0;
        int unavailabilityConflicts = 0;
        double totalTeacherIdleTimeMs = 0;

        // --- Perform Checks (Similar logic to computeFitness, but just counting) ---
        for (int i = 0; i < schedule.size(); i++) {
            Pfe pfe1 = schedule.get(i);
            LocalDateTime pfe1Start = pfe1.getDateHeure();

            if (pfe1Start == null || pfe1.getSalle() == null || pfe1.getSalle().isBlank()) {
                System.err.println("Analysis Warning: Invalid PFE entry found: " + pfe1);
                unavailabilityConflicts += 100; // Indicate severe issue in analysis
                continue;
            }
            LocalDateTime pfe1End = pfe1Start.plus(DEFENSE_DURATION);
            List<String> pfe1Teachers = pfe1.getInvolvedTeachers();

            // 1. Check Unavailability
            List<TimeSlot> roomSlots = roomUnavailability.getOrDefault(pfe1.getSalle(), Collections.emptyList());
            for (TimeSlot slot : roomSlots) {
                if (intervalsOverlap(pfe1Start, pfe1End, slot.start(), slot.end())) {
                    unavailabilityConflicts++; break;
                }
            }
            boolean teacherUnavailable = false;
            for (String teacherId : pfe1Teachers) {
                List<TimeSlot> teacherSlots = teacherUnavailability.getOrDefault(teacherId, Collections.emptyList());
                for (TimeSlot slot : teacherSlots) {
                    if (intervalsOverlap(pfe1Start, pfe1End, slot.start(), slot.end())) {
                        unavailabilityConflicts++; teacherUnavailable = true; break;
                    }
                }
                if(teacherUnavailable) break;
            }


            // 2. Check overlaps with other PFEs
            for (int j = i + 1; j < schedule.size(); j++) {
                Pfe pfe2 = schedule.get(j);
                LocalDateTime pfe2Start = pfe2.getDateHeure();
                if (pfe2Start == null) continue;
                LocalDateTime pfe2End = pfe2Start.plus(DEFENSE_DURATION);

                if (intervalsOverlap(pfe1Start, pfe1End, pfe2Start, pfe2End)) {
                    // Room Conflict
                    if (pfe1.getSalle().equals(pfe2.getSalle())) roomConflicts++;
                    // Teacher Conflict
                    Set<String> pfe2Teachers = new HashSet<>(pfe2.getInvolvedTeachers());
                    if (pfe1Teachers.stream().anyMatch(pfe2Teachers::contains)) {
                        teacherConflicts++;
                    }
                }
            }
        }

        // --- Calculate Idle Time (same logic as in computeFitness) ---
        Map<String, List<LocalDateTime>> teacherSchedules = new HashMap<>();
        for (Pfe pfe : schedule) {
            if (pfe.getDateHeure() == null) continue;
            for (String teacherId : pfe.getInvolvedTeachers()) {
                teacherSchedules.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(pfe.getDateHeure());
            }
        }
        for (List<LocalDateTime> appointments : teacherSchedules.values()) {
            if (appointments.size() > 1) {
                appointments.sort(Comparator.naturalOrder());
                for (int i = 1; i < appointments.size(); i++) {
                    LocalDateTime prevEndTime = appointments.get(i - 1).plus(DEFENSE_DURATION);
                    LocalDateTime currentStartTime = appointments.get(i);
                    if (currentStartTime.isAfter(prevEndTime)) {
                        totalTeacherIdleTimeMs += Duration.between(prevEndTime, currentStartTime).toMillis();
                    }
                }
            }
        }

        // --- Recalculate Fitness (for consistency in the report) ---
        double fitness = computeFitness(schedule);
        double idleHours = totalTeacherIdleTimeMs / (1000.0 * 3600.0);

        return new AnalysisResult(fitness, roomConflicts, teacherConflicts, unavailabilityConflicts, idleHours);
    }

    public static void evoluer(List<Algorithme.PFE> pfes){
        AtomicInteger counter = new AtomicInteger(1); // start from 1

        List<Pfe> pfesData = pfes.stream()
                .map(pfe -> {
                    String id = String.format("PFE%03d", counter.getAndIncrement());
                    return new Pfe(id, pfe.getRapport(), pfe.getEncadrant(), pfe.getRapporteur(), pfe.getPresident(), pfe.getSalle(), pfe.getDateHeure());
                })
                .collect(Collectors.toList());
//        List<Pfe> pfesData = new ArrayList<>(List.of(
//                new Pfe("PFE001", "Systeme X", "profA", "profB", "profC", null, null),
//                new Pfe("PFE002", "Algo Y", "profD", "profA", "profE", null, null),
//                new Pfe("PFE003", "Interface Z", "profB", "profF", "profG", null, null),
//                new Pfe("PFE004", "Database W", "profA", "profH", "profI", null, null),
//                new Pfe("PFE005", "Mobile App V", "profD", "profJ", "profA", null, null),
//                new Pfe("PFE006", "AI Model", "profF", "profC", "profE", null, null),
//                new Pfe("PFE007", "Network Sim", "profH", "profB", "profG", null, null),
//                new Pfe("PFE008", "Security Proto", "profI", "profD", "profJ", null, null),
//                new Pfe("PFE009", "Web Platform", "profA", "profF", "profH", null, null),
//                new Pfe("PFE010", "Data Analysis", "profC", "profE", "profB", null, null)
//                // Add more for realistic testing
//        ));
        List<String> sallesDisponibles = List.of("101","102","103","104","105","106");
        LocalDateTime dateDebutPlanning = LocalDateTime.of(2024, 6, 17, 8, 0, 0); // June 17th, 8:00 AM
        LocalDateTime dateFinPlanning = LocalDateTime.of(2024, 7, 17, 18, 0, 0);  // June 21st, 6:00 PM (exclusive)

        // 4. Define Unavailability Data
        Map<String, List<TimeSlot>> teacherUnavailability = Map.of(

        );
        Map<String, List<TimeSlot>> roomUnavailability = Map.of(

        );

        // 5. Configure GA parameters
        int populationSize = 150;
        int generations = 300; // Increase for harder problems
        double crossoverRate = 0.85;
        double mutationRate = 0.10; // Adjust based on results
        int elitismCount = 3;

        // 6. Create and run the Genetic Algorithm
        try {
            GeneticSchedulerRT gaRT = new GeneticSchedulerRT(
                    pfesData, sallesDisponibles, dateDebutPlanning, dateFinPlanning,
                    teacherUnavailability, roomUnavailability,
                    populationSize, generations, crossoverRate, mutationRate, elitismCount
            );

            System.out.printf(" -> Population: %d, Generations: %d%n", populationSize, generations);
            System.out.printf(" -> Scheduling %d PFEs between %s and %s%n", pfesData.size(), dateDebutPlanning, dateFinPlanning);

            long startTime = System.currentTimeMillis();
            List<Pfe> bestSchedule = gaRT.run();
            long endTime = System.currentTimeMillis();

            System.out.printf("%n--- Genetic Algorithm Finished in %.3f seconds ---%n", (endTime - startTime) / 1000.0);

            // 7. Analyze the result
            if (bestSchedule != null) {
                AnalysisResult analysis = gaRT.analyzeSchedule(bestSchedule); // Analyze the final best schedule

                System.out.println("\nBest Schedule Analysis:");
                if(analysis.error() != null) {
                    System.out.println("  Error during analysis: " + analysis.error());
                } else {
                    System.out.printf("  Fitness: %.8f%n", analysis.fitness());
                    System.out.printf("  Room Conflicts (Overlap): %d%n", analysis.roomConflicts());
                    System.out.printf("  Teacher Conflicts (Overlap): %d%n", analysis.teacherConflicts());
                    System.out.printf("  Unavailability Conflicts: %d%n", analysis.unavailabilityConflicts());
                    System.out.printf("  Total Teacher Idle Time: %.2f hours%n", analysis.totalTeacherIdleTimeHours());

                    if (analysis.roomConflicts() == 0 && analysis.teacherConflicts() == 0 && analysis.unavailabilityConflicts() == 0) {
                        System.out.println("\n>>> Valid schedule found (0 hard conflicts) <<<");
                        // Sort schedule by date for better readability
                        bestSchedule.sort(Comparator.comparing(Pfe::getDateHeure, Comparator.nullsLast(Comparator.naturalOrder()))); // Handle potential nulls defensively

                        System.out.println("\nOptimal Planning (Sorted by Date):");
                        for (Pfe pfe : bestSchedule) {
                            System.out.printf("  - %s: %s @ %s (Enc:%s, Rap:%s, Pres:%s)%n",
                                    pfe.getId(), pfe.getSalle(),
                                    (pfe.getDateHeure() != null ? pfe.getDateHeure().toString() : "N/A"), // Format date as needed
                                    pfe.getEncadrantId(), pfe.getRapporteurId(), pfe.getPresidentId());
                        }
                    } else {
                        System.out.println("\n>>> Warning: The best schedule found still contains hard conflicts! <<<");
                        System.out.println("  Review conflicts breakdown above.");
                        System.out.println("  Consider increasing generations/population, adjusting penalties, or checking problem constraints.");
                    }
                }
            } else {
                System.out.println("\n>>> Genetic algorithm did not produce a valid schedule. <<<");
                System.out.println("   Check logs for errors during initialization or execution.");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("\n--- Error during GA setup ---");
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("\n--- An unexpected error occurred during GA execution ---");
            e.printStackTrace();
        }
    }

    // --- Main Method for Example Usage ---
    public static void main(String[] args) {

    }
}

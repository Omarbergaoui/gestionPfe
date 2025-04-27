package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.Authentication.UserNotFoundException;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
import com.Application.Gestion.des.PFE.Dtos.UserDto;
import com.Application.Gestion.des.PFE.departement.DepartmentNotFoundException;
import com.Application.Gestion.des.PFE.disponibilte.AvailableDateException;
import com.Application.Gestion.des.PFE.disponibilte.DisponibilityNotFoundException;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateException;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateFormatException;
import com.Application.Gestion.des.PFE.email.EmailService;
import com.Application.Gestion.des.PFE.enumeration.Role;
import com.Application.Gestion.des.PFE.token.TokenRepository;
import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.departement.DepartementRepository;
import com.Application.Gestion.des.PFE.departement.DepartementRequest;
import com.Application.Gestion.des.PFE.token.Token;
import com.Application.Gestion.des.PFE.salle.DisponibilityReq;
import com.Application.Gestion.des.PFE.user.UserEntity;
import com.Application.Gestion.des.PFE.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Store;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EnseignantService {
    private final EnseignantRepository enseignantRepository;
    private final TokenRepository tokenRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public EnseignantDto createEnseignant(EnseignantRequest enseignantRequest) {
        if (enseignantRepository.findByEmail(enseignantRequest.email()) != null) {
            throw new EnseignantAlreadyExistsException("A teacher with this email already exists.");
        }
        Optional<Departement> optionalDepartement = departementRepository.findById(enseignantRequest.DepartementId());
        if (optionalDepartement.isEmpty()) {
            throw new DepartmentNotFoundException("The specified department does not exist.");
        } else {
            Departement departement = optionalDepartement.get();
            String activationCode = generateRandomCode();
            emailService.sendActivationEmail(enseignantRequest.email(), activationCode);
            Enseignant enseignant = Enseignant.builder()
                    .firstname(enseignantRequest.firstName())
                    .lastname(enseignantRequest.lastName())
                    .matiere(enseignantRequest.matiere())
                    .email(enseignantRequest.email())
                    .role(Role.ENSEIGNANT)
                    .password(passwordEncoder.encode(enseignantRequest.password()))
                    .activationcode(activationCode)
                    .departementId(departement)
                    .accountLocked(false)
                    .enable(true)
                    .build();

            enseignantRepository.save(enseignant);
            return EnseignantDto.builder()
                    .id(enseignant.getId())
                    .firstName(enseignant.getFirstname())
                    .lastName(enseignant.getLastname())
                    .role(enseignant.getRole())
                    .email(enseignant.getEmail())
                    .matiere(enseignant.getMatiere())
                    .departementId(enseignant.getDepartementId())
                    .disponibilite(enseignant.getDisponibilite())
                    .build();
        }
    }


    public List<EnseignantDto> getAllEnseignants() {
        return enseignantRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != null && !Role.ADMIN.equals(user.getRole()))
                .map(user -> {
                    Enseignant enseignant = (Enseignant) user;
                    return EnseignantDto.builder()
                            .id(enseignant.getId())
                            .firstName(enseignant.getFirstname()) // Assuming firstname in Enseignant
                            .lastName(enseignant.getLastname())   // Assuming lastname in Enseignant
                            .role(enseignant.getRole())
                            .email(enseignant.getEmail())
                            .matiere(enseignant.getMatiere())
                            // Assuming DepartementDto mapping or annotations are handled
                            .departementId(enseignant.getDepartementId())
                            .disponibilite(
                                    enseignant.getDisponibilite() != null
                                            ? enseignant.getDisponibilite().stream()
                                            .filter(d -> d.isAfter(LocalDateTime.now()))
                                            .collect(Collectors.toList())
                                            : new ArrayList<>()
                            )
                            .build();
                })
                .collect(Collectors.toList()); // Collect the mapped DTOs into a list
    }


    public EnseignantDto getEnseignantByEmail(EnseignantRequestName enseignantRequestName) {
        Enseignant enseignant = enseignantRepository.findByEmail(enseignantRequestName.Email());
        if (enseignant == null) {
            throw new EnseignantNotFoundException("Teacher not found");
        } else if (enseignant.getRole().equals(Role.ADMIN)) {
            throw new EnseignantNotFoundException("Teacher not found");
        } else {
            return EnseignantDto.builder()
                    .id(enseignant.getId())
                    .firstName(enseignant.getFirstname())
                    .lastName(enseignant.getLastname())
                    .role(enseignant.getRole())
                    .email(enseignant.getEmail())
                    .matiere(enseignant.getMatiere())
                    .departementId(enseignant.getDepartementId())
                    .disponibilite(enseignant.getDisponibilite() != null
                            ? enseignant.getDisponibilite().stream()
                            .filter(d -> d.isAfter(LocalDateTime.now()))
                            .collect(Collectors.toList())
                            : new ArrayList<>())
                    .build();

        }
    }


    public EnseignantDto getEnseignantById(EnseignantRequestId enseignantRequestId) {
        Optional<Enseignant> enseignant = enseignantRepository.findById(enseignantRequestId.id());
        if (enseignant.isEmpty()) {
            throw new EnseignantNotFoundException("Teacher not found");
        } else if (enseignant.get().getRole().equals(Role.ADMIN)) {
            throw new EnseignantNotFoundException("Teacher not found");
        } else {
            return EnseignantDto.builder()
                    .id(enseignant.get().getId())
                    .firstName(enseignant.get().getFirstname())
                    .lastName(enseignant.get().getLastname())
                    .role(enseignant.get().getRole())
                    .email(enseignant.get().getEmail())
                    .matiere(enseignant.get().getMatiere())
                    .departementId(enseignant.get().getDepartementId())
                    .disponibilite(
                            enseignant.get().getDisponibilite() != null
                                    ? enseignant.get().getDisponibilite().stream()
                                    .filter(d -> d.isAfter(LocalDateTime.now()))
                                    .collect(Collectors.toList())
                                    : new ArrayList<>()
                    )
                    .build();
        }
    }

    private Enseignant getEnseignantByIdprivate(EnseignantRequestId enseignantRequestId) {
        Optional<Enseignant> enseignant = enseignantRepository.findById(enseignantRequestId.id());
        if (enseignant.isEmpty() || enseignant.get().getRole().equals(Role.ADMIN)) {
            throw new EnseignantNotFoundException("Teacher not found");
        }
        return enseignant.get();
    }

    private Enseignant getEnseignantByEmailprivate(EnseignantRequestName enseignantRequestName) {
        Enseignant enseignant = enseignantRepository.findByEmail(enseignantRequestName.Email());
        if (enseignant == null || enseignant.getRole().equals(Role.ADMIN)) {
            throw new EnseignantNotFoundException("Teacher not found");
        }
        return enseignant;
    }

    public Object UpdateEnseignantById(UserEntity user, EnseignantRequestUpdate enseignantRequestUpdate) {
        if (user.getRole().equals(Role.ADMIN)) {
            if (enseignantRequestUpdate.firstName() != null) {
                user.setFirstname(enseignantRequestUpdate.firstName());
            }
            if (enseignantRequestUpdate.lastName() != null) {
                user.setLastname(enseignantRequestUpdate.lastName());
            }
            userRepository.save(user);
            return UserDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstname())
                    .lastName(user.getLastname())
                    .role(user.getRole())
                    .email(user.getEmail())
                    .build();
        } else {
            Enseignant enseignant = (Enseignant) user;
            if (enseignantRequestUpdate.Matiere() != null) {
                enseignant.setMatiere(enseignantRequestUpdate.Matiere());
            }
            if (enseignantRequestUpdate.firstName() != null) {
                enseignant.setFirstname(enseignantRequestUpdate.firstName());
            }
            if (enseignantRequestUpdate.lastName() != null) {
                enseignant.setLastname(enseignantRequestUpdate.lastName());
            }
            enseignantRepository.save(enseignant);
            return EnseignantDto.builder()
                    .id(enseignant.getId())
                    .firstName(enseignant.getFirstname())
                    .lastName(enseignant.getLastname())
                    .role(enseignant.getRole())
                    .email(enseignant.getEmail())
                    .matiere(enseignant.getMatiere())
                    .departementId(enseignant.getDepartementId())
                    .disponibilite(
                            enseignant.getDisponibilite() != null
                                    ? enseignant.getDisponibilite().stream()
                                    .filter(d -> d.isAfter(LocalDateTime.now()))
                                    .collect(Collectors.toList())
                                    : new ArrayList<>()
                    )
                    .build();
        }
    }


    public String DeleteEnseignantByName(EnseignantRequestName enseignantRequestName) {
        Enseignant enseignant = getEnseignantByEmailprivate(enseignantRequestName);

        if (enseignant.getRole().equals(Role.CHEFDEPARTEMENT)) {
            throw new UserNotFoundException("Please assign a new Head of Department before deleting the current one.");
        }

        List<Token> tokens = tokenRepository.findByUser(enseignant);
        if (tokens != null && !tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
        }

        enseignantRepository.delete(enseignant);

        return "Teacher deleted successfully";
    }

    public String DeleteEnseignantById(EnseignantRequestId enseignantRequestId) {
        Enseignant enseignant = getEnseignantByIdprivate(enseignantRequestId);
        if (enseignant.getRole().equals(Role.CHEFDEPARTEMENT)) {
            throw new UserNotFoundException("Please assign a new Head of Department before deleting the current one.");
        }

        List<Token> tokens = tokenRepository.findByUser(enseignant);
        if (tokens != null && !tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
        }

        enseignantRepository.delete(enseignant);

        return "Teacher deleted successfully";
    }

    public EnseignantDto UpdateEnseignantDepartement(EnseignantRequestId enseignantRequestId, DepartementRequest departementRequest) {
        Enseignant enseignant = getEnseignantByIdprivate(enseignantRequestId);
        if (departementRepository.findById(departementRequest.id()).isEmpty()) {
            throw new DepartmentNotFoundException("The specified department does not exist.");
        } else {
            if (enseignant.getRole().equals(Role.CHEFDEPARTEMENT)) {
                throw new EnseignantNotFoundException("Please select another head of department before updating this one.");
            }
            Departement departement = departementRepository.findById(departementRequest.id()).get();
            enseignant.setDepartementId(departement);
            enseignantRepository.save(enseignant);
            return EnseignantDto.builder()
                    .id(enseignant.getId())
                    .firstName(enseignant.getFirstname())
                    .lastName(enseignant.getLastname())
                    .role(enseignant.getRole())
                    .email(enseignant.getEmail())
                    .matiere(enseignant.getMatiere())
                    .departementId(enseignant.getDepartementId())
                    .disponibilite(
                            enseignant.getDisponibilite() != null
                                    ? enseignant.getDisponibilite().stream()
                                    .filter(d -> d.isAfter(LocalDateTime.now()))
                                    .collect(Collectors.toList())
                                    : new ArrayList<>()
                    )
                    .build();
        }
    }

    private LocalDateTime parseAndValidateDate(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Invalid date format. Use yyyy/MM/dd HH:mm");
        }
    }

    private boolean isValidDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now)) return false;
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(18, 0));
    }


    public EnseignantDto ajouterdisponibilite(DisponibilityReq disponibilityReq, UserEntity user) {
        LocalDateTime localDateTime = parseAndValidateDate(disponibilityReq.dateTime());
        if (!isValidDateTime(localDateTime)) {
            throw new InvalidDateException("Date is invalid or in the past.");
        }
        if (user == null || user.getRole() == Role.ADMIN) {
            throw new UserNotFoundException("User not Found");
        }
        Enseignant enseignant = (Enseignant) user;
        if (enseignant.getDisponibilite().contains(localDateTime)) {
            throw new AvailableDateException("This date is already available.");
        }
        enseignant.getDisponibilite().add(localDateTime);
        enseignantRepository.save(enseignant);
        return EnseignantDto.builder()
                .id(enseignant.getId())
                .firstName(enseignant.getFirstname())
                .lastName(enseignant.getLastname())
                .role(enseignant.getRole())
                .email(enseignant.getEmail())
                .matiere(enseignant.getMatiere())
                .departementId(enseignant.getDepartementId())
                .disponibilite(
                        enseignant.getDisponibilite() != null
                                ? enseignant.getDisponibilite().stream()
                                .filter(d -> d.isAfter(LocalDateTime.now()))
                                .collect(Collectors.toList())
                                : new ArrayList<>()
                )
                .build();
    }

    public EnseignantDto deletedisponibilite(DisponibilityReq disponibilityReq, UserEntity user) {
        LocalDateTime localDateTime = parseAndValidateDate(disponibilityReq.dateTime());
        if (!isValidDateTime(localDateTime)) {
            throw new InvalidDateException("Date is invalid or in the past.");
        }
        if (user == null || user.getRole() == Role.ADMIN) {
            throw new UserNotFoundException("User not Found");
        }
        Enseignant enseignant = (Enseignant) user;
        if (!enseignant.getDisponibilite().contains(localDateTime)) {
            throw new DisponibilityNotFoundException("This date is not available for removal.");
        }
        enseignant.getDisponibilite().remove(localDateTime);
        enseignantRepository.save(enseignant);
        return EnseignantDto.builder()
                .id(enseignant.getId())
                .firstName(enseignant.getFirstname())
                .lastName(enseignant.getLastname())
                .role(enseignant.getRole())
                .email(enseignant.getEmail())
                .matiere(enseignant.getMatiere())
                .departementId(enseignant.getDepartementId())
                .disponibilite(
                        enseignant.getDisponibilite() != null
                                ? enseignant.getDisponibilite().stream()
                                .filter(d -> d.isAfter(LocalDateTime.now()))
                                .collect(Collectors.toList())
                                : new ArrayList<>()
                )
                .build();
    }

    public List<EnseignantDto> getEnseignantDisponible(DisponibilityReq disponibilityReq) {
        LocalDateTime localDateTime = parseAndValidateDate(disponibilityReq.dateTime());
        List<Role> allowedRoles = List.of(Role.ENSEIGNANT, Role.CHEFDEPARTEMENT);
        return enseignantRepository.findByRoleInAndDisponibiliteNotContaining(allowedRoles, localDateTime)
                .stream()
                .map(enseignant -> EnseignantDto.builder()
                        .id(enseignant.getId())
                        .firstName(enseignant.getFirstname())
                        .lastName(enseignant.getLastname())
                        .role(enseignant.getRole())
                        .email(enseignant.getEmail())
                        .matiere(enseignant.getMatiere())
                        .departementId(enseignant.getDepartementId())
                        .disponibilite(
                                enseignant.getDisponibilite() != null
                                        ? enseignant.getDisponibilite().stream()
                                        .filter(d -> d.isAfter(LocalDateTime.now()))
                                        .collect(Collectors.toList())
                                        : new ArrayList<>())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<EnseignantDto> getEnseignantIndisponible(DisponibilityReq disponibilityReq) {
        LocalDateTime localDateTime = parseAndValidateDate(disponibilityReq.dateTime());
        List<Role> allowedRoles = List.of(Role.ENSEIGNANT, Role.CHEFDEPARTEMENT);
        return enseignantRepository.findByRoleInAndDisponibiliteContaining(allowedRoles, localDateTime)
                .stream()
                .map(enseignant -> EnseignantDto.builder()
                        .id(enseignant.getId())
                        .firstName(enseignant.getFirstname())
                        .lastName(enseignant.getLastname())
                        .role(enseignant.getRole())
                        .email(enseignant.getEmail())
                        .matiere(enseignant.getMatiere())
                        .departementId(enseignant.getDepartementId())
                        .disponibilite(
                                enseignant.getDisponibilite() != null
                                        ? enseignant.getDisponibilite().stream()
                                        .filter(d -> d.isAfter(LocalDateTime.now()))
                                        .collect(Collectors.toList())
                                        : new ArrayList<>()
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    public EnseignantDto UpdateEnseignantAdmin(UserEntity requestingUser, UpdateEnsAdmin update) {

        if (requestingUser == null || !Role.ADMIN.equals(requestingUser.getRole())) {
            // Use AccessDeniedException for authorization issues
            throw new RuntimeException("User does not have permission to perform this action.");
        }

        Optional<Enseignant> targetEnseignantOpt = enseignantRepository.findById(update.targetId());
        if (targetEnseignantOpt.isEmpty()) {
            throw new EnseignantNotFoundException("Target teacher not found with ID: " + update.targetId());
        }
        Enseignant targetEnseignant = targetEnseignantOpt.get();

        if (Role.ADMIN.equals(targetEnseignant.getRole())) {
            // Use a specific exception if available, otherwise RuntimeException is okay here
            throw new RuntimeException("Cannot modify an ADMIN user through this operation.");
            // throw new RuntimeException("Cannot modify an ADMIN user through this operation.");
        }

        Optional<Departement> targetDepartementOpt = departementRepository.findById(update.DepartementId());
        if (targetDepartementOpt.isEmpty()) {
            throw new DepartmentNotFoundException("Target department not found with ID: " + update.DepartementId());
        }
        Departement targetDepartement = targetDepartementOpt.get();

        Role originalRole = targetEnseignant.getRole();
        Departement originalDepartement = targetEnseignant.getDepartementId();
        String originalDepartementId = (originalDepartement != null) ? originalDepartement.getId() : null;
        String newDepartementId = targetDepartement.getId();

        Role newRole = null;
        try {
            newRole = Role.valueOf(update.role().toUpperCase());
            if (Role.ADMIN.equals(newRole)) {
                throw new RuntimeException("Cannot assign ADMIN role through this operation.");
                //throw new RuntimeException("Cannot assign ADMIN role through this operation.");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified: " + update.role());
            // throw new RuntimeException("Invalid role specified: " + update.role());
        }

        // --- Validation: Changing department of an existing CHEFDEPARTEMENT ---
        if (Role.CHEFDEPARTEMENT.equals(originalRole) && !Objects.equals(originalDepartementId, newDepartementId)) {
            throw new RuntimeException("Cannot change the department of a Head of Department. Change their role first or assign a new Head to the original department.");
            // throw new RuntimeException("Cannot change the department of a Head of Department. Change their role first or assign a new Head to the original department.");
        }

        // --- *** VALIDATION: Assigning CHEFDEPARTEMENT role to a department that already has one *** ---
        // This check is only needed if the role is changing TO Chef
        if (Role.CHEFDEPARTEMENT.equals(newRole) && !Role.CHEFDEPARTEMENT.equals(originalRole)) {
            Enseignant existingChef = targetDepartement.getChefdepartement();
            // Check if the target department has a chef AND that chef is not the person we are currently updating
            if (existingChef != null && !existingChef.getId().equals(targetEnseignant.getId())) {

                throw new RuntimeException("The target department '" + targetDepartement.getNom() + "' already has a Head of Department assigned.");

            }
        }
        // --- End Validation ---


        // --- Apply Updates ---
        boolean requiresDepartementUpdate = false;

        if (update.firstName() != null) {
            targetEnseignant.setFirstname(update.firstName());
        }
        if (update.lastName() != null) {
            targetEnseignant.setLastname(update.lastName());
        }
        targetEnseignant.setMatiere(update.matiere());

        // Update Department association only if it changed
        if (!Objects.equals(originalDepartementId, newDepartementId)) {
            targetEnseignant.setDepartementId(targetDepartement);
        }

        // Update Role & handle corresponding Department Chef updates
        if (!Objects.equals(originalRole, newRole)) {
            targetEnseignant.setRole(newRole);

            // If role changed FROM Chef, remove them from their original department's chef field
            if (Role.CHEFDEPARTEMENT.equals(originalRole) && originalDepartement != null) {
                // Check if they were indeed the chef of that original department before nullifying
                if (originalDepartement.getChefdepartement() != null && originalDepartement.getChefdepartement().getId().equals(targetEnseignant.getId())) {
                    originalDepartement.setChefdepartement(null);
                    departementRepository.save(originalDepartement);
                }
            }

            // If role changed TO Chef, assign them to the target department's chef field
            if (Role.CHEFDEPARTEMENT.equals(newRole)) {
                targetDepartement.setChefdepartement(targetEnseignant);
                requiresDepartementUpdate = true; // Mark target department for saving
            }
        }
        // Redundant check removed - If role remains Chef, logic above handles potential department change restriction.
        // If role remains Chef and department is same, no action needed on department's chef field unless it was wrongly null before.
        // We can add a defensive check here if needed:
        else if (Role.CHEFDEPARTEMENT.equals(newRole) && (targetDepartement.getChefdepartement() == null || !targetDepartement.getChefdepartement().getId().equals(targetEnseignant.getId()))) {
            // If role is still Chef, but the department doesn't list them as chef, fix it.
            targetDepartement.setChefdepartement(targetEnseignant);
            requiresDepartementUpdate = true;
        }


        // --- Save Entities ---
        Enseignant savedEnseignant = enseignantRepository.save(targetEnseignant);

        if (requiresDepartementUpdate) {
            departementRepository.save(targetDepartement);
        }

        // --- Map to DTO and Return ---
        return mapToEnseignantDto(savedEnseignant);
    }

    // Helper mapping function
    private EnseignantDto mapToEnseignantDto(Enseignant enseignant) {
        if (enseignant == null) {
            return null;
        }
        return EnseignantDto.builder()
                .id(enseignant.getId())
                .firstName(enseignant.getFirstname())
                .lastName(enseignant.getLastname())
                .role(enseignant.getRole())
                .email(enseignant.getEmail())
                .matiere(enseignant.getMatiere())
                .departementId(enseignant.getDepartementId()) // Ensure mapping/annotations handle this
                .disponibilite(
                        enseignant.getDisponibilite() != null
                                ? enseignant.getDisponibilite().stream()
                                .filter(d -> d.isAfter(LocalDateTime.now()))
                                .collect(Collectors.toList())
                                : new ArrayList<>()
                )
                .build();
    }

}
package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.Authentication.UserNotFoundException;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EnseignantService {
    private final EnseignantRepository enseignantRepository;
    private final TokenRepository tokenRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public EnseignantDto createEnseignant(EnseignantRequest enseignantRequest) {
        if (enseignantRepository.findByEmail(enseignantRequest.email()) != null) {
            throw new EnseignantAlreadyExistsException("A teacher with this email already exists.");
        }
        Optional<Departement> optionalDepartement = departementRepository.findByNom(enseignantRequest.DepartementName());
        if (optionalDepartement.isEmpty()) {
            Departement departement = departementRepository.save(Departement.builder()
                    .nom(enseignantRequest.DepartementName())
                    .chefdepartement(null)
                    .build());

            String activationCode = generateRandomCode();
            emailService.sendActivationEmail(enseignantRequest.email(), activationCode);

            Enseignant enseignant = Enseignant.builder()
                    .firstname(enseignantRequest.firstName())
                    .lastname(enseignantRequest.lastName())
                    .matiere(enseignantRequest.matiere())
                    .email(enseignantRequest.email())
                    .role(Role.CHEFDEPARTEMENT)
                    .password(passwordEncoder.encode(enseignantRequest.password()))
                    .activationcode(activationCode)
                    .departementId(departement)
                    .accountLocked(false)
                    .enable(false)
                    .build();

            enseignantRepository.save(enseignant);
            departement.setChefdepartement(enseignant);
            departementRepository.save(departement);

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
        else {
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
                    .enable(false)
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
        return enseignantRepository.findByRole(Role.ENSEIGNANT.name())
                .stream()
                .map(enseignant -> EnseignantDto.builder()
                        .id(enseignant.getId())
                        .firstName(enseignant.getFirstname())
                        .lastName(enseignant.getLastname())
                        .role(enseignant.getRole())
                        .email(enseignant.getEmail())
                        .matiere(enseignant.getMatiere())
                        .departementId(enseignant.getDepartementId())
                        .disponibilite(enseignant.getDisponibilite())
                        .build()
                )
                .collect(Collectors.toList());
    }


    public EnseignantDto getEnseignantByEmail(EnseignantRequestName enseignantRequestName) {
        Enseignant enseignant = enseignantRepository.findByEmail(enseignantRequestName.Email());
        if (enseignant == null) {
            throw new EnseignantNotFoundException("Teacher not found");
        }
        else if(enseignant.getRole().equals(Role.ADMIN) ){
            throw new EnseignantNotFoundException("Teacher not found");
        }
        else{
            return  EnseignantDto.builder()
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


    public EnseignantDto getEnseignantById(EnseignantRequestId enseignantRequestId) {
        Optional<Enseignant> enseignant = enseignantRepository.findById(enseignantRequestId.id());
        if (enseignant.isEmpty()) {
            throw new EnseignantNotFoundException("Teacher not found");
        }
        else if(enseignant.get().getRole().equals(Role.ADMIN) ){
            throw new EnseignantNotFoundException("Teacher not found");
        }
        else {
            return EnseignantDto.builder()
                    .id(enseignant.get().getId())
                    .firstName(enseignant.get().getFirstname())
                    .lastName(enseignant.get().getLastname())
                    .role(enseignant.get().getRole())
                    .email(enseignant.get().getEmail())
                    .matiere(enseignant.get().getMatiere())
                    .departementId(enseignant.get().getDepartementId())
                    .disponibilite(enseignant.get().getDisponibilite())
                    .build();
        }
    }

    private Enseignant getEnseignantByIdprivate(EnseignantRequestId enseignantRequestId){
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

    public EnseignantDto UpdateEnseignantById(UserEntity user,EnseignantRequestUpdate enseignantRequestUpdate){
        if(user==null || user.getRole().equals(Role.ADMIN)){
            throw new UserNotFoundException("User not found");
        }
        else{
            Enseignant enseignant=(Enseignant) user;
            if(enseignantRequestUpdate.Matiere()!=null){
                enseignant.setMatiere(enseignantRequestUpdate.Matiere());
            }
            if(enseignantRequestUpdate.firstName()!=null){
                enseignant.setFirstname(enseignantRequestUpdate.firstName());
            }
            if(enseignantRequestUpdate.lastName()!=null){
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
                    .disponibilite(enseignant.getDisponibilite())
                    .build();
        }
    }


    public String DeleteEnseignantByName(EnseignantRequestName enseignantRequestName){
        Enseignant enseignant=getEnseignantByEmailprivate(enseignantRequestName);
        if(enseignant.getRole().equals(Role.CHEFDEPARTEMENT)){
            throw new UserNotFoundException("Please select another head of department before deleting this one.");
        }
        List<Token> tokens=tokenRepository.findByUser(enseignant);
        tokenRepository.deleteAll(tokens);
        return "Teacher deleted successfully";
    }

    public String DeleteEnseignantById(EnseignantRequestId enseignantRequestId){
        Enseignant enseignant=getEnseignantByIdprivate(enseignantRequestId);
        if(enseignant.getRole().equals(Role.CHEFDEPARTEMENT)){
            throw new UserNotFoundException("Please select another head of department before deleting this one.");
        }
        List<Token> tokens=tokenRepository.findByUser(enseignant);
        tokenRepository.deleteAll(tokens);
        return "Teacher deleted successfully";
    }

    public EnseignantDto UpdateEnseignantDepartement(EnseignantRequestId enseignantRequestId, DepartementRequest departementRequest){
       Enseignant enseignant=getEnseignantByIdprivate(enseignantRequestId);
       if(departementRepository.findById(departementRequest.id()).isEmpty()){
           throw new DepartmentNotFoundException("The specified department does not exist.");
       }
       else{
           if(enseignant.getRole().equals(Role.CHEFDEPARTEMENT)){
               throw new EnseignantNotFoundException("");
           }
           Departement departement= departementRepository.findById(departementRequest.id()).get();
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
                   .disponibilite(enseignant.getDisponibilite())
                   .build();
       }
   }

    private LocalDateTime parseAndValidateDate(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Format de date invalide. Utilisez yyyy/MM/dd HH:mm");
        }
    }

    private boolean isValidDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now)) return false;
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(18, 0));
    }


    public EnseignantDto ajouterdisponibilite(DisponibilityReq disponibilityReq,UserEntity user) {
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        if (!isValidDateTime(localDateTime)) {
            throw new InvalidDateException("Date is invalid or in the past.");
        }
        if(user==null || user.getRole()== Role.ADMIN ){
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
                .disponibilite(enseignant.getDisponibilite())
                .build();
    }

    public EnseignantDto deletedisponibilite(DisponibilityReq disponibilityReq,UserEntity user) {
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        if (!isValidDateTime(localDateTime)) {
            throw new InvalidDateException("Date is invalid or in the past.");
        }
        if(user==null || user.getRole() == Role.ADMIN ){
            throw new UserNotFoundException("User not Found");
        }
        Enseignant enseignant = (Enseignant) user;
        if(!enseignant.getDisponibilite().contains(localDateTime)){
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
                .disponibilite(enseignant.getDisponibilite())
                .build();
    }

    public List<EnseignantDto> getEnseignantDisponible(DisponibilityReq disponibilityReq){
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        List<Role> allowedRoles = List.of(Role.ENSEIGNANT, Role.CHEFDEPARTEMENT);
        return enseignantRepository.findByRoleInAndDisponibiliteNotContaining(allowedRoles,localDateTime)
                .stream()
                .map(enseignant -> EnseignantDto.builder()
                        .id(enseignant.getId())
                        .firstName(enseignant.getFirstname())
                        .lastName(enseignant.getLastname())
                        .role(enseignant.getRole())
                        .email(enseignant.getEmail())
                        .matiere(enseignant.getMatiere())
                        .departementId(enseignant.getDepartementId())
                        .disponibilite(enseignant.getDisponibilite())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<EnseignantDto> getEnseignantIndisponible(DisponibilityReq disponibilityReq){
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        List<Role> allowedRoles = List.of(Role.ENSEIGNANT, Role.CHEFDEPARTEMENT);
        return enseignantRepository.findByRoleInAndDisponibiliteContaining(allowedRoles,localDateTime)
                .stream()
                .map(enseignant -> EnseignantDto.builder()
                        .id(enseignant.getId())
                        .firstName(enseignant.getFirstname())
                        .lastName(enseignant.getLastname())
                        .role(enseignant.getRole())
                        .email(enseignant.getEmail())
                        .matiere(enseignant.getMatiere())
                        .departementId(enseignant.getDepartementId())
                        .disponibilite(enseignant.getDisponibilite())
                        .build()
                )
                .collect(Collectors.toList());
    }
}

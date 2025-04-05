package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.email.EmailService;
import com.Application.Gestion.des.PFE.token.TokenRepository;
import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.departement.DepartementRepository;
import com.Application.Gestion.des.PFE.departement.DepartementRequest;
import com.Application.Gestion.des.PFE.token.Token;
import com.Application.Gestion.des.PFE.salle.DisponibilityReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
    public Enseignant createEnseignant(EnseignantRequest enseignantRequest){
        if(enseignantRepository.findByEmail(enseignantRequest.email())!=null){
            throw new RuntimeException("enseignant déja existe");
        }
        if(departementRepository.findById(enseignantRequest.DepartementId()).isPresent()){
            throw new RuntimeException("departement non existant");
        }
        else {
            Departement departement=departementRepository.findById(enseignantRequest.DepartementId()).get();
            String activationcode=generateRandomCode();
            emailService.sendActivationEmail(enseignantRequest.email(),activationcode);
            return Enseignant.builder()
                    .firstname(enseignantRequest.firstName())
                    .lastname(enseignantRequest.lastName())
                    .matiere(enseignantRequest.matiere())
                    .email(enseignantRequest.email())
                    .password(passwordEncoder.encode(enseignantRequest.password()))
                    .activationcode(activationcode)
                    .departementId(departement)
                    .accountLocked(false)
                    .enable(false)
                    .build();
        }
    }

    public List<Enseignant> getAllEnseignants(){
        return enseignantRepository.findAll();
    }

    public Enseignant getEnseigantByEmail(EnseignantRequestName enseignantRequestName){
        if(enseignantRepository.findByEmail(enseignantRequestName.Email())==null){
            throw new RuntimeException("Enseignant non trouvé");
        }
        return enseignantRepository.findByEmail(enseignantRequestName.Email());
    }

    public Enseignant getEnseigantById(EnseignantRequestId enseignantRequestId){
        if(!enseignantRepository.findById(enseignantRequestId.id()).isPresent()){
            throw new RuntimeException("Enseignant non trouvé");
        }
        return enseignantRepository.findById(enseignantRequestId.id()).get();
    }

    public Enseignant UpdateEnseignantById(EnseignantRequestId enseignantRequestId,EnseignantRequestUpdate enseignantRequestUpdate){
        Enseignant enseignant=getEnseigantById(enseignantRequestId);
        enseignant.setMatiere(enseignantRequestUpdate.Matiere());
        enseignant.setFirstname(enseignantRequestUpdate.firstName());
        enseignant.setLastname(enseignantRequestUpdate.lastName());
        return enseignantRepository.save(enseignant);
    }

    public Enseignant UpdateEnseignantByName(EnseignantRequestName enseignantRequestName,EnseignantRequestUpdate enseignantRequestUpdate){
        Enseignant enseignant=getEnseigantByEmail(enseignantRequestName);
        enseignant.setMatiere(enseignantRequestUpdate.Matiere());
        enseignant.setFirstname(enseignantRequestUpdate.firstName());
        enseignant.setLastname(enseignantRequestUpdate.lastName());
        return enseignantRepository.save(enseignant);
    }


    public String DeleteEnseignantByName(EnseignantRequestName enseignantRequestName){
        Enseignant enseignant=getEnseigantByEmail(enseignantRequestName);
        List<Token> tokens=tokenRepository.findByUser(enseignant);
        tokenRepository.deleteAll(tokens);
        return "Enseignant supprimée avec succées";
    }

    public String DeleteEnseignantById(EnseignantRequestId enseignantRequestId){
        Enseignant enseignant=getEnseigantById(enseignantRequestId);
        List<Token> tokens=tokenRepository.findByUser(enseignant);
        tokenRepository.deleteAll(tokens);
        return "Enseignant supprimée avec succées";
    }

   public Enseignant RemoveEnseignantfromDepartment(EnseignantRequestId enseignantRequestId){
       Enseignant enseignant=getEnseigantById(enseignantRequestId);
       enseignant.setDepartementId(null);
       return enseignantRepository.save(enseignant);
   }

   public Enseignant AffecterEnseignantDepartement(EnseignantRequestId enseignantRequestId, DepartementRequest departementRequest){
       Enseignant enseignant=getEnseigantById(enseignantRequestId);
       if(departementRepository.findById(departementRequest.id()).isPresent()){
           throw new RuntimeException("Departement non existant");
       }
       else{
           Departement departement= departementRepository.findById(departementRequest.id()).get();
           enseignant.setDepartementId(departement);
           return enseignantRepository.save(enseignant);
       }
   }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private LocalDateTime parseAndValidateDate(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format de date invalide. Utilisez yyyy/MM/dd HH:mm");
        }
    }

    private boolean isValidDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now)) return false;
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(18, 0));
    }


    public Enseignant ajouterdisponibilite(DisponibilityReq disponibilityReq,EnseignantRequestId enseignantRequestId) {
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        if (!isValidDateTime(localDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date invalide ou dans le passé.");
        }
        Enseignant enseignant=getEnseigantById(enseignantRequestId);
        enseignant.getDisponibilite().add(localDateTime);
        return enseignantRepository.save(enseignant);
    }

    public Enseignant deletedisponibilite(DisponibilityReq disponibilityReq,EnseignantRequestId enseignantRequestId) {
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        if (!isValidDateTime(localDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date invalide ou dans le passé.");
        }
        Enseignant enseignant=getEnseigantById(enseignantRequestId);
        enseignant.getDisponibilite().remove(localDateTime);
        return enseignantRepository.save(enseignant);
    }

    public List<Enseignant> getEnseignantDisponible(DisponibilityReq disponibilityReq){
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        return enseignantRepository.findAll().stream()
                .filter(enseignant -> !enseignant.getDisponibilite().contains(localDateTime))
                .collect(Collectors.toList());
    }

    public List<Enseignant> getEnseignantIndisponible(DisponibilityReq disponibilityReq){
        LocalDateTime localDateTime=parseAndValidateDate(disponibilityReq.dateTime());
        return enseignantRepository.findAll().stream()
                .filter(enseignant -> enseignant.getDisponibilite().contains(localDateTime))
                .collect(Collectors.toList());
    }
}

package com.Application.Gestion.des.PFE.admin;

import com.Application.Gestion.des.PFE.Authentication.UserNotFoundException;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
import com.Application.Gestion.des.PFE.Dtos.UserDto;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantNotFoundException;
import com.Application.Gestion.des.PFE.enumeration.Role;
import com.Application.Gestion.des.PFE.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    public UserDto Update(UserEntity user,AdminRequestUpdate adminRequestUpdate){
        if(user==null || user.getRole().equals(Role.ENSEIGNANT) || user.getRole().equals(Role.CHEFDEPARTEMENT)){
            throw new UserNotFoundException("User not found");
        }
        else{
            Admin admin=(Admin) user;
            if(adminRequestUpdate.firstName()!=null){
                admin.setFirstname(adminRequestUpdate.firstName());
            }
            if(adminRequestUpdate.lastName()!=null){
                admin.setLastname(adminRequestUpdate.lastName());
            }
            adminRepository.save(admin);
            return EnseignantDto.builder()
                    .id(admin.getId())
                    .firstName(admin.getFirstname())
                    .lastName(admin.getLastname())
                    .role(admin.getRole())
                    .email(admin.getEmail())
                    .build();
        }
    }

    public UserDto GetByEmail(AdminEmail adminEmail){
        Admin admin = adminRepository.findByEmail(adminEmail.email());
        if (admin == null) {
            throw new UserNotFoundException("User not found");
        }
        else if(admin.getRole().equals(Role.ENSEIGNANT) || admin.getRole().equals(Role.CHEFDEPARTEMENT)){
            throw new EnseignantNotFoundException("User not found");
        }
        else{
            return  UserDto.builder()
                    .id(admin.getId())
                    .firstName(admin.getFirstname())
                    .lastName(admin.getLastname())
                    .role(admin.getRole())
                    .email(admin.getEmail())
                    .build();
        }
    }

    public UserDto GetById(AdminId adminId){
        Optional<Admin> admin = adminRepository.findById(adminId.id());
        if (admin.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        else if(admin.get().getRole().equals(Role.ENSEIGNANT) || admin.get().getRole().equals(Role.CHEFDEPARTEMENT)){
            throw new EnseignantNotFoundException("User not found");
        }
        else{
            return  UserDto.builder()
                    .id(admin.get().getId())
                    .firstName(admin.get().getFirstname())
                    .lastName(admin.get().getLastname())
                    .role(admin.get().getRole())
                    .email(admin.get().getEmail())
                    .build();
        }

    }

}

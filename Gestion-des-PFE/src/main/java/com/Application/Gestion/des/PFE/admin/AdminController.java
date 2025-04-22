package com.Application.Gestion.des.PFE.admin;


import com.Application.Gestion.des.PFE.Dtos.UserDto;
import com.Application.Gestion.des.PFE.user.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/Admin")
public class AdminController {
    private final AdminService adminService;
    @PutMapping("/update")
    public ResponseEntity<UserDto> Update(@AuthenticationPrincipal UserEntity user,AdminRequestUpdate adminRequestUpdate){
        return ResponseEntity.ok(adminService.Update(user,adminRequestUpdate));
    }

    @GetMapping("/Email")
    public ResponseEntity<UserDto> GetByEmail(@RequestBody AdminEmail adminEmail){
        return ResponseEntity.ok(adminService.GetByEmail(adminEmail));
    }

    @GetMapping("/id")
    public ResponseEntity<UserDto> GetByEmail(@RequestParam String  adminid){
        return ResponseEntity.ok(adminService.GetById(new AdminId(adminid)));
    }
}

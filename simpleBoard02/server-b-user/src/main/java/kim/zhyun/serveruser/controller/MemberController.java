package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "계정 조회, 계정 정보 수정, 계정 권한 수정 API")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class MemberController {
    
    @Operation(summary = "모든 계정 정보 조회")
    @GetMapping
    public void findAll() {
        
    }
    
    @Operation(summary = "본인 계정 정보 조회")
    @GetMapping("/{id}")
    public void findById() {
    
    }
    
    @Operation(summary = "본인 계정 정보 수정")
    @PutMapping("/{id}")
    public void updateById() {
    
    }
    
    @Operation(summary = "계정 권한 수정")
    @PutMapping("/{id}/role")
    public void updateByIdAndRole() {
    
    }
    
}

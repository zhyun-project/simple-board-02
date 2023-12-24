package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "회원가입, 로그인, 로그아웃, 회원탈퇴 API")
@RequiredArgsConstructor
@RestController
public class SignController {
    
    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public void signUp() {
    
    }
    
    @Operation(summary = "로그인")
    @PostMapping("/sign-in")
    public void signIn() {
    
    }
    
    @Operation(summary = "로그아웃")
    @GetMapping("/sign-out")
    public void signOut() {
    
    }
    
    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/withdrawal")
    public void withdrawal() {
    
    }
    
}

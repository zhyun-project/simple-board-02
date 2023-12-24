package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "이메일 인증, 이메일 중복 확인, 닉네임 중복 확인 API")
@Validated
@RequiredArgsConstructor
@RequestMapping("/check")
@RestController
public class CheckController {
    
    @Operation(summary = "이메일, 닉네임 중복 확인")
    @GetMapping
    public void signUp(@RequestParam(name = "email", required = false)
                       @Email(message = "올바른 이메일을 입력해주세요.")
                       String email,
                       @RequestParam(name = "nickname", required = false)
                       @Length(min = 1, max = 6, message = "6글자 이하로 입력해주세요.")
                       String nickname) {
        
    }
    
    @Operation(summary = "이메일로 인증코드 전송")
    @PostMapping("/auth")
    public void sendEmail() {
    
    }
    
    @Operation(summary = "메일 인증")
    @GetMapping("/auth")
    public void sendEmail(@RequestParam(name = "code") String code) {
    
    }
    
}

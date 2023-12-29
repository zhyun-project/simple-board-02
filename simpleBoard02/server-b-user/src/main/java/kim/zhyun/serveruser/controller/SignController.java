package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kim.zhyun.serveruser.data.ApiResponse;
import kim.zhyun.serveruser.data.SignupRequest;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static kim.zhyun.serveruser.data.message.ResponseMessage.SUCCESS_FORMAT_SIGN_UP;


@Tag(name = "회원가입, 로그인, 로그아웃, 회원탈퇴 API")
@RequiredArgsConstructor
@RestController
public class SignController {
    private final SignUpService signUpService;
    
    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(HttpServletRequest request, SignupRequest signupRequest) {
        signUpService.saveMember(request.getSession().getId(), signupRequest);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                        .status(true)
                        .message(String.format(SUCCESS_FORMAT_SIGN_UP, signupRequest.getNickname()))
                .build());
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

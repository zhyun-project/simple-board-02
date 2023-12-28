package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kim.zhyun.serveruser.data.ApiResponse;
import kim.zhyun.serveruser.data.EmailAuthCodeRequest;
import kim.zhyun.serveruser.data.message.ResponseMessage;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static kim.zhyun.serveruser.data.message.ResponseMessage.*;


@Tag(name = "이메일 인증, 이메일 중복 확인, 닉네임 중복 확인 API")
@RequiredArgsConstructor
@Validated
@RequestMapping("/check")
@RestController
public class CheckController {
    private final SignUpService signupService;
    
    @Operation(summary = "이메일, 닉네임 중복 확인")
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> duplicateCheck(HttpServletRequest request,
                                                            @RequestParam(name = "email", required = false)
                                                            @Email(message = "올바른 이메일 주소를 입력해주세요.", regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
                                                            String email,
                                                            @RequestParam(name = "nickname", required = false)
                                                                @Length(min = 1, max = 6, message = "1글자 이상, 6글자 이하로 입력해주세요.")
                                                                String nickname) {
        String sessionId = request.getSession().getId();
        
        boolean result = false;
        ResponseMessage message = SIGN_UP_CHECK_VALUE_IS_EMPTY;
        
        // email 중복확인
        if (email != null) {
            result = signupService.availableEmail(email, sessionId);
            message = result ? SIGN_UP_AVAILABLE_EMAIL
                             : SIGN_UP_UNAVAILABLE_EMAIL;
        }
        
        // 닉네임 중복확인
        if (nickname != null) {
            result = signupService.availableNickname(nickname, sessionId);
            message = result ? SIGN_UP_AVAILABLE_NICKNAME
                             : SIGN_UP_UNAVAILABLE_NICKNAME;
        }
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(result)
                .message(message).build());
    }

    @Operation(summary = "이메일로 인증코드 전송")
    @PostMapping("/auth")
    public void sendEmail(HttpServletRequest request,
                          @Valid @RequestBody EmailAuthCodeRequest userRequest) {
        signupService.sendEmailAuthCode(request.getSession().getId(), userRequest);
    }
    
    @Operation(summary = "메일 인증코드 검증")
    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> authEmailCode(HttpServletRequest request,
                              @RequestParam(name = "code")
                              @NotBlank(message = "코드를 입력해 주세요.") String code) {
        signupService.verifyEmailAuthCode(request.getSession().getId(), code);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(VERIFY_EMAIL_AUTH_SUCCESS).build());
    }
    
}

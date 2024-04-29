package kim.zhyun.serveruser.domain.signup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.zhyun.serveruser.common.annotation.Email;
import kim.zhyun.serveruser.common.annotation.Nickname;
import kim.zhyun.serveruser.common.annotation.VerifyCode;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SIGN_UP_CHECK_VALUE_IS_EMPTY;


@Tag(name = "이메일 인증, 이메일 중복 확인, 닉네임 중복 확인 API")
@RequiredArgsConstructor
@Validated
@RequestMapping("/check")
@RestController
public class CheckApiController {
    private final SignUpBusiness signUpBusiness;
    
    @Operation(summary = "이메일, 닉네임 중복 확인")
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> duplicateCheck(
            HttpServletRequest request,
            
            @RequestParam(name = "email", required = false)
            @Email String email,
            
            @RequestParam(name = "nickname", required = false)
            @Nickname String nickname
    ) {
        String sessionId = request.getSession().getId();
        
        boolean result = false;
        String message = RESPONSE_SIGN_UP_CHECK_VALUE_IS_EMPTY;
        
        // email 중복확인
        if (email != null) {
            message = signUpBusiness.emailDuplicateCheck(email, sessionId);
        }
        
        // 닉네임 중복확인
        if (nickname != null) {
            message = signUpBusiness.nicknameDuplicateCheck(nickname, sessionId);
        }
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(result)
                .message(message)
                .build());
    }

    @Operation(summary = "이메일로 인증코드 전송")
    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> sendEmail(
            HttpServletRequest request,
            
            @RequestBody
            @Valid EmailAuthCodeRequest userRequest
    ) {
        String message = signUpBusiness.sendEmailAuthCode(request.getSession().getId(), userRequest);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(message)
                .build());
    }
    
    @Operation(summary = "메일 인증코드 검증")
    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> authEmailCode(
            HttpServletRequest request,
            
            @RequestParam(name = "code")
            @VerifyCode String code
    ) {
        String message = signUpBusiness.verifyEmailAuthCode(request.getSession().getId(), code);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(message)
                .build());
    }
    
}

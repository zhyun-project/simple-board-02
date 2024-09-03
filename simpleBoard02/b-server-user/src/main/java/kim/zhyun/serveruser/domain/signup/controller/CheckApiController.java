package kim.zhyun.serveruser.domain.signup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.serveruser.common.annotation.Email;
import kim.zhyun.serveruser.common.annotation.Nickname;
import kim.zhyun.serveruser.common.annotation.VerifyCode;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SIGN_UP_AVAILABLE_EMAIL;
import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SIGN_UP_AVAILABLE_NICKNAME;


@RequiredArgsConstructor
@Validated
@RequestMapping("/check")
@RestController
public class CheckApiController {
    private final SignUpBusiness signUpBusiness;


    @Operation(tags = "1. 이메일 중복 확인")
    @GetMapping("/duplicate-email")
    public ResponseEntity<ApiResponse<Void>> duplicateCheckEmail(
            HttpServletRequest request,

            @Parameter(name = "email", example = "test@gmail.com", required = true)
            @RequestParam(name = "email")
            @Email String email
    ) {
        String sessionId = request.getSession().getId();

        String message = signUpBusiness.emailDuplicateCheck(email, sessionId);
        boolean result = message.contains(RESPONSE_SIGN_UP_AVAILABLE_EMAIL);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(result)
                .message(message)
                .build());
    }

    @Operation(tags = "2. 이메일 인증코드 전송")
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


    @Operation(tags = "3. 이메일 인증코드 검증")
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

    @Operation(tags = "4. 닉네임 중복 확인")
    @GetMapping("/duplicate-nickname")
    public ResponseEntity<ApiResponse<Void>> duplicateCheckNickname(
            HttpServletRequest request,

            @Parameter(name = "nickname", example = "닉네임", required = true)
            @RequestParam(name = "nickname")
            @Nickname String nickname
    ) {
        String sessionId = request.getSession().getId();

        String message = signUpBusiness.nicknameDuplicateCheck(nickname, sessionId);
        boolean result = message.contains(RESPONSE_SIGN_UP_AVAILABLE_NICKNAME);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(result)
                .message(message)
                .build());
    }

}

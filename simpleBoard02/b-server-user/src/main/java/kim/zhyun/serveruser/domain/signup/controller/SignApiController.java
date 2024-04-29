package kim.zhyun.serveruser.domain.signup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "회원가입, 회원탈퇴 API")
@RequiredArgsConstructor
@RestController
public class SignApiController {
    private final SignUpBusiness signUpBusiness;
    
    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(
            HttpServletRequest request,
            
            @Valid
            @RequestBody SignupRequest signupRequest
    ) {
        String message = signUpBusiness.saveMember(request.getSession().getId(), signupRequest);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(message)
                .build());
    }
    
}

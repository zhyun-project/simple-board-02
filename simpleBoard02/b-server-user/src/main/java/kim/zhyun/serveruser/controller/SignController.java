package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.serveruser.data.SignupRequest;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.response.ApiResponse;
import kim.zhyun.serveruser.service.MemberService;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.serveruser.data.message.ResponseMessage.*;


@Tag(name = "회원가입, 회원탈퇴 API")
@RequiredArgsConstructor
@RestController
public class SignController {
    private final SignUpService signUpService;
    private final MemberService memberService;
    
    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(HttpServletRequest request,
                                                    @Valid @RequestBody SignupRequest signupRequest) {
        signUpService.saveMember(request.getSession().getId(), signupRequest);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(String.format(RESPONSE_SUCCESS_FORMAT_SIGN_UP, signupRequest.getNickname())).build());
    }
    
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(JWT_HEADER) String jwt, Authentication authentication) {
        JwtUserDto principal = (JwtUserDto) authentication.getPrincipal();
        memberService.logout(jwt, principal.getEmail());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(String.format(RESPONSE_SUCCESS_FORMAT_SIGN_OUT, principal.getNickname(), principal.getEmail())).build());
    }
    
    @Operation(summary = "회원탈퇴")
    @PostMapping("/withdrawal")
    public ResponseEntity<Object> withdrawal(@RequestHeader(JWT_HEADER) String jwt) {
        UserDto withdrawal = memberService.withdrawal(jwt);
        
        return ResponseEntity.ok(ApiResponse.builder()
                        .status(true)
                        .message(String.format(RESPONSE_USER_WITHDRAWAL, withdrawal.getNickname(), withdrawal.getEmail())).build());
    }
    
}

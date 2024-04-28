package kim.zhyun.serveruser.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.zhyun.serveruser.common.model.ApiResponse;
import kim.zhyun.serveruser.domain.member.business.MemberBusiness;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static kim.zhyun.jwt.constants.JwtConstants.JWT_HEADER;
import static kim.zhyun.serveruser.common.message.ResponseMessage.*;
import static kim.zhyun.serveruser.common.model.type.RoleType.TYPE_ADMIN;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;


@Tag(name = "계정 조회, 계정 정보 수정, 계정 권한 수정, 로그아웃, 탈퇴 API")
@Validated
@RequiredArgsConstructor
@RestController
public class MemberApiController {
    private final MemberBusiness memberBusiness;
    
    @Operation(summary = "모든 계정 정보 조회")
    @PreAuthorize("hasRole('"+TYPE_ADMIN+"')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        List<UserResponse> response = memberBusiness.findAll();
        
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .status(true)
                .message(RESPONSE_USER_REFERENCE_ALL)
                .result(response)
                .build());
    }
    
    @Operation(summary = "본인 계정 정보 조회")
    @PostAuthorize("returnObject.body.result.email == T(kim.zhyun.jwt.domain.dto.JwtUserInfoDto).from(principal).email")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(
            @PathVariable long id
    ) {
        UserResponse response = memberBusiness.findById(id);
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(true)
                .message(RESPONSE_USER_REFERENCE_ME)
                .result(response)
                .build());
    }
    
    @Operation(summary = "본인 계정 정보 수정 (닉네임, 비밀번호만 변경)")
    @PreAuthorize("#request.email == T(kim.zhyun.jwt.domain.dto.JwtUserInfoDto).from(principal).email")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateById(
            HttpServletRequest http,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse savedUser = memberBusiness.updateUserInfo(http.getSession().getId(), request);
        
        return ResponseEntity.created(fromCurrentRequestUri().build().toUri())
                .body(ApiResponse.<Void>builder()
                        .status(true)
                        .message(String.format(
                                RESPONSE_USER_INFO_UPDATE,
                                savedUser.getNickname()))
                        .build());
    }
    
    @Operation(summary = "계정 권한 수정")
    @PreAuthorize("hasRole('"+TYPE_ADMIN+"')")
    @PutMapping("/role")
    public ResponseEntity<ApiResponse<Void>> updateByIdAndRole(
            @Valid @RequestBody UserGradeUpdateRequest request
    ) {
        String responseMessage = memberBusiness.updateUserGrade(request);
        
        return ResponseEntity.created(fromCurrentContextPath().path("/user").build().toUri())
                .body(ApiResponse.<Void>builder()
                        .status(true)
                        .message(responseMessage)
                        .build());
    }
    
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(JWT_HEADER) String jwt
    ) {
        String responseMessage = memberBusiness.logout(jwt);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message(responseMessage)
                .build());
    }
    
    @Operation(summary = "회원탈퇴")
    @PostMapping("/withdrawal")
    public ResponseEntity<Object> withdrawal(
            @RequestHeader(JWT_HEADER) String jwt
    ) {
        String responseMessage = memberBusiness.withdrawal(jwt);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(responseMessage)
                .build());
    }
    
}

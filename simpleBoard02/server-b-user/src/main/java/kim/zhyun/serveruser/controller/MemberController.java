package kim.zhyun.serveruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.zhyun.serveruser.data.UserGradeUpdateRequest;
import kim.zhyun.serveruser.data.UserUpdateRequest;
import kim.zhyun.serveruser.data.response.ApiResponse;
import kim.zhyun.serveruser.data.response.UserResponse;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static kim.zhyun.serveruser.data.message.ResponseMessage.*;
import static kim.zhyun.serveruser.data.type.RoleType.TYPE_ADMIN;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.*;


@Tag(name = "계정 조회, 계정 정보 수정, 계정 권한 수정 API")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class MemberController {
    private final MemberService memberService;
    
    @Operation(summary = "모든 계정 정보 조회")
    @PreAuthorize("hasRole('"+TYPE_ADMIN+"')")
    @GetMapping
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_USER_REFERENCE_ALL)
                .result(memberService.findAll()).build());
    }
    
    @Operation(summary = "본인 계정 정보 조회")
    @PostAuthorize("returnObject.body.result.email == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).email")
    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_USER_REFERENCE_ME)
                .result(memberService.findById(id)).build());
    }
    
    @Operation(summary = "본인 계정 정보 수정 (닉네임, 비밀번호만 변경)")
    @PreAuthorize("#request.email == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).email")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateById(HttpServletRequest http,
                                             @Valid @RequestBody UserUpdateRequest request) {
        UserResponse savedUser = memberService.updateUserInfo(http.getSession().getId(), request);
        
        return ResponseEntity.created(fromCurrentRequestUri().build().toUri())
                .body(ApiResponse.builder()
                        .status(true)
                        .message(String.format(RESPONSE_USER_INFO_UPDATE, savedUser.getNickname())).build());
    }
    
    @Operation(summary = "계정 권한 수정")
    @PreAuthorize("hasRole('"+TYPE_ADMIN+"')")
    @PutMapping("/role")
    public ResponseEntity<Object> updateByIdAndRole(@RequestBody UserGradeUpdateRequest request) {
        UserResponse savedUser = memberService.updateUserGrade(request);
        
        return ResponseEntity.created(fromCurrentContextPath().path("/user").build().toUri())
                .body(ApiResponse.builder()
                        .status(true)
                        .message(String.format(RESPONSE_USER_GRADE_UPDATE,
                                savedUser.getNickname(),
                                savedUser.getRole().getGrade())).build());
    }
    
}

package kim.zhyun.serveruser.domain.member.business;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.service.JwtLogoutService;
import kim.zhyun.jwt.util.SecurityUtil;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.service.MemberService;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static kim.zhyun.serveruser.common.message.ResponseMessage.*;

@RequiredArgsConstructor
@Service
public class MemberBusiness {
    
    private final MemberService memberService;
    private final SessionUserService sessionUserService;
    private final JwtLogoutService jwtLogoutService;

    private final UserConverter userConverter;

    
    public UserResponse findById(long userId) {
        UserEntity userEntity = memberService.findByIdWithThrow(userId);
        return userConverter.toResponse(userEntity);
    }
    
    public List<UserResponse> findAll() {
        return memberService.findAll()
                .stream()
                .map(userConverter::toResponse)
                .toList();
    }
    
    /**
     * 유저 정보 수정
     */
    public String updateUserInfo(String sessionId, UserUpdateRequest request) {

        // 닉네임 변경 요청인지 확인
        boolean nicknameIsNotBlank = Strings.isNotBlank(request.getNickname());
        
        // 닉네임이 예전에 사용하던것과 같은지 확인
        String originNickname = memberService.findByIdWithThrow(request.getId()).getNickname();
        boolean nicknameIsNotSameOrigin = nicknameIsNotBlank && !request.getNickname().equals(originNickname);
        
        // 신규 닉네임 여부
        boolean isNewNickname = nicknameIsNotSameOrigin && sessionUserService
                .existNicknameDuplicateCheckWithThrow(sessionId, request.getNickname());
        
        // 비밀번호 업데이트 여부
        boolean isNewPassword = Strings.isNotBlank(request.getPassword());

        // db 반영
        UserEntity updatedUserEntity = memberService.updateUserInfo(isNewNickname, isNewPassword, request);

        // 임시 저장정보(session) 삭제
        sessionUserService.deleteById(sessionId);

        return RESPONSE_USER_INFO_UPDATE.formatted(updatedUserEntity.getNickname());
    }
    
    /**
     * 계정 권한 수정
     */
    public String updateUserGrade(UserGradeUpdateRequest request) {
        UserEntity userEntity = memberService.updateUserGrade(request);
        
        return RESPONSE_USER_GRADE_UPDATE.formatted(userEntity.getNickname(), userEntity.getRole().getGrade());
    }
    
    /**
     * 로그아웃
     * - 로그아웃한 jwt를 재사용하지 못하도록 redis에 저장
     */
    public String logout() {
        JwtUserInfoDto jwtUserInfoDto = SecurityUtil.getJwtUserInfoDto();
        String jwt = SecurityUtil.getJwt();

        jwtLogoutService.setLogoutToken(jwt, jwtUserInfoDto);

        SecurityContextHolder.clearContext();
        
        return RESPONSE_SUCCESS_FORMAT_SIGN_OUT.formatted(jwtUserInfoDto.getNickname(), jwtUserInfoDto.getEmail());
    }
    
    /**
     * 회원 탈퇴
     */
    public String withdrawal() {
        long userId = SecurityUtil.getUserId();
        UserEntity userEntity = memberService.withdrawal(userId);
        UserResponse response = userConverter.toResponse(userEntity);
        
        return RESPONSE_USER_WITHDRAWAL.formatted(response.getNickname(), response.getEmail());
    }
}

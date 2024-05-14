package kim.zhyun.serveruser.domain.member.business;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.domain.member.service.MemberService;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_PREFIX;
import static kim.zhyun.serveruser.common.message.ResponseMessage.*;

@RequiredArgsConstructor
@Service
public class MemberBusiness {
    
    private final MemberService memberService;
    private final SessionUserService sessionUserService;
    
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

        return String.format(
                RESPONSE_USER_INFO_UPDATE,
                updatedUserEntity.getNickname());
    }
    
    /**
     * 계정 권한 수정
     */
    public String updateUserGrade(UserGradeUpdateRequest request) {
        UserEntity userEntity = memberService.updateUserGrade(request);
        
        return String.format(
                RESPONSE_USER_GRADE_UPDATE,
                userEntity.getNickname(),
                userEntity.getRole().getGrade());
    }
    
    /**
     * 로그아웃
     */
    public String logout(String headerToken) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtUserInfoDto jwtUserInfoDto = JwtUserInfoDto.from(principal);
        
        String jwt = headerToken.substring(JWT_PREFIX.length());
        
        memberService.logout(jwt, jwtUserInfoDto);
        
        SecurityContextHolder.clearContext();
        
        return String.format(
                RESPONSE_SUCCESS_FORMAT_SIGN_OUT,
                jwtUserInfoDto.getNickname(),
                jwtUserInfoDto.getEmail());
    }
    
    /**
     * 회원 탈퇴
     */
    public String withdrawal(String headerToken) {
        String token = headerToken.substring(JWT_PREFIX.length());
        UserEntity userEntity = memberService.withdrawal(token);
        UserResponse response = userConverter.toResponse(userEntity);
        
        return String.format(
                RESPONSE_USER_WITHDRAWAL,
                response.getNickname(),
                response.getEmail());
    }
}

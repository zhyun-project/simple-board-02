package kim.zhyun.serveruser.domain.member.business;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.service.JwtLogoutService;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.service.MemberService;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static kim.zhyun.serveruser.common.message.ResponseMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@Slf4j
@DisplayName("member business test")
@ExtendWith(MockitoExtension.class)
class MemberBusinessTest {
    
    @InjectMocks MemberBusiness memberBusiness;

    @Mock JwtLogoutService jwtLogoutService;

    @Mock MemberService memberService;
    @Mock SessionUserService sessionUserService;
    @Mock UserConverter userConverter;
    
    
    
    @DisplayName("회원 조회 - 성공")
    @Test
    void findById_success() {
        long userId = 1L;
        
        UserEntity userEntity = getUserEntity(userId, "member@email.mail", "password", "nickname", false);
        given(memberService.findByIdWithThrow(userId)).willReturn(userEntity);
        
        UserResponse userResponse = getUserResponse(userEntity);
        given(userConverter.toResponse(userEntity)).willReturn(userResponse);
        
        
        // when
        UserResponse resultResponse = memberBusiness.findById(userId);
        
        
        // then
        assertEquals(userResponse, resultResponse);
        
        then(memberService).should(times(1)).findByIdWithThrow(userId);
        then(userConverter).should(times(1)).toResponse(userEntity);
    }
    
    
    @DisplayName("회원 조회 - 실패: 예외")
    @Test
    void findById_fail() {
        
        long userId = 10000L;
        
        UserEntity userEntity = getUserEntity(userId, "member@email.mail", "password", "nickname", false);
        given(memberService.findByIdWithThrow(userId)).willThrow(new ApiException(EXCEPTION_SIGNIN_FAIL));
        
        
        // when
        assertThrows(
                ApiException.class,
                () -> memberBusiness.findById(userId),
                EXCEPTION_SIGNIN_FAIL
        );
        
        then(memberService).should(times(1)).findByIdWithThrow(userId);
        then(userConverter).should(times(0)).toResponse(userEntity);
    }
    
    
    @DisplayName("회원 전체 조회")
    @ParameterizedTest
    @MethodSource
    @EmptySource
    void findAll(List<UserEntity> userEntities) {
        // given
        given(memberService.findAll()).willReturn(userEntities);
        
        for (UserEntity userEntity : userEntities) {
            given(userConverter.toResponse(userEntity)).willReturn(getUserResponse(userEntity));
        }
        
        
        // when
        List<UserResponse> resultList = memberBusiness.findAll();
        
        
        // then
        assertEquals(userEntities.size(), resultList.size());
    }
    static Stream<List<UserEntity>> findAll() {
        return Stream.of(
                List.of(
                        getUserEntity(1L, "member1@email.mail", "pas51sworvd", "nickname 1", false),
                        getUserEntity(2L, "member2@email.mail", "p4ass1wodrd", "nickname 2", false),
                        getUserEntity(3L, "member3@email.mail", "1passswor2d", "nickname 3", false)
                )
        );
    }
    
    
    @DisplayName("유저 정보 수정 - 닉네임, 비밀번호")
    @Test
    void updateUserInfo_success() {
        String requestSessionId = "session-id-12213";
        long requestUserId = 1543L;
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .id(requestUserId)
                .email("update_member@email.mail")
                .nickname("update nickname")
                .password("update_password")
                .build();
        
        // -- 사용중이던 user entity 찾기
        UserEntity originUserEntity = getUserEntity(
                userUpdateRequest.getId(), "member@email.mail", "password", "nickname", false
        );
        given(memberService.findByIdWithThrow(userUpdateRequest.getId())).willReturn(originUserEntity);
        
        // -- 신규 닉네임 중복체크 확인
        boolean existDuplicateCheckNickname = true;
        given(sessionUserService.existNicknameDuplicateCheckWithThrow(requestSessionId, userUpdateRequest.getNickname())).willReturn(existDuplicateCheckNickname);
        
        
        // -- db 반영
        UserEntity updatedUserEntity = getUserEntity(
                originUserEntity.getId(), originUserEntity.getEmail(), userUpdateRequest.getPassword(), userUpdateRequest.getNickname(), false
        );
        given(memberService.updateUserInfo(true, true, userUpdateRequest)).willReturn(updatedUserEntity);
        
        // -- 임시 저장정보(session) 삭제
        willDoNothing().given(sessionUserService).deleteById(requestSessionId);
        
        
        // when
        String responseMessage = memberBusiness.updateUserInfo(requestSessionId, userUpdateRequest);
        
        
        // then
        assertEquals(RESPONSE_USER_INFO_UPDATE.formatted(userUpdateRequest.getNickname()), responseMessage);
    }
    
    @DisplayName("유저 정보 수정 - 실패: 닉네임중복확인 안함")
    @Test
    void updateUserInfo_fail() {
        String requestSessionId = "session-id-12213";
        long requestUserId = 1543L;
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .id(requestUserId)
                .email("update_member@email.mail")
                .nickname("update nickname")
                .password("update_password")
                .build();
        
        // -- 사용중이던 user entity 찾기
        UserEntity originUserEntity = getUserEntity(
                userUpdateRequest.getId(), "member@email.mail", "password", "nickname", false
        );
        given(memberService.findByIdWithThrow(userUpdateRequest.getId())).willReturn(originUserEntity);
        
        // -- 신규 닉네임 중복체크 확인
        boolean existDuplicateCheckNickname = false;
        given(sessionUserService.existNicknameDuplicateCheckWithThrow(requestSessionId, userUpdateRequest.getNickname())).willThrow(new ApiException(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK));
        
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> memberBusiness.updateUserInfo(requestSessionId, userUpdateRequest),
                EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK
        );
    }
    
    
    @DisplayName("계정 권한 수정")
    @Test
    void updateUserGrade() {
        long updateUserId = 123432L;
        String updateRoleType = RoleType.TYPE_MEMBER;
        
        UserGradeUpdateRequest userGradeUpdateRequest = UserGradeUpdateRequest.builder()
                .id(updateUserId)
                .role(updateRoleType)
                .build();
        
        UserEntity savedUserEntity = getUserEntity(updateUserId, "member@email.mail", "password", "nickname", userGradeUpdateRequest.getRole(), false);
        given(memberService.updateUserGrade(userGradeUpdateRequest)).willReturn(savedUserEntity);
        
        
        // when
        String resultMessage = memberBusiness.updateUserGrade(userGradeUpdateRequest);
        
        
        // then
        assertEquals(
                RESPONSE_USER_GRADE_UPDATE.formatted(savedUserEntity.getNickname(), savedUserEntity.getRole().getGrade()),
                resultMessage
        );
    }
    
    
    @DisplayName("로그아웃")
    @Test
    void logout() {
        Long requestUserId = 153L;
        JwtUserInfoDto jwtUserInfoDto = JwtUserInfoDto.builder()
                .id(requestUserId)
                .email("member@mail.ail")
                .nickname("nickname")
                .grade(RoleType.TYPE_MEMBER)
                .build();

        // -- 로그아웃
        String jwtHeader = "Bearer ";
        String jwt = jwtHeader + "jwt-json-web-token";

        TestSecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthentication(
                        jwtUserInfoDto,
                        jwt,
                        List.of(new SimpleGrantedAuthority(jwtUserInfoDto.getGrade()))));

        willDoNothing().given(jwtLogoutService).setLogoutToken(jwt, jwtUserInfoDto);

        
        // when
        String responseMessage = memberBusiness.logout();
        
        
        // then
        assertEquals(
                RESPONSE_SUCCESS_FORMAT_SIGN_OUT.formatted(jwtUserInfoDto.getNickname(), jwtUserInfoDto.getEmail()),
                responseMessage
        );
    }
    
    
    @DisplayName("회원 탈퇴")
    @Test
    void withdrawal() {
        String jwtHeader = "Bearer ";
        String jwt = jwtHeader + "jwt-json-web-token";
        long userId = 123L;

        UserEntity withdrawalUserEntity = getUserEntity(userId, "member@mail.ail", "password", "nickname", true);

        JwtUserInfoDto jwtUserInfoDto = JwtUserInfoDto.builder()
                .id(withdrawalUserEntity.getId())
                .email(withdrawalUserEntity.getEmail())
                .nickname(withdrawalUserEntity.getNickname())
                .grade(withdrawalUserEntity.getRole().getGrade())
                .build();

        TestSecurityContextHolder.getContext().setAuthentication(
                new JwtAuthentication(
                        jwtUserInfoDto,
                        jwt,
                        List.of(new SimpleGrantedAuthority(RoleType.TYPE_MEMBER))
                )
        );

        given(memberService.withdrawal(userId)).willReturn(withdrawalUserEntity);

        UserResponse userResponse = getUserResponse(withdrawalUserEntity);
        given(userConverter.toResponse(withdrawalUserEntity)).willReturn(userResponse);


        // when
        String responseMessage = memberBusiness.withdrawal();
        
        
        // then
        assertEquals(
                RESPONSE_USER_WITHDRAWAL.formatted(userResponse.getNickname(), userResponse.getEmail()),
                responseMessage
        );
    }
    
    
    
    
    private static UserResponse getUserResponse(UserEntity userEntity) {
        return UserResponse.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .role(userEntity.getRole())
                .build();
    }
    
    
    private static UserEntity getUserEntity(long userId, String email, String password, String nickname, String roleType, boolean withdrawal) {
        log.info("role type 변경 : {}", roleType);
        
        return getUserEntity(userId, email, password, nickname, withdrawal);
    }
    private static UserEntity getUserEntity(long userId, String email, String password, String nickname, boolean withdrawal) {
        return UserEntity.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .role(new RoleEntity())
                .withdrawal(withdrawal)
                .password(password)
                .createdAt(LocalDateTime.now().minusDays(10))
                .modifiedAt(LocalDateTime.now().minusDays(3))
                .build();
    }
}


package kim.zhyun.serveruser.service.impl;

import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serveruser.advice.MailAuthException;
import kim.zhyun.serveruser.advice.SignUpException;
import kim.zhyun.serveruser.data.EmailAuthCodeRequest;
import kim.zhyun.serveruser.data.EmailAuthDto;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.SignupRequest;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.EmailService;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.service.SessionUserService;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.data.type.RoleType.TYPE_MEMBER;

@RequiredArgsConstructor
@Service
public class SignUpServiceImpl implements SignUpService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
    private final NicknameReserveService nicknameReserveService;
    private final SessionUserService sessionUserService;
    private final EmailService emailService;
    private final JwtUserInfoRepository jwtUserInfoRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public boolean availableEmail(String email, String sessionId) {
        boolean existEmail = userRepository.existsByEmailIgnoreCase(email);
        
        if (existEmail)
            return false;
        
        // session user 저장소에 이메일 등록
        saveEmailToSessionUserStorage(email, sessionId);
        
        return true;
    }
    
    @Override
    public boolean availableNickname(String nickname, String sessionId) {
        boolean isMemberUsed = userRepository.existsByNicknameIgnoreCase(nickname);
        
        if (isMemberUsed)
            return false;
        
        NicknameDto newNicknameInfo = NicknameDto.builder()
                .nickname(nickname)
                .sessionId(sessionId).build();
        
        boolean isNotAvailable = !nicknameReserveService.availableNickname(newNicknameInfo);
        
        if (isNotAvailable)
            return false;
        
        // 닉네임 예약
        nicknameReserveService.saveNickname(newNicknameInfo);
        
        // session user 저장소에 닉네임 등록
        saveNicknameToSessionUserStorage(nickname, sessionId);
        
        return true;
    }
    
    @Override
    public void sendEmailAuthCode(String sessionId, EmailAuthCodeRequest request) {
        // 1. email 중복검사 확인
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        
        if (sessionUser.getEmail() == null || !sessionUser.getEmail().equals(request.getEmail()))
            throw new MailAuthException(EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK);
        
        // 2. 메일 발송
        emailService.sendEmailAuthCode(request.getEmail());
    }
    
    @Override
    public void verifyEmailAuthCode(String sessionId, String code) {
        String email = sessionUserService.findById(sessionId).getEmail();
        EmailAuthDto requestInfo = EmailAuthDto.builder()
                .email(email)
                .code(code).build();

        // 코드 불일치 case 1 : 만료된 경우
        if (!emailService.existEmail(requestInfo))
            throw new MailAuthException(EXCEPTION_VERIFY_EMAIL_AUTH_CODE_EXPIRED);
        
        // 코드 불일치 case 2 : 코드 불일치
        if (!emailService.existCode(requestInfo))
            throw new MailAuthException(EXCEPTION_VERIFY_FAIL_EMAIL_AUTH_CODE);
        
        // 인증 성공
        emailService.deleteAndUpdateSessionUserEmail(requestInfo, sessionId);
    }
    
    @Override
    public void saveMember(String sessionId, SignupRequest request) {
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        
        // 중복 확인 하지 않은 email
        if (sessionUser.getEmail() == null
                || (!sessionUser.getEmail().equals(request.getEmail()) || !sessionUser.isEmailVerification()))
            throw new SignUpException(EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK);
        
        // 중복 확인 하지 않은 nickname
        if (sessionUser.getNickname() == null
                || !sessionUser.getNickname().equals(request.getNickname()))
            throw new SignUpException(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK);
        
        Role role = roleRepository.findByGrade(TYPE_MEMBER);
        User saved = userRepository.save(User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(getPassword(request.getPassword()))
                .role(role).build());
        
        sessionUserService.deleteById(sessionId);
        
        jwtUserInfoUpdate(saved);
    }
    
    
    /**
     * redis user info 저장소 업데이트
     */
    private void jwtUserInfoUpdate(User user) {
        jwtUserInfoRepository.save(JwtUserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .grade("ROLE_" + user.getRole().getGrade())
                .build());
    }
    
    /**
     * session user 저장소에 이메일 등록
     */
    private void saveEmailToSessionUserStorage(String email, String sessionId) {
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        sessionUser.setEmail(email);
        sessionUser.setEmailVerification(false);
        sessionUserService.save(sessionUser);
    }
    
    /**
     * session user 저장소에 닉네임 등록
     */
    private void saveNicknameToSessionUserStorage(String nickname, String sessionId) {
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        sessionUser.setNickname(nickname);
        sessionUserService.save(sessionUser);
    }
    
    private String getPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
}

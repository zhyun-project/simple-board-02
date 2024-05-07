package kim.zhyun.serveruser.domain.signup.business;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameFindDto;
import kim.zhyun.serveruser.domain.signup.converter.EmailAuthConverter;
import kim.zhyun.serveruser.domain.signup.converter.NicknameReserveConverter;
import kim.zhyun.serveruser.domain.signup.converter.SessionUserConverter;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import kim.zhyun.serveruser.domain.signup.service.EmailService;
import kim.zhyun.serveruser.domain.signup.service.NicknameReserveService;
import kim.zhyun.serveruser.domain.signup.service.SignUpService;
import kim.zhyun.serveruser.utils.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.common.message.ResponseMessage.*;

@RequiredArgsConstructor
@Service
public class SignUpBusiness {
    
    private final SignUpService signUpService;
    private final SessionUserService sessionUserService;
    private final NicknameReserveService nicknameReserveService;
    private final EmailService emailService;
    
    private final SessionUserConverter sessionUserConverter;
    private final NicknameReserveConverter nicknameReserveConverter;
    private final EmailAuthConverter emailAuthConverter;
    private final UserConverter userConverter;
    
    private final EmailUtil emailUtil;
    
    
    
    /**
    이메일 중복확인
     */
    public String emailDuplicateCheck(String email, String sessionId) {
        boolean isAvailable = signUpService.isAvailableEmail(email);
        
        if (isAvailable) {
            // session user 저장소에 이메일 등록
            sessionUserService.updateEmail(sessionUserConverter.toEmailUpdateDto(
                    sessionId,
                    email,
                    false
            ));
        }
        
        return isAvailable
                ? RESPONSE_SIGN_UP_AVAILABLE_EMAIL
                : RESPONSE_SIGN_UP_UNAVAILABLE_EMAIL;
    }
    
    /**
     닉네임 중복확인
     */
    public String nicknameDuplicateCheck(String nickname, String sessionId) {
        boolean isUsed = signUpService.isUsedNickname(nickname);
        
        if (isUsed) {
            return RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME;
        }
        
        NicknameFindDto nicknameFindDto = nicknameReserveConverter.toFindDto(sessionId, nickname);
        boolean isNotAvailable = !nicknameReserveService.availableNickname(nicknameFindDto);
        
        if (isNotAvailable)
            return RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME;
        
        // 닉네임 예약
        nicknameReserveService.saveNickname(nicknameFindDto);
        
        // session user 저장소에 닉네임 등록
        sessionUserService.updateNickname(
                nicknameReserveConverter.toUpdateDto(sessionId, nickname)
        );
        
        return RESPONSE_SIGN_UP_AVAILABLE_NICKNAME;
    }
    
    /**
     * 이메일 인증코드 전송
     */
    public String sendEmailAuthCode(String sessionId, EmailAuthCodeRequest request) {
        // 1. email 중복검사 확인
        sessionUserService.emailDuplicateCheckWithThrow(sessionId, request.getEmail());
        
        // 2. 메일 발송
        String authCode = emailUtil.getAuthCode();
        String mailTitle = emailUtil.EMAIL_AUTH_TITLE_FORM_NEED_AUTH_CODE.formatted(authCode);
        String body = emailUtil.EMAIL_AUTH_BODY_FORM_NEED_AUTH_CODE.formatted(authCode);
        String fromEmail = "no-reply@simpleboard.02";
        String fromName = "SB02-ADMIN";
        
        try {
            MimeMessage message = emailUtil.createMessage(request.getEmail(), mailTitle, body, fromEmail, fromName);
            emailUtil.sendMail(message);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(EXCEPTION_MAIL_SEND_FAIL.formatted(e.getMessage()), e);
        }
        
        // 3. 메일 인증 정보 저장
        EmailAuthDto emailAuthDto = emailAuthConverter.toDto(request, authCode);
        emailService.saveEmailAuthInfo(emailAuthDto);
        
        return RESPONSE_SEND_EMAIL_AUTH_CODE;
    }
    
    /**
     * 이메일 인증코드 검증
     */
    public String verifyEmailAuthCode(String sessionId, String code) {
        String email = sessionUserService.findById(sessionId).getEmail();
        EmailAuthDto requestInfo = emailAuthConverter.toDto(email, code);
        
        // 코드 불일치 case 1 : 만료된 경우
        if (!emailService.existEmail(requestInfo))
            throw new ApiException(EXCEPTION_VERIFY_EMAIL_AUTH_CODE_EXPIRED);
        
        // 코드 불일치 case 2 : 코드 불일치
        if (!emailService.existCode(requestInfo))
            throw new ApiException(EXCEPTION_VERIFY_FAIL_EMAIL_AUTH_CODE);
        
        // 인증 성공
        emailService.deleteAndUpdateSessionUserEmail(sessionUserConverter.toEmailUpdateDto(
                sessionId,
                email,
                true
        ));
        
        return RESPONSE_VERIFY_EMAIL_AUTH_SUCCESS;
    }
    
    /**
     * 신규 회원 등록
     */
    public String saveMember(String sessionId, SignupRequest request) {
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        
        // 중복 확인 하지 않은 email
        if (sessionUser.getEmail() == null
                || (!sessionUser.getEmail().equals(request.getEmail())
                || !sessionUser.isEmailVerification()))
            throw new ApiException(EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK);
        
        // 중복 확인 하지 않은 nickname
        if (sessionUser.getNickname() == null
                || !sessionUser.getNickname().equals(request.getNickname()))
            throw new ApiException(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK);
        
        RoleEntity roleEntity = signUpService.getGrade(sessionUser.getEmail());
        
        // 신규 user 등록
        UserEntity savedUser = signUpService.saveUser(userConverter.toEntity(request, roleEntity));
        
        // redis user info 저장소 업데이트
        signUpService.jwtUserInfoUpdate(savedUser);
        
        // 임시 저장 정보 삭제
        sessionUserService.deleteById(sessionId);
        
        return String.format(RESPONSE_SUCCESS_FORMAT_SIGN_UP, request.getNickname());
    }
}

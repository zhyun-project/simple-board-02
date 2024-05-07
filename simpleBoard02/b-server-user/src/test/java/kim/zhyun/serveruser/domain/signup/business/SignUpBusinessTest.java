package kim.zhyun.serveruser.domain.signup.business;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import kim.zhyun.serveruser.domain.signup.converter.EmailAuthConverter;
import kim.zhyun.serveruser.domain.signup.service.EmailService;
import kim.zhyun.serveruser.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;

import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SEND_EMAIL_AUTH_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@Slf4j
@DisplayName("email service test")
@ExtendWith(MockitoExtension.class)
class SignUpBusinessTest {
    
    @InjectMocks SignUpBusiness signUpBusiness;
    @Mock EmailAuthConverter emailAuthConverter;
    @Mock SessionUserService sessionUserService;
    @Mock EmailService emailService;
    @Mock EmailUtil emailUtil;
    
    
    private final String SESSION_ID = "session-id";
    private final String TO_ADDRESS = "gimwlgus@gmail.com";
    
    
    
    @DisplayName("이메일 전송 과정 검증")
    @Test
    void send_email() throws MessagingException, UnsupportedEncodingException {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of(TO_ADDRESS);
        
        // -- 메일 중복 검사 확인
        willDoNothing().given(sessionUserService).emailDuplicateCheckWithThrow(SESSION_ID, emailAuthCodeRequest.getEmail());

        // -- 메일 발송
        String AUTH_CODE = "authcode";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        
        given(emailUtil.getAuthCode()).willReturn(AUTH_CODE);
        given(emailUtil.createMessage(eq(emailAuthCodeRequest.getEmail()), anyString(), anyString(), anyString(), anyString())).willReturn(mimeMessage);
        willDoNothing().given(emailUtil).sendMail(mimeMessage);
        
        // -- 메일 인증 정보 저장
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(emailAuthCodeRequest.getEmail())
                .code(AUTH_CODE)
                .build();
        
        given(emailAuthConverter.toDto(emailAuthCodeRequest, AUTH_CODE)).willReturn(emailAuthDto);
        willDoNothing().given(emailService).saveEmailAuthInfo(emailAuthDto);
        
        
        // when
        String responseMessage = signUpBusiness.sendEmailAuthCode(SESSION_ID, emailAuthCodeRequest);
        
        
        // then
        assertEquals(RESPONSE_SEND_EMAIL_AUTH_CODE, responseMessage);
        
        InOrder order = inOrder(sessionUserService, emailUtil, emailAuthConverter, emailService);
        order.verify(sessionUserService, times(1)).emailDuplicateCheckWithThrow(SESSION_ID, emailAuthCodeRequest.getEmail());
        order.verify(emailUtil, times(1)).getAuthCode();
        order.verify(emailUtil, times(1)).createMessage(eq(emailAuthCodeRequest.getEmail()), anyString(), anyString(), anyString(), anyString());
        order.verify(emailUtil, times(1)).sendMail(mimeMessage);
        order.verify(emailAuthConverter, times(1)).toDto(emailAuthCodeRequest, AUTH_CODE);
        order.verify(emailService, times(1)).saveEmailAuthInfo(emailAuthDto);
    }
    
}

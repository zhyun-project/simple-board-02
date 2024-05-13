package kim.zhyun.serveruser.domain.signup.controller;

import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.common.message.ExceptionMessage;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SUCCESS_FORMAT_SIGN_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SignApiControllerTest {
    
    @InjectMocks SignApiController signApiController;
    @Mock SignUpBusiness signUpBusiness;
    
    
    
    @DisplayName("회원가입 - 성공")
    @Test
    void signUp_success() {
        // given
        SignupRequest signupRequest = SignupRequest.of(
                "new@email.mail", "닉네임", "password"
        );
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = RESPONSE_SUCCESS_FORMAT_SIGN_UP;
        
        given(signUpBusiness.saveMember(request.getSession().getId(), signupRequest)).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = signApiController.signUp(request, signupRequest);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(), HttpStatusCode.valueOf(HttpStatus.SC_OK));
        assertEquals(responseEntity.getBody().getStatus(), true);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    
    @DisplayName("회원가입 - 실패: email 혹은 nickname 중복확인 안함")
    @ParameterizedTest
    @ValueSource(strings = {
            ExceptionMessage.EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK,
            ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK
    })
    void signUp_fail(String responseMessage) {
        // given
        SignupRequest signupRequest = SignupRequest.of(
                "new@email.mail", "닉네임", "password"
        );
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        given(signUpBusiness.saveMember(request.getSession().getId(), signupRequest)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> signApiController.signUp(request, signupRequest),
                responseMessage
        );
    }
    
}
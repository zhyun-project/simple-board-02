package kim.zhyun.serveruser.filter;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.serveruser.config.TestSecurityConfig;
import kim.zhyun.serveruser.config.security.SecurityAuthenticationManager;
import kim.zhyun.serveruser.filter.model.SignInRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(
        value = AuthenticationFilter.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtFilter.class,
                        SessionCheckFilter.class
                }
        )
)
class AuthenticationFilterTest {
    
    @MockBean SecurityAuthenticationManager securityAuthenticationManager;
    @MockBean JwtProvider jwtProvider;
    
    @Autowired MockMvc mvc;
    
    
    @BeforeEach
    void jwtProvider_init() {
        MockitoAnnotations.openMocks(this);
        jwtProvider.setJwtExpired(30L, "d");
    }
    
    
    @DisplayName("로그인 요청")
    @Test
    void attemptAuthentication_about_login() throws Exception {
        // given
        SignInRequest request = SignInRequest.of("user@email.mail", "password");

        UsernamePasswordAuthenticationToken doReturnAuthentication = new UsernamePasswordAuthenticationToken(
                JwtUserInfoDto.builder()
                        .id(1L)
                        .email(request.getEmail())
                        .nickname("nickname")
                        .build(), request.getPassword(), Set.of(new SimpleGrantedAuthority(RoleType.ROLE_MEMBER))
        );
        given(securityAuthenticationManager.authenticate(any(Authentication.class))).willReturn(doReturnAuthentication);
        
        
        // when
        mvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsBytes(request))
                )
                .andDo(MockMvcResultHandlers.print());
        
        
        // then
        then(securityAuthenticationManager).should(times(1)).authenticate(any(Authentication.class));
        then(jwtProvider).should(times(1)).tokenFrom(doReturnAuthentication);
    }
    
    
    @DisplayName("로그인 외")
    @ParameterizedTest
    @MethodSource
    void attemptAuthentication_others(RequestBuilder method) throws Exception {
        // given
        
        
        // when
        mvc.perform(method)
                .andDo(MockMvcResultHandlers.print());
        
        
        // then
        then(securityAuthenticationManager).should(times(0))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        then(jwtProvider).should(times(0))
                .tokenFrom(TestSecurityContextHolder.getContext().getAuthentication());
    }
    static Stream<RequestBuilder> attemptAuthentication_others() {
        return Stream.of(
                get("/check"),
                post("/check/auth"),
                get("/check/auth"),
                post("/sign-up"),
                
                get("/all"),
                get("/{id}", 123L),
                put("/{id}", 9237L),
                put("/role"),
                post("/logout"),
                post("/withdrawal")
        );
    }
    
}

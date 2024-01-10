package kim.zhyun.serverarticle.controller;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serverarticle.container.RedisTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_AUTHENTICATION;
import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_PERMISSION;
import static kim.zhyun.serverarticle.data.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.serverarticle.data.type.RoleType.TYPE_MEMBER;
import static kim.zhyun.serverarticle.util.TestSecurityUser.getJwtUserDto;
import static kim.zhyun.serverarticle.util.TestSecurityUser.setAuthentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class ArticleControllerTest {
    
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final JwtProvider jwtProvider;
    private final MockMvc mvc;

    public ArticleControllerTest(@Autowired MockMvc mvc,
                                 @Autowired JwtProvider jwtProvider,
                                 @Autowired JwtUserInfoRepository jwtUserInfoRepository) {
        this.mvc = mvc;
        this.jwtProvider = jwtProvider;
        this.jwtUserInfoRepository = jwtUserInfoRepository;
    }
    
    @DisplayName("전체 유저 조회 테스트")
    @Test
    @WithAnonymousUser
    void all_search_test() throws Exception {
        mvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andDo(print());
    }
    
    @DisplayName("특정 유저 조회 테스트")
    @Test
    @WithAnonymousUser
    void all_search_target_user() throws Exception {
        mvc.perform(get("/{userId}/articles", 1))
                .andExpect(status().isOk())
                .andDo(print());
    }
    
    @DisplayName("특정 유저 상세 조회 테스트")
    @Test
    @WithAnonymousUser
    void search_target_user_detail() throws Exception {
        mvc.perform(get("/{userId}/articles", 1))
                .andExpect(status().isOk())
                .andDo(print());
    }
    
    @DisplayName("게시글 등록 테스트")
    @Nested
    class SaveTest {
        
        @DisplayName("실패 : anonymous user")
        @Test
        @WithAnonymousUser
        void fail_anonymous() throws Exception {
            mvc.perform(post("/{userId}/articles", 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_AUTHENTICATION))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 남의 계정")
        @Test
        void fail_others() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            mvc.perform(post("/{userId}/articles", admin.getId() + 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        void success() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            mvc.perform(post("/{userId}/articles", admin.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("게시글 수정 테스트")
    @Nested
    class UpdateTest {
        
        @DisplayName("실패 : anonymous user")
        @Test
        @WithAnonymousUser
        void fail_anonymous() throws Exception {
            mvc.perform(put("/{userId}/articles/{articleId}", 1, 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_AUTHENTICATION))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 남의 계정")
        @Test
        void fail_others() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            mvc.perform(put("/{userId}/articles/{articleId}", admin.getId() + 1, 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        void success() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            mvc.perform(put("/{userId}/articles/{articleId}", admin.getId(), 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("게시글 삭제 테스트")
    @Nested
    class DeleteTest {
        
        @DisplayName("실패 : anonymous user")
        @Test
        @WithAnonymousUser
        void fail_anonymous() throws Exception {
            mvc.perform(delete("/{userId}/articles/{articleId}", 1, 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_AUTHENTICATION))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 남의 계정")
        @Test
        void fail_others() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            mvc.perform(delete("/{userId}/articles/{articleId}", admin.getId() + 1, 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        void success() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            mvc.perform(delete("/{userId}/articles/{articleId}", admin.getId(), 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andDo(print());
        }
    }
    
    
    @BeforeEach void init() {
        initRedisUserInfo(2, "gimwlgus@gmail.com", "얼거스", TYPE_ADMIN);
        initRedisUserInfo(3, "gimwlgus@daum.net", "zhyun", TYPE_MEMBER);
        initRedisUserInfo(5, "gimwlgus@kakao.com", "얼구스", TYPE_MEMBER);
    }
    @AfterEach void clean() {
        initRedisUserInfo(2, "gimwlgus@gmail.com", "얼거스", TYPE_ADMIN);
        initRedisUserInfo(3, "gimwlgus@daum.net", "zhyun", TYPE_MEMBER);
        initRedisUserInfo(5, "gimwlgus@kakao.com", "얼구스", TYPE_MEMBER);
    }
    
    private void initRedisUserInfo(long id, String email, String nickname, String grade) {
        jwtUserInfoRepository.save(JwtUserInfo.builder()
                        .id(id)
                        .email(email)
                        .nickname(nickname)
                        .grade("ROLE_" + grade).build());
    }
}
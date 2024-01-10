package kim.zhyun.serverarticle.controller;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serverarticle.container.RedisTestContainer;
import kim.zhyun.serverarticle.data.ArticleSaveRequest;
import kim.zhyun.serverarticle.data.ArticleUpdateRequest;
import kim.zhyun.serverarticle.data.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.data.entity.Article;
import kim.zhyun.serverarticle.respository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_AUTHENTICATION;
import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_PERMISSION;
import static kim.zhyun.serverarticle.data.message.ResponseMessage.RESPONSE_ARTICLE_INSERT;
import static kim.zhyun.serverarticle.data.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.serverarticle.data.type.RoleType.TYPE_MEMBER;
import static kim.zhyun.serverarticle.util.TestSecurityUser.getJwtUserDto;
import static kim.zhyun.serverarticle.util.TestSecurityUser.setAuthentication;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class ArticleControllerTest {
    
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final JwtProvider jwtProvider;
    private final MockMvc mvc;

    public ArticleControllerTest(@Autowired MockMvc mvc,
                                 @Autowired JwtProvider jwtProvider,
                                 @Autowired JwtUserInfoRepository jwtUserInfoRepository,
                                 @Autowired RedisTemplate<String, String> redisTemplate,
                                 @Autowired ArticleRepository articleRepository) {
        this.mvc = mvc;
        this.jwtProvider = jwtProvider;
        this.jwtUserInfoRepository = jwtUserInfoRepository;
        this.redisTemplate = redisTemplate;
        this.articleRepository = articleRepository;
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
        mvc.perform(get("/{userId}/articles", 2))
                .andExpect(status().isOk())
                .andDo(print());
    }
    
    @DisplayName("특정 유저 상세 조회 테스트")
    @Test
    @WithAnonymousUser
    void search_target_user_detail() throws Exception {
        // given
        setAuthentication(jwtProvider, "admin");
        JwtUserDto admin = getJwtUserDto();
        
        ArticleSaveRequest request = ArticleSaveRequest.builder()
                .userId(admin.getId())
                .title("admin 제목 1")
                .content("admin 내용 1").build();
        
        mvc.perform(post("/{userId}/articles", admin.getId())
                        .contentType(APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)));
        
        TestSecurityContextHolder.clearContext();
        
        // when
        mvc.perform(get("/{userId}/articles/{articleId}", admin.getId(), 1))
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
            mvc.perform(post("/{userId}/articles", 2))
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
            
            ArticleSaveRequest request = ArticleSaveRequest.builder()
                    .userId(admin.getId())
                    .title("admin 제목 1")
                    .content("admin 내용 1").build();
            
            mvc.perform(post("/{userId}/articles", admin.getId() + 1)
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공 - admin 1건 저장")
        @Test
        void success() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            ArticleSaveRequest request = ArticleSaveRequest.builder()
                    .userId(admin.getId())
                    .title("admin 제목 1")
                    .content("admin 내용 1").build();
            
            mvc.perform(post("/{userId}/articles", admin.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_INSERT))
                    .andDo(print());
        }
        
        
        @DisplayName("성공 - admin , member1 각각 1건 저장")
        @Test
        void success_and_all_article_id_is_1() throws Exception {
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            ArticleSaveRequest request = ArticleSaveRequest.builder()
                    .userId(admin.getId())
                    .title("admin 제목 1")
                    .content("admin 내용 1").build();
            
            mvc.perform(post("/{userId}/articles", admin.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());
            
            
            setAuthentication(jwtProvider, "member1");
            JwtUserDto member1 = getJwtUserDto();
            
            request = ArticleSaveRequest.builder()
                    .userId(member1.getId())
                    .title("member1 제목 1")
                    .content("member1 내용 1").build();
            
            mvc.perform(post("/{userId}/articles", member1.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isCreated())
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
            
            ArticleSaveRequest saveRequest = ArticleSaveRequest.builder()
                    .userId(admin.getId())
                    .title("admin 제목 1")
                    .content("admin 내용 1").build();
            
            mvc.perform(put("/{userId}/articles/{articleId}", admin.getId() + 1, 1)
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(saveRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        void success() throws Exception {
            // given
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            
            ArticleSaveRequest saveRequest = ArticleSaveRequest.builder()
                    .userId(admin.getId())
                    .title("admin 제목 1")
                    .content("admin 내용 1").build();
            
            mvc.perform(post("/{userId}/articles", admin.getId())
                    .contentType(APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(saveRequest)));
            
            // when
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(admin.getId()).get(0);
            ArticleUpdateRequest updateRequest = ArticleUpdateRequest.builder()
                    .id(article.getId())
                    .userId(admin.getId())
                    .articleId(article.getArticleId())
                    .title("admin 제목 업데이트 1")
                    .content("admin 내용 업데이트 1").build();

            mvc.perform(put("/{userId}/articles/{articleId}", admin.getId(), article.getArticleId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(updateRequest)));
            
            // then
            Article articleUpdated = articleRepository.findByUserIdAndArticleId(admin.getId(), article.getArticleId()).get();
            
            assertThat(articleUpdated.getCreatedAt()).isEqualTo(article.getCreatedAt());
            
            assertThat(articleUpdated.getModifiedAt()).isNotEqualTo(article.getModifiedAt());
            assertThat(articleUpdated.getTitle()).isNotEqualTo(article.getTitle());
            assertThat(articleUpdated.getContent()).isNotEqualTo(article.getContent());
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
            
            ArticlesDeleteRequest deleteRequest = ArticlesDeleteRequest.builder()
                    .userId(admin.getId() + 1)
                    .articleIds(List.of(2L)).build(); // 10개 - 중복제외
            
            mvc.perform(delete("/{userId}/articles", admin.getId() + 1)
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(deleteRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        void success() throws Exception {
            // given
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            saveArticle100(admin);
            
            setAuthentication(jwtProvider, "member1");
            JwtUserDto member1 = getJwtUserDto();
            saveArticle100(member1);
            
            setAuthentication(jwtProvider, "member2");
            JwtUserDto member2 = getJwtUserDto();
            saveArticle100(member2);
            
            setAuthentication(jwtProvider, "member1");
            
            // when
            ArticlesDeleteRequest deleteRequest = ArticlesDeleteRequest.builder()
                    .userId(member1.getId())
                    .articleIds(List.of(2L,4L,8L,7L,6L,9L,4L,2L,13L,55L,78L,32L)).build(); // 10개 - 중복제외
            
            mvc.perform(delete("/{userId}/articles", member1.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.status").value(true))
                    .andDo(print());
            
            assertThat(articleRepository.findAll().size()).isEqualTo(300 - 10);
        }
        
        @DisplayName("성공 - 탈퇴자 삭제")
        @Test
        void success_by_withdrawal() throws Exception {
            // given
            setAuthentication(jwtProvider, "admin");
            JwtUserDto admin = getJwtUserDto();
            saveArticle100(admin);
            
            setAuthentication(jwtProvider, "member1");
            JwtUserDto member1 = getJwtUserDto();
            saveArticle100(member1);
            
            setAuthentication(jwtProvider, "member2");
            JwtUserDto member2 = getJwtUserDto();
            saveArticle100(member2);
            
            setAuthentication(jwtProvider, "member1");
            
            // when
            ArticlesDeleteRequest deleteRequest = ArticlesDeleteRequest.builder()
                    .userId(member1.getId()).build(); // 10개 - 중복제외
            
            mvc.perform(delete("/withdrawal/{userId}/articles", member1.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.status").value(true))
                    .andDo(print());
            
            assertThat(articleRepository.findAll().size()).isEqualTo(300 - 100);
        }
        
        private void saveArticle100(JwtUserDto user) throws Exception {
            
            for (int i = 1; i <= 100; i++) {
                ArticleSaveRequest saveRequest = ArticleSaveRequest.builder()
                        .userId(user.getId())
                        .title(user.getNickname() + " 제목 " + i)
                        .content(user.getNickname() + " 내용 " + i).build();
                
                mvc.perform(post("/{userId}/articles", user.getId())
                        .contentType(APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(saveRequest)));
            }
        }
        
    }
    
    
    @BeforeEach void init() {
        initRedisUserInfo(2, "gimwlgus@gmail.com", "얼거스", TYPE_ADMIN);
        initRedisUserInfo(3, "gimwlgus@daum.net", "zhyun", TYPE_MEMBER);
        initRedisUserInfo(5, "gimwlgus@kakao.com", "얼구스", TYPE_MEMBER);
    }
    
    @BeforeEach
    @AfterEach
    void clean() {
        articleRepository.deleteAllInBatch();
        redisTemplate.keys("*").stream()
                .filter(key -> key.startsWith("ARTICLE_ID:"))
                .map(redisTemplate::delete).close();
    }
    
    private void initRedisUserInfo(long id, String email, String nickname, String grade) {
        jwtUserInfoRepository.save(JwtUserInfo.builder()
                        .id(id)
                        .email(email)
                        .nickname(nickname)
                        .grade("ROLE_" + grade).build());
    }
}
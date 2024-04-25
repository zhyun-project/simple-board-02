package kim.zhyun.serverarticle.controller;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serverarticle.config.SecurityConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static kim.zhyun.serverarticle.data.message.ExceptionMessage.*;
import static kim.zhyun.serverarticle.data.message.ResponseMessage.*;
import static kim.zhyun.serverarticle.data.type.RoleType.*;
import static kim.zhyun.serverarticle.util.TestSecurityUser.getJwtUserDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(RedisTestContainer.class)
@Import(SecurityConfig.class)
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
    
    @DisplayName("게시글 등록")
    @Nested
    class SaveTest {
        
        @DisplayName("실패 : anonymous user")
        @Test
        void fail_anonymous() throws Exception {
            // given
            
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            clearContext();
            
            getPerformSave(member1)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_AUTHENTICATION))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 남의 계정")
        @Test
        void fail_others() throws Exception {
            JwtUserDto target = getJwtUserDto(jwtProvider, "member2");
            JwtUserDto me = getJwtUserDto(jwtProvider, "member1");
            
            getPerformSave(target)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 탈퇴한 계정")
        @Test
        void fail_withdrawal() throws Exception {
            JwtUserDto me = getJwtUserDto(jwtProvider, "member1");
            updateUserRoleTo(me, ROLE_WITHDRAWAL);
            
            getPerformSave(me)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("성공 : 내 계정")
        @Test
        void success() throws Exception {
            JwtUserDto me = getJwtUserDto(jwtProvider, "member1");
            
            getPerformSave(me)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_INSERT))
                    .andDo(print());
        }
        
        @DisplayName("성공 : article id 검증")
        @Test
        void success_so_many() throws Exception {
            // when
            JwtUserDto mem1 = getJwtUserDto(jwtProvider, "member1");
            initArticleId(mem1.getId());
            
            JwtUserDto mem2 = getJwtUserDto(jwtProvider, "member2");
            initArticleId(mem2.getId());
            
            JwtUserDto admin = getJwtUserDto(jwtProvider, "admin");
            initArticleId(admin.getId());
            
            
            // then
            mem1 = getJwtUserDto(jwtProvider, "member1");
            getPerformSave(mem1).andExpect(jsonPath("$.result.articleId").value(1));
            getPerformSave(mem1).andExpect(jsonPath("$.result.articleId").value(2));
            getPerformSave(mem1).andExpect(jsonPath("$.result.articleId").value(3));
            
            mem2 = getJwtUserDto(jwtProvider, "member2");
            getPerformSave(mem2).andExpect(jsonPath("$.result.articleId").value(1));
            getPerformSave(mem2).andExpect(jsonPath("$.result.articleId").value(2));
            
            admin = getJwtUserDto(jwtProvider, "admin");
            getPerformSave(admin).andExpect(jsonPath("$.result.articleId").value(1));
            getPerformSave(admin).andExpect(jsonPath("$.result.articleId").value(2));
            getPerformSave(admin).andExpect(jsonPath("$.result.articleId").value(3));
            
            mem2 = getJwtUserDto(jwtProvider, "member2");
            getPerformSave(mem2).andExpect(jsonPath("$.result.articleId").value(3));
        }
        
    }
    
    @DisplayName("게시글 없음")
    @Nested
    class EmptyTest {
        
        @DisplayName("전체 게시글 조회")
        @Test
        void find_all() throws Exception {
            // when - then
            getPerformFindAll()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ALL))
                    .andExpect(jsonPath("$.result").isEmpty())
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 조회")
        @Test
        void find_all_by_user() throws Exception {
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            clearContext();
            
            getPerformFindByUserId(member1.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ALL_BY_USER.formatted(member1.getNickname())))
                    .andExpect(jsonPath("$.result").isEmpty())
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 상세 조회")
        @Test
        void find_by_user_article_id() throws Exception {
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            clearContext();
            
            getPerformFindByUserArticleId(member1.getId(), 1)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_ARTICLE_NOT_FOUND))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 수정")
        @Test
        void update_by_user_article_id() throws Exception {
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            
            getPerformUpdate(member1.getId(), 1, Article.builder()
                    .id(1L).articleId(1)
                    .title("1").content("1").build())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_ARTICLE_NOT_FOUND))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 삭제")
        @Test
        void delete_by_user_article_id() throws Exception {
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            
            getPerformDelete(member1.getId(), Set.of(1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_DELETE))
                    .andDo(print());
        }
        
    }
    
    
    @DisplayName("게시글 있음")
    @Nested
    class ArticleExistTrueTest {
        
        @DisplayName("전체 게시글 조회")
        @Test
        void find_all() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            getPerformFindAll()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ALL))
                    .andExpect(jsonPath("$.result.length()").value(3))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 조회")
        @Test
        void find_all_by_user() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            clearContext();
            
            getPerformFindByUserId(member1.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ALL_BY_USER.formatted(member1.getNickname())))
                    .andExpect(jsonPath("$.result.length()").value(1))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 상세 조회")
        @Test
        void find_by_user_article_id() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member1.getId()).get(0);
            clearContext();
            
            getPerformFindByUserArticleId(member1.getId(), article.getArticleId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ONE_BY_USER.formatted(member1.getNickname(), article.getArticleId())))
                    .andExpect(jsonPath("$.result.id").value(article.getId()))
                    .andExpect(jsonPath("$.result.articleId").value(article.getArticleId()))
                    .andExpect(jsonPath("$.result.title").value(article.getTitle()))
                    .andExpect(jsonPath("$.result.content").value(article.getContent()))
                    .andExpect(jsonPath("$.result.createdAt").value(article.getCreatedAt().toString()))
                    .andExpect(jsonPath("$.result.modifiedAt").value(article.getModifiedAt().toString()))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 수정 - 실패 : 다른사람것")
        @Test
        void update_fail_by_user_article_id() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            JwtUserDto member2 = getJwtUserDto(jwtProvider, "member2");
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member2.getId()).get(0);
            
            getPerformUpdate(member2.getId(), article.getArticleId(), article)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 수정 - 성공")
        @Test
        void update_by_user_article_id() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member1.getId()).get(0);
            
            getPerformUpdate(member1.getId(), article.getArticleId(), article)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_UPDATE))
                    .andDo(print());
            
            Article articleUpdated = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member1.getId()).get(0);
            assertThat(articleUpdated.getCreatedAt()).isEqualTo(article.getCreatedAt());
            assertThat(articleUpdated.getModifiedAt()).isNotEqualTo(article.getModifiedAt());
        }
        
        @DisplayName("유저 게시글 삭제")
        @Test
        void delete_by_user_article_id() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member1.getId()).get(0);
            
            getPerformDelete(member1.getId(), Set.of(article.getArticleId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_DELETE))
                    .andDo(print());
            
            Optional<Article> container = articleRepository.findByUserIdAndArticleId(member1.getId(), article.getArticleId());
            assertThat(container).isEmpty();
            
        }
        
        @DisplayName("유저 게시글 삭제 : fail - 다른사람것")
        @Test
        void delete_fail_by_user_article_id() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            // when - then
            JwtUserDto member2 = getJwtUserDto(jwtProvider, "member2");
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member2.getId()).get(0);
            
            getPerformDelete(member2.getId(), Set.of(article.getArticleId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
    }
    
    
    
    @DisplayName("탈퇴자의 게시글 (삭제 전)")
    @Nested
    class ArticleWithdrawalNotDeletedTest {
        
        @DisplayName("전체 게시글 조회")
        @Test
        void find_all() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            updateUserRoleTo(member1, ROLE_WITHDRAWAL);
            
            // when - then
            getPerformFindAll()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ALL))
                    .andExpect(jsonPath("$.result.length()").value(3))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 조회")
        @Test
        void find_all_by_user() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            updateUserRoleTo(member1, ROLE_WITHDRAWAL);
            clearContext();
            
            // when - then
            getPerformFindByUserId(member1.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ALL_BY_USER.formatted(member1.getNickname())))
                    .andExpect(jsonPath("$.result.length()").value(1))
                    .andDo(print());
        }
        
        @DisplayName("유저 게시글 상세 조회")
        @Test
        void find_by_user_article_id() throws Exception {
            // given
            makeArticleData("member1");
            makeArticleData("member2");
            makeArticleData("admin");
            
            JwtUserDto member1 = getJwtUserDto(jwtProvider, "member1");
            updateUserRoleTo(member1, ROLE_WITHDRAWAL);
            clearContext();
            
            // when - then
            Article article = articleRepository.findAllByUserIdOrderByCreatedAtDesc(member1.getId()).get(0);
            
            getPerformFindByUserArticleId(member1.getId(), article.getArticleId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_ARTICLE_FIND_ONE_BY_USER.formatted(member1.getNickname(), article.getArticleId())))
                    .andExpect(jsonPath("$.result.id").value(article.getId()))
                    .andExpect(jsonPath("$.result.articleId").value(article.getArticleId()))
                    .andExpect(jsonPath("$.result.title").value(article.getTitle()))
                    .andExpect(jsonPath("$.result.content").value(article.getContent()))
                    .andExpect(jsonPath("$.result.createdAt").value(article.getCreatedAt().toString()))
                    .andExpect(jsonPath("$.result.modifiedAt").value(article.getModifiedAt().toString()))
                    .andDo(print());
        }
        
    }
    
    
    
    @DisplayName("탈퇴자의 게시글 (삭제 후)")
    @Nested
    class ArticleWithdrawalDeletedTest {
        
        @DisplayName("전체 게시글 조회")
        @Test
        void find_all() throws Exception {
            mvc.perform(post("/withdrawal")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(Set.of(1L))))
                    .andDo(print());
            
            mvc.perform(get("/{userId}/all", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.result.length()").value(0))
                    .andDo(print());
        }
        
        
    }
    
    
    
    
    /**
     * article id 초기화 - `@BeforeEach` , `@AfterEach` 로 삭제를 해준다고 하는데도 삭제가 안되는 경우가 있어서 생성
     */
    private void initArticleId(long userId) {
        redisTemplate.opsForValue().set("ARTICLE_ID:%d".formatted(userId), "0");
    }
    
    /**
     * 계정 권한 수정
     */
    private void updateUserRoleTo(JwtUserDto user, String role_type) {
        JwtUserInfo jwtUserInfo = jwtUserInfoRepository.findById(user.getId()).get();
        jwtUserInfo.setGrade(role_type);
        jwtUserInfoRepository.save(jwtUserInfo);
        
        SecurityContext securityContext = TestSecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(), authentication.getCredentials(), Set.of(new SimpleGrantedAuthority(role_type))
        ));
        TestSecurityContextHolder.setContext(securityContext);
    }
    
    /**
     * 게시글 수정 perform
     */
    private ResultActions getPerformUpdate(long userId, long articleId, Article article) throws Exception {
        
        return mvc.perform(put("/{userId}/{articleId}", userId, articleId)
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ArticleUpdateRequest.builder()
                        .id(article.getId())
                        .userId(userId)
                        .articleId(article.getArticleId())
                        .title("%s 업데이트".formatted(article.getTitle()))
                        .content("%s 업데이트".formatted(article.getContent())).build())));
    }
    
    /**
     * 게시글 삭제 perform
     */
    private ResultActions getPerformDelete(long userId, Collection<Long> articleIds) throws Exception {
        
        return mvc.perform(post("/{userId}/delete", userId)
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ArticlesDeleteRequest.builder()
                        .userId(userId)
                        .articleIds(articleIds).build())));
    }
    
    /**
     * 게시글 저장 perform
     */
    private ResultActions getPerformSave(JwtUserDto user) throws Exception {
        
        return mvc.perform(post("/{userId}", user.getId())
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ArticleSaveRequest.builder()
                        .userId(user.getId())
                        .title("%s 제목 %d%d".formatted(user.getNickname(), now().getMinute(), now().getSecond()))
                        .content("%s 내용 %d%d".formatted(user.getNickname(), now().getMinute(), now().getSecond())).build())));
    }
    
    /**
     * 게시글 조회 perform
     */
    private ResultActions getPerformFindAll() throws Exception {
        return mvc.perform(get("/all"));
    }
    private ResultActions getPerformFindByUserId(long userId) throws Exception {
        return mvc.perform(get("/{userId}/all", userId));
    }
    private ResultActions getPerformFindByUserArticleId(long userId, long articleId) throws Exception {
        return mvc.perform(get("/{userId}/{articleId}", userId, articleId));
    }
    
    
    /**
     * 게시글 dummy data 생성 - @Return JwtUserDto
     */
    private JwtUserDto makeArticleData(String memberType) throws Exception {
        JwtUserDto user = getJwtUserDto(jwtProvider, memberType);
        
        getPerformSave(user)
                .andExpect(status().isCreated());
        
        clearContext();
        return user;
    }
    
    /**
     * authentication 삭제
     */
    private static void clearContext() {
        TestSecurityContextHolder.clearContext();
    }
    
    /**
     * redis - rdb 계정 정보 저장
     */
    @BeforeEach void init() {
        initRedisUserInfo(2, "gimwlgus@gmail.com", "얼거스", TYPE_ADMIN);
        initRedisUserInfo(3, "gimwlgus@daum.net", "zhyun", TYPE_MEMBER);
        initRedisUserInfo(1, "gimwlgus@kakao.com", "얼구스", TYPE_MEMBER);
    }
    private void initRedisUserInfo(long id, String email, String nickname, String grade) {
        jwtUserInfoRepository.save(JwtUserInfo.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .grade("ROLE_" + grade).build());
    }
    
    /**
     * rdb - 게시글 전체 삭제 , redis - article_id:OO 전체 삭제
     */
    @AfterEach
    void clean() {
        log.info("🧹 init [article all, ARTICLE_ID:] ------------------------------------------------------------------------------------------------");
        articleRepository.deleteAllInBatch();
        redisTemplate.keys("*").stream()
                .filter(key -> key.startsWith("ARTICLE"))
                .map(redisTemplate::delete).close();
    }
    
}
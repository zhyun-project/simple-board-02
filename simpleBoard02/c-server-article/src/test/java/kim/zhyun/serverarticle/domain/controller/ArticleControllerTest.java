package kim.zhyun.serverarticle.domain.controller;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.exception.message.CommonExceptionMessage;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serverarticle.common.message.ExceptionMessage;
import kim.zhyun.serverarticle.common.message.ResponseMessage;
import kim.zhyun.serverarticle.config.TestSecurityConfig;
import kim.zhyun.serverarticle.domain.business.ArticleBusiness;
import kim.zhyun.serverarticle.domain.controller.model.ArticleResponse;
import kim.zhyun.serverarticle.domain.controller.model.ArticleSaveRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticleUpdateRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticlesDeleteRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("article controller test")
@Import(TestSecurityConfig.class)
@WebMvcTest(
        controllers = ArticleController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class ArticleControllerTest {
    
    @MockBean ArticleBusiness articleBusiness;

    @Autowired MockMvc mvc;
    ObjectMapper objectMapper = new ObjectMapper();
    
    
    @DisplayName("전체 게시글 조회 - 게시글 없음")
    @Test
    void findAll_zero() throws Exception {
        // given
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_FIND_ALL;
        
        
        // when - then
        mvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result.length()").value(0))
                .andDo(print());
    }
    
    @DisplayName("전체 게시글 조회 - 게시글 있음")
    @Test
    void findAll_many() throws Exception {
        // given
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_FIND_ALL;
        List<ArticleResponse> articleResponseList = getArticleResponseList_30();
        
        given(articleBusiness.findAll()).willReturn(articleResponseList);
        
        
        // when - then
        mvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result.length()").value(30))
                .andDo(print());
    }
    
    
    @DisplayName("특정 유저 전체 게시글 조회")
    @ParameterizedTest
    @ValueSource(longs = {
            1L, 2L, 3L
    })
    void findAllByUser(long targetUserId) throws Exception {
        // given
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_FIND_ALL_BY_USER.formatted(targetUserId);
        
        List<ArticleResponse> targetUserArticleResponseList = getArticleResponseList_30().stream()
                .filter(articleResponse -> articleResponse.getUser().getId() == targetUserId)
                .toList();
        
        given(articleBusiness.findAllByUser(targetUserId)).willReturn(targetUserArticleResponseList);
        
        
        // when - then
        mvc.perform(get("/all/user/{userId}", targetUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result.length()").value(10))
                .andExpect(jsonPath("$.result[0].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[1].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[2].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[3].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[4].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[5].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[6].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[7].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[8].user.id").value(targetUserId))
                .andExpect(jsonPath("$.result[9].user.id").value(targetUserId))
                .andDo(print());
    }
    
    
    @DisplayName("유저 게시글 상세 조회 - 성공")
    @Test
    void findByArticleId_success() throws Exception {
        // given
        long targetUserId = 3L;
        long targetArticleId = 7L;
        
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_FIND_ONE_BY_USER.formatted(targetUserId, targetArticleId);
        
        ArticleResponse targetUserArticleResponse = getArticleResponseList_30().stream()
                .filter(articleResponse -> (articleResponse.getUser().getId() == targetUserId) && articleResponse.getArticleId() == targetArticleId)
                .findFirst().get();
        
        given(articleBusiness.findByArticleId(targetUserId, targetArticleId)).willReturn(targetUserArticleResponse);
        
        
        // when - then
        mvc.perform(get("/{articleId}/user/{userId}", targetArticleId, targetUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result.id").value(targetUserArticleResponse.getId()))
                .andExpect(jsonPath("$.result.articleId").value(targetUserArticleResponse.getArticleId()))
                .andExpect(jsonPath("$.result.user.id").value(targetUserArticleResponse.getUser().getId()))
                .andDo(print());
    }
    
    @DisplayName("유저 게시글 상세 조회 - 실패: 등록되지 않은 articleId 또는 userId")
    @Test
    void findByArticleId_fail() throws Exception {
        // given
        long targetUserId = 3L;
        long targetArticleId = 100L;
        
        String responseMessage = ExceptionMessage.EXCEPTION_ARTICLE_NOT_FOUND;
        
        given(articleBusiness.findByArticleId(targetUserId, targetArticleId)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        mvc.perform(get("/{articleId}/user/{userId}", targetArticleId, targetUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("게시글 등록 - 성공")
    @ParameterizedTest
    @MethodSource
    void save_success(String roleType, String title, String content) throws Exception {
        // given
        long loginUserId = 1L;
        
        setSecurityContext(loginUserId, roleType);
        
        ArticleSaveRequest articleSaveRequest = ArticleSaveRequest.builder()
                .title(title)
                .content(content)
                .build();
        
        ArticleResponse doArticleResponse = getArticleResponse(
                234L, 53L, loginUserId,
                articleSaveRequest.getTitle(), articleSaveRequest.getContent()
        );
        given(articleBusiness.save(articleSaveRequest, loginUserId)).willReturn(doArticleResponse);
        
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_INSERT;
        
        
        // when - then
        mvc.perform(
                        post("/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleSaveRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result.id").value(doArticleResponse.getId()))
                .andExpect(jsonPath("$.result.articleId").value(doArticleResponse.getArticleId()))
                .andExpect(jsonPath("$.result.user.id").value(doArticleResponse.getUser().getId()))
                .andExpect(jsonPath("$.result.title").value(doArticleResponse.getTitle()))
                .andDo(print());
    }
    static Stream<Arguments> save_success() {
        return Stream.of(
                Arguments.of(
                        RoleType.ROLE_ADMIN, "제목", "내용"
                ),
                Arguments.of(
                        RoleType.ROLE_ADMIN,
                        "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십",
                        """
                                
                                
                                내용
                                
                                
                                
                                
                                """
                )
        );
    }
    
    @DisplayName("게시글 등록 - 실패- 제목 입력 형식 오류")
    @ParameterizedTest
    @CsvSource({
            "'ROLE_MEMBER', '일이삼사오육칠팔구십 일이삼사오육칠팔구십일이삼사오육칠팔구십'", // 31자
            "'ROLE_MEMBER', '일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십a'", // 31자
            "'ROLE_MEMBER', '                              '", // 30자
            "'ROLE_MEMBER', '                               '", // 31자
            "'ROLE_MEMBER', ''",
            "'ROLE_MEMBER', ' '",
    })
    void save_success_false(String roleType, String title) throws Exception {
        // given
        long loginUserId = 1L;
        
        setSecurityContext(loginUserId, roleType);
        
        ArticleSaveRequest articleSaveRequest = ArticleSaveRequest.builder()
                .title(title)
                .content("내용")
                .build();
        
        ArticleResponse doArticleResponse = getArticleResponse(
                234L, 53L, loginUserId,
                articleSaveRequest.getTitle(), articleSaveRequest.getContent()
        );
        given(articleBusiness.save(articleSaveRequest, loginUserId)).willReturn(doArticleResponse);
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_VALID_FORMAT;
        String responseDetailMessage = ExceptionMessage.EXCEPTION_TITLE_FORMAT;
        
        
        // when - then
        mvc.perform(
                        post("/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleSaveRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result[0].field").value("title"))
                .andExpect(jsonPath("$.result[0].message").value(responseDetailMessage))
                .andDo(print());
    }
    
    @DisplayName("게시글 등록 - 실패- 내용 입력 형식 오류")
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "                                                                                ",
            """
                    
                    """,
            """
                    
                    """
    })
    @NullAndEmptySource
    void save_success_false_with_content(String content) throws Exception {
        // given
        long loginUserId = 1L;
        
        setSecurityContext(loginUserId, RoleType.ROLE_MEMBER);
        
        ArticleSaveRequest articleSaveRequest = ArticleSaveRequest.builder()
                .title("제목")
                .content(content)
                .build();
        
        ArticleResponse doArticleResponse = getArticleResponse(
                234L, 53L, loginUserId,
                articleSaveRequest.getTitle(), articleSaveRequest.getContent()
        );
        given(articleBusiness.save(articleSaveRequest, loginUserId)).willReturn(doArticleResponse);
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_VALID_FORMAT;
        String responseDetailMessage = ExceptionMessage.EXCEPTION_CONTENT_IS_NULL;
        
        
        // when - then
        mvc.perform(
                        post("/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleSaveRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result[0].field").value("content"))
                .andExpect(jsonPath("$.result[0].message").value(responseDetailMessage))
                .andDo(print());
    }
    
    @DisplayName("게시글 실패 - 권한 없음: 탈퇴자, 비회원")
    @Nested
    class SaveFailCase {
        
        @Test
        void save_fail() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            mvc.perform(post("/save"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @Test
        @WithAnonymousUser
        void save_fail_with_anonymous() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            mvc.perform(post("/save"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void save_fail_with_withdrawal() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
            
            mvc.perform(post("/save"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("게시글 수정 - 성공")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_MEMBER,
            RoleType.ROLE_ADMIN
    })
    void updateByArticleId_success(String roleType) throws Exception {
        // given
        long loginUserId = 1L;
        
        setSecurityContext(loginUserId, roleType);
        
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .id(3L)
                .articleId(234L)
                .userId(loginUserId)
                .title("제목")
                .content("내용")
                .build();
        
        ArticleResponse doArticleResponse = getArticleResponse(
                articleUpdateRequest.getId(), articleUpdateRequest.getArticleId(), articleUpdateRequest.getUserId(),
                articleUpdateRequest.getTitle(), articleUpdateRequest.getContent()
        );
        given(articleBusiness.update(articleUpdateRequest)).willReturn(doArticleResponse);
        
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_UPDATE;
        
        
        // when - then
        mvc.perform(
                        put("/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleUpdateRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    @DisplayName("게시글 수정 - 실패: 남의 계정")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_MEMBER,
            RoleType.ROLE_ADMIN
    })
    void updateByArticleId_fail(String roleType) throws Exception {
        // given
        long loginUserId = 1L;
        long targetUserId = 2L;
        
        setSecurityContext(loginUserId, roleType);
        
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .id(3L)
                .articleId(234L)
                .userId(targetUserId)
                .title("제목")
                .content("내용")
                .build();
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
        
        
        // when - then
        mvc.perform(
                        put("/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleUpdateRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    @DisplayName("게시글 수정 - 실패: 권한 없음(탈퇴자, 비회원)")
    @Nested
    class UpdateFailCase {
        
        @Test
        void save_fail() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            mvc.perform(put("/update"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @Test
        @WithAnonymousUser
        void save_fail_with_anonymous() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            mvc.perform(put("/update"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void save_fail_with_withdrawal() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
            
            mvc.perform(put("/update"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("게시글 삭제 - 성공")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_MEMBER,
            RoleType.ROLE_ADMIN
    })
    void deleteByArticleId_success(String roleType) throws Exception {
        // given
        long loginUserId = 1L;
        
        setSecurityContext(loginUserId, roleType);
        
        ArticlesDeleteRequest articleDeleteRequest = ArticlesDeleteRequest.builder()
                .userId(loginUserId)
                .articleIds(Set.of(3L, 9L, 65L, 21L, 54L))
                .build();
        
        willDoNothing().given(articleBusiness).delete(articleDeleteRequest);
        
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_DELETE;
        
        
        // when - then
        mvc.perform(
                        post("/delete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleDeleteRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    @DisplayName("게시글 삭제 - 실패: 남의 계정")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_MEMBER,
            RoleType.ROLE_ADMIN
    })
    void deleteByArticleId_fail(String roleType) throws Exception {
        // given
        long loginUserId = 1L;
        long targetUserId = 2L;
        
        setSecurityContext(loginUserId, roleType);
        
        ArticlesDeleteRequest articleDeleteRequest = ArticlesDeleteRequest.builder()
                .userId(targetUserId)
                .articleIds(Set.of(3L, 9L, 65L, 21L, 54L))
                .build();
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
        
        
        // when - then
        mvc.perform(
                        post("/delete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(articleDeleteRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    @DisplayName("게시글 삭제 - 실패: 권한 없음(탈퇴자, 비회원)")
    @Nested
    class DeleteFailCase {
        
        @Test
        void deleteByArticleId_fail() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            mvc.perform(post("/delete"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @Test
        @WithAnonymousUser
        void deleteByArticleId_fail_with_anonymous() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            mvc.perform(post("/delete"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void deleteByArticleId_fail_with_withdrawal() throws Exception {
            String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
            
            mvc.perform(post("/delete"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("탈퇴 유저 게시글 삭제")
    @Test
    void deleteAllByUser() throws Exception {
        // given
        Set<Long> userIds = Set.of(3L, 9L, 65L, 21L, 54L);
        
        String responseFailMessage = ExceptionMessage.EXCEPTION_DELETED_WITHDRAWAL
                + ExceptionMessage.EXCEPTION_NOT_WITHDRAWAL;
        
        given(articleBusiness.deleteUserAll(anyCollection())).willReturn(responseFailMessage);
        
        String responseMessage = ResponseMessage.RESPONSE_ARTICLE_DELETE_FOR_WITHDRAWAL;
        
        
        // when - then
        mvc.perform(
                        post("/delete/withdrawal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(userIds))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result").value(responseFailMessage))
                .andDo(print());
    }
    
    
    
    private List<ArticleResponse> getArticleResponseList_30() {
        return LongStream
                .range(0, 30)
                .mapToObj(id -> {
                    long userId = id / 10 + 1;
                    long articleId = id % 10 + 1;
                    
                    return getArticleResponse(
                            id, articleId, userId,
                            "제목" + articleId, "내용 " + articleId,
                            LocalDateTime.now().minusDays(id), LocalDateTime.now().minusDays(id)
                    );
                })
                .toList();
    }
    private ArticleResponse getArticleResponse(
            long id, long articleId, long userId,
            String title, String content,
            LocalDateTime createdAt, LocalDateTime modifiedAt
    ) {
        return ArticleResponse.builder()
                .id(id)
                .articleId(articleId)
                .user(JwtUserInfoDto.builder()
                        .id(userId)
                        .email("user" + userId)
                        .nickname("닉넴" + userId)
                        .build())
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }
    private ArticleResponse getArticleResponse(
            long id, long articleId, long userId,
            String title, String content
    ) {
        return ArticleResponse.builder()
                .id(id)
                .articleId(articleId)
                .user(JwtUserInfoDto.builder()
                        .id(userId)
                        .email("user" + userId)
                        .nickname("닉넴" + userId)
                        .build())
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
    
    private void setSecurityContext(long requestUserId, String roleType) {
        TestSecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken
                                .authenticated(
                                        JwtUserInfoEntity.builder()
                                                .id(requestUserId)
                                                .email("user@email.mail")
                                                .nickname("user")
                                                .grade(roleType)
                                                .build(),
                                        null,
                                        List.of(new SimpleGrantedAuthority(roleType))
                                )
                );
    }
}
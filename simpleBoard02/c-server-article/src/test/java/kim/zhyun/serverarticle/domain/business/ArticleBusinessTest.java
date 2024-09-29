package kim.zhyun.serverarticle.domain.business;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.exception.message.CommonExceptionMessage;
import kim.zhyun.serverarticle.common.message.ExceptionMessage;
import kim.zhyun.serverarticle.domain.controller.model.ArticleResponse;
import kim.zhyun.serverarticle.domain.controller.model.ArticleSaveRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticleUpdateRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.domain.converter.ArticleConverter;
import kim.zhyun.serverarticle.domain.respository.ArticleEntity;
import kim.zhyun.serverarticle.domain.service.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class ArticleBusinessTest {

    @InjectMocks ArticleBusiness articleBusiness;
    
    @Mock ArticleService articleService;
    @Mock ArticleConverter articleConverter;


    @DisplayName("전체 게시글 조회 - 생성일자 내림차순")
    @Test
    void findAll() {
        // given
        List<ArticleEntity> articleEntityList = LongStream.rangeClosed(1, 5)
                .mapToObj(id -> {
                    long articleId = id % 10;
                    long userId = id / 10 + 1;
                    
                    return getArticleEntity(
                            id, articleId, userId,
                            LocalDateTime.now().minusDays(id), LocalDateTime.now().minusDays(id)
                    );
                })
                .toList();
        
        given(articleService.findAllOrderByCreatedAtDesc()).willReturn(articleEntityList);
        
        List<ArticleResponse> articleResponseList = articleEntityList.stream()
                .map(articleEntity -> {
                    JwtUserInfoDto jwtUserInfoDto = getJwtUserInfoDto(articleEntity);
                    ArticleResponse articleResponse = getArticleResponse(articleEntity, jwtUserInfoDto);
                    
                    given(articleConverter.toResponse(eq(articleEntity))).willReturn(articleResponse);
                    
                    return articleResponse;
                })
                .toList();
        
        
        // when
        List<ArticleResponse> resultArticleResponseList = articleBusiness.findAll();
        
        
        // then
        assertEquals(articleResponseList, resultArticleResponseList);
        
        assertTrue(resultArticleResponseList.get(0).getCreatedAt()
                .isAfter(resultArticleResponseList.get(4).getCreatedAt()));
        assertTrue(resultArticleResponseList.get(0).getCreatedAt()
                .isAfter(resultArticleResponseList.get(2).getCreatedAt()));
        assertTrue(resultArticleResponseList.get(2).getCreatedAt()
                .isAfter(resultArticleResponseList.get(4).getCreatedAt()));
    }
    
    
    @DisplayName("특정 사용자 게시글 전체 조회")
    @Test
    void findAllByUser() {
        // given
        long targetUserId = (long)(Math.random() * 3) + 1; // 1 ~ 3
        List<ArticleEntity> targetArticleEntityList = LongStream.range(1, 25)
                .mapToObj(id -> {
                    long articleId = id % 10;
                    long userId = id / 10 + 1;
                    
                    return getArticleEntity(
                            id, articleId, userId,
                            LocalDateTime.now().minusDays(id), LocalDateTime.now().minusDays(id)
                    );
                })
                .filter(articleEntity -> articleEntity.getUserId() == targetUserId)
                .toList();
        
        given(articleService.findAllByUserIdOrderByCreatedAtDesc(targetUserId)).willReturn(targetArticleEntityList);
        
        List<ArticleResponse> articleResponseList = targetArticleEntityList.stream()
                .map(articleEntity -> {
                    JwtUserInfoDto jwtUserInfoDto = getJwtUserInfoDto(articleEntity);
                    ArticleResponse articleResponse = getArticleResponse(articleEntity, jwtUserInfoDto);
                    
                    given(articleConverter.toResponse(eq(articleEntity))).willReturn(articleResponse);
                    
                    return articleResponse;
                })
                .toList();
        
        
        // when
        List<ArticleResponse> resultArticleResponseList = articleBusiness.findAllByUser(targetUserId);
        
        
        // then
        assertEquals(articleResponseList, resultArticleResponseList);
        
        int lastIndex = resultArticleResponseList.size() - 1;
        assertTrue(resultArticleResponseList.get(0).getCreatedAt()
                .isAfter(resultArticleResponseList.get(lastIndex).getCreatedAt()));
        assertTrue(resultArticleResponseList.get(0).getCreatedAt()
                .isAfter(resultArticleResponseList.get(2).getCreatedAt()));
        assertTrue(resultArticleResponseList.get(2).getCreatedAt()
                .isAfter(resultArticleResponseList.get(lastIndex).getCreatedAt()));
    }
    
    
    @DisplayName("특정 사용자 게시글 1건 조회")
    @Test
    void findByArticleId() {
        // given
        long targetUserId = 1L;
        long targetArticleId = 10L;
        
        ArticleEntity articleEntity = getArticleEntity(8L, targetArticleId, targetUserId, LocalDateTime.now(), LocalDateTime.now());
        given(articleService.findByUserIdAndArticleId(targetUserId, targetArticleId)).willReturn(articleEntity);
        
        JwtUserInfoDto jwtUserInfoDto = getJwtUserInfoDto(articleEntity);
        ArticleResponse articleResponse = getArticleResponse(articleEntity, jwtUserInfoDto);
        given(articleConverter.toResponse(eq(articleEntity))).willReturn(articleResponse);
        
        
        // when
        ArticleResponse resultArticleResponse = articleBusiness.findByArticleId(targetUserId, targetArticleId);
        
        
        // then
        assertEquals(articleResponse, resultArticleResponse);
    }
    
    
    @DisplayName("게시글 저장")
    @Test
    void save() {
        // given
        long userId = 1L;
        ArticleSaveRequest articleSaveRequest = ArticleSaveRequest.builder()
                .title("새 글")
                .content("안녕하세요 🌂")
                .build();
        
        ArticleEntity newArticleEntity = getArticleEntity(
                userId,
                articleSaveRequest.getTitle(), articleSaveRequest.getContent(),
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(articleConverter.toEntity(eq(articleSaveRequest), eq(userId))).willReturn(newArticleEntity);
        
        ArticleEntity savedArticleEntity = getArticleEntity(
                9L, 2L, userId,
                articleSaveRequest.getTitle(), articleSaveRequest.getContent(),
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(articleService.save(newArticleEntity)).willReturn(savedArticleEntity);
        
        JwtUserInfoDto jwtUserInfoDto = getJwtUserInfoDto(savedArticleEntity);
        ArticleResponse articleResponse = getArticleResponse(savedArticleEntity, jwtUserInfoDto);
        given(articleConverter.toResponse(eq(savedArticleEntity))).willReturn(articleResponse);

        TestSecurityContextHolder.setAuthentication(new JwtAuthentication(
                jwtUserInfoDto, "this-is-jwt", Set.of(new SimpleGrantedAuthority(RoleType.ROLE_MEMBER))
        ));


        // when
        ArticleResponse resultArticleResponse = articleBusiness.save(articleSaveRequest);
        
        
        // then
        assertEquals(articleResponse, resultArticleResponse);
    }
    
    
    @DisplayName("게시글 수정 - 성공")
    @Test
    void update_success() {
        // given
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .id(7L)
                .articleId(2L)
                .userId(1L)
                .title("수정) 글")
                .content("안녕하세요 ☔")
                .build();
        
        ArticleEntity originArticleEntity = getArticleEntity(
                articleUpdateRequest.getId(), articleUpdateRequest.getArticleId(), articleUpdateRequest.getUserId(),
                "제목", "내용 🌂",
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(articleService.findByUserIdAndArticleId(articleUpdateRequest.getUserId(), articleUpdateRequest.getArticleId()))
                .willReturn(originArticleEntity);
        
        ArticleEntity updatedArticleEntity = getArticleEntity(
                articleUpdateRequest.getId(), articleUpdateRequest.getArticleId(), articleUpdateRequest.getUserId(),
                articleUpdateRequest.getTitle(), articleUpdateRequest.getContent(),
                originArticleEntity.getCreatedAt(), LocalDateTime.now()
        );
        given(articleService.update(eq(originArticleEntity), eq(articleUpdateRequest))).willReturn(updatedArticleEntity);
        
        JwtUserInfoDto jwtUserInfoDto = getJwtUserInfoDto(updatedArticleEntity);
        ArticleResponse articleResponse = getArticleResponse(updatedArticleEntity, jwtUserInfoDto);
        given(articleConverter.toResponse(eq(updatedArticleEntity))).willReturn(articleResponse);
        
        
        // when
        ArticleResponse resultArticleResponse = articleBusiness.update(articleUpdateRequest);
        
        
        // then
        assertEquals(articleResponse, resultArticleResponse);
    }
    
    @DisplayName("게시글 수정 - 실패")
    @Test
    void update_fail() {
        // given
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .id(7L)
                .articleId(2L)
                .userId(1L)
                .title("수정) 글")
                .content("안녕하세요 ☔")
                .build();
        
        given(articleService.findByUserIdAndArticleId(articleUpdateRequest.getUserId(), articleUpdateRequest.getArticleId()))
                .willReturn(null);
        
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> articleBusiness.update(articleUpdateRequest),
                CommonExceptionMessage.EXCEPTION_NOT_FOUND
        );
    }
    
    
    @DisplayName("게시글 삭제")
    @Test
    void delete() {
        // given
        ArticlesDeleteRequest articlesDeleteRequest = ArticlesDeleteRequest.builder()
                .userId(3L)
                .articleIds(Set.of(9L, 23L, 16L, 88L))
                .build();
        
        willDoNothing().given(articleService).delete(articlesDeleteRequest);
        
        
        // when - then
        assertDoesNotThrow(() -> articleBusiness.delete(articlesDeleteRequest));
    }
    
    
    @DisplayName("탈퇴자 게시글 삭제")
    @Test
    void deleteUserAll() {
        // given
        Set<Long> withdrawalIds = Set.of(3L, 6L, 10L, 324L, 1L);
        
        String doFailMessage = ExceptionMessage.EXCEPTION_DELETED_WITHDRAWAL.formatted(324L)
                + ExceptionMessage.EXCEPTION_DELETED_WITHDRAWAL.formatted(6L);
        given(articleService.deleteUserAll(withdrawalIds)).willReturn(doFailMessage);
        
        
        // when
        String resultFailMessage = articleBusiness.deleteUserAll(withdrawalIds);
        
        
        // then
        assertEquals(doFailMessage, resultFailMessage);
    }
    
    
    
    private ArticleResponse getArticleResponse(ArticleEntity articleEntity, JwtUserInfoDto jwtUserInfoDto) {
        return ArticleResponse.builder()
                .id(articleEntity.getId())
                .articleId(articleEntity.getArticleId())
                .user(jwtUserInfoDto)
                .title(articleEntity.getTitle())
                .content(articleEntity.getContent())
                .createdAt(articleEntity.getCreatedAt())
                .modifiedAt(articleEntity.getModifiedAt())
                .build();
        
    }
    private JwtUserInfoDto getJwtUserInfoDto(ArticleEntity articleEntity) {
        return JwtUserInfoDto.builder()
                .id(articleEntity.getUserId())
                .email("user@email.mail")
                .nickname("김유저")
                .build();
    }
    private ArticleEntity getArticleEntity(long userId, String title, String content, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return ArticleEntity.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }
    private ArticleEntity getArticleEntity(Long id, long articleId, long userId, String title, String content, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return ArticleEntity.builder()
                .id(id)
                .articleId(articleId)
                .userId(userId)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }
    private ArticleEntity getArticleEntity(Long id, long articleId, long userId, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return ArticleEntity.builder()
                .id(id)
                .articleId(articleId)
                .userId(userId)
                .title("title " + articleId)
                .content("content " + articleId)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }
}

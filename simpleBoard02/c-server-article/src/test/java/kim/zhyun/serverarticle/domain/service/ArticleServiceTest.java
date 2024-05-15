package kim.zhyun.serverarticle.domain.service;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.exception.message.CommonExceptionMessage;
import kim.zhyun.serverarticle.common.value.ArticleValue;
import kim.zhyun.serverarticle.domain.controller.model.ArticleUpdateRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.domain.respository.ArticleEntity;
import kim.zhyun.serverarticle.domain.respository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static kim.zhyun.serverarticle.common.message.ExceptionMessage.EXCEPTION_DELETED_WITHDRAWAL;
import static kim.zhyun.serverarticle.common.message.ExceptionMessage.EXCEPTION_NOT_WITHDRAWAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@DisplayName("article service test")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    
    ArticleService articleService;
    
    @Mock JwtUserInfoRepository jwtUserInfoRepository;
    @Mock ArticleRepository articleRepository;
    
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    
    ArticleValue articleValue = new ArticleValue("ARTICLE_ID:");
    
    
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        articleService = new ArticleService(
                jwtUserInfoRepository,
                articleRepository,
                redisTemplate,
                articleValue
        );
    }
    
    @DisplayName("전체 조회- 생성일 내림차순(ex. 오늘날짜 -> 0번)")
    @Test
    void findAllOrderByCreatedAtDesc() {
        // given
        int maxRange = 100;
        List<ArticleEntity> doArticleEntityList = LongStream.range(0, maxRange)
                .mapToObj(id -> {
                    long articleId = id % 10 + 1;
                    long userId = id / 10 + 1;
                    
                    return getArticleEntity(
                            id, articleId, userId,
                            "user %d 제목 %d".formatted(userId, articleId),
                            "user %d 내용 %d".formatted(userId, articleId),
                            LocalDateTime.now().minusDays(id),
                            LocalDateTime.now().minusDays(id)
                    );
                })
                .toList();

        given(articleRepository.findAll(Sort.by(Sort.Order.desc("createdAt")))).willReturn(doArticleEntityList);
        
        
        // when
        List<ArticleEntity> resultArticleEntityList = articleService.findAllOrderByCreatedAtDesc();
        
        
        // then
        assertThat(resultArticleEntityList).isNotEmpty();
        assertEquals(doArticleEntityList, resultArticleEntityList);
        
        assertTrue(resultArticleEntityList.get(0).getCreatedAt()
                .isAfter(resultArticleEntityList.get(99).getCreatedAt()));
        assertTrue(resultArticleEntityList.get(0).getCreatedAt()
                .isAfter(resultArticleEntityList.get(33).getCreatedAt()));
        assertTrue(resultArticleEntityList.get(33).getCreatedAt()
                .isAfter(resultArticleEntityList.get(55).getCreatedAt()));
        assertTrue(resultArticleEntityList.get(55).getCreatedAt()
                .isAfter(resultArticleEntityList.get(99).getCreatedAt()));
    }
    
    
    @DisplayName("특정 `userId`에 해당하는 게시글 전체 조회-생성일 내림차순(ex. 오늘날짜 -> 0번)")
    @Test
    void findAllByUserIdOrderByCreatedAtDesc() {
        // given
        long targetUserId = (long) (Math.random() * 10 + 1);
        int maxRange = 100;
        List<ArticleEntity> doArticleEntityList = LongStream.range(0, maxRange)
                .mapToObj(id -> {
                    long articleId = id % 10 + 1;
                    long userId = id / 10 + 1;
                    
                    return getArticleEntity(
                            (long)id, articleId, userId,
                            "user %d 제목 %d".formatted(userId, articleId),
                            "user %d 내용 %d".formatted(userId, articleId),
                            LocalDateTime.now().minusDays(id),
                            LocalDateTime.now().minusDays(id)
                    );
                })
                .filter(article -> article.getUserId() == targetUserId)
//                .peek(System.out::println)
                .toList();
        
        given(articleRepository.findAllByUserIdOrderByCreatedAtDesc(targetUserId)).willReturn(doArticleEntityList);
        
        
        // when
        List<ArticleEntity> resultArticleEntityList = articleService.findAllByUserIdOrderByCreatedAtDesc(targetUserId);
        
        
        // then
        assertThat(resultArticleEntityList).isNotEmpty();
        assertEquals(doArticleEntityList, resultArticleEntityList);
        
        assertTrue(resultArticleEntityList.get(0).getCreatedAt()
                .isAfter(resultArticleEntityList.get(9).getCreatedAt()));
        assertTrue(resultArticleEntityList.get(0).getCreatedAt()
                .isAfter(resultArticleEntityList.get(3).getCreatedAt()));
        assertTrue(resultArticleEntityList.get(3).getCreatedAt()
                .isAfter(resultArticleEntityList.get(5).getCreatedAt()));
        assertTrue(resultArticleEntityList.get(5).getCreatedAt()
                .isAfter(resultArticleEntityList.get(9).getCreatedAt()));
    }
    
    
    @DisplayName("특정 `userId`와 `article`에 해당하는 게시글 1건 조회")
    @Test
    void findByUserIdAndArticleId() {
        // given
        long targetUserId = (long) (Math.random() * 10 + 1);
        long targetArticleId = (long) (Math.random() * 10 + 1);
        int maxRange = 100;
        Optional<ArticleEntity> doOptionalArticleEntity = LongStream.range(0, maxRange)
                .mapToObj(id -> {
                    long articleId = id % 10 + 1;
                    long userId = id / 10 + 1;
                    
                    return getArticleEntity(
                            id, articleId, userId,
                            "user %d 제목 %d".formatted(userId, articleId),
                            "user %d 내용 %d".formatted(userId, articleId),
                            LocalDateTime.now().minusDays(id),
                            LocalDateTime.now().minusDays(id)
                    );
                })
                .filter(article -> (article.getUserId() == targetUserId) && (article.getArticleId() == targetArticleId))
                .peek(System.out::println)
                .findFirst();
        
        given(articleRepository.findByUserIdAndArticleId(targetUserId, targetArticleId)).willReturn(doOptionalArticleEntity);
        
        
        // when
        ArticleEntity resultArticleEntity = articleService.findByUserIdAndArticleId(targetUserId, targetArticleId);
        
        
        // then
        assertTrue(doOptionalArticleEntity.isPresent());
        assertEquals(doOptionalArticleEntity.get(), resultArticleEntity);
    }
    
    
    @DisplayName("게시글 저장")
    @Test
    void save() {
        // given
        // -- new article id 생성
        long userId = 10L;
        long newArticleId = 3L;
        String redisArticleCountKey = articleValue.REDIS_ARTICLE_ID_KEY + userId;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(redisArticleCountKey)).willReturn(newArticleId);
        
        ArticleEntity requestArticleEntity = getArticleEntity(
                null, newArticleId, userId, "제목", "내용", LocalDateTime.now(), LocalDateTime.now()
        );
        ArticleEntity responseArticleEntity = getArticleEntity(
                1L, requestArticleEntity.getArticleId(), requestArticleEntity.getUserId(), requestArticleEntity.getTitle(), requestArticleEntity.getContent(), requestArticleEntity.getCreatedAt(), requestArticleEntity.getModifiedAt()
        );
        given(articleRepository.save(requestArticleEntity)).willReturn(responseArticleEntity);
        
        
        // when
        ArticleEntity savedArticleEntity = articleService.save(requestArticleEntity);
        
        
        // then
        assertEquals(responseArticleEntity, savedArticleEntity);
    }
    
    
    @DisplayName("게시글 수정")
    @Test
    void update() {
        long userId = 10L;
        long articleId = 3L;
        
        ArticleEntity requestArticleEntity = getArticleEntity(
                1L, articleId, userId, "제목", "내용", LocalDateTime.now(), LocalDateTime.now()
        );
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .id(1L)
                .articleId(articleId)
                .userId(userId)
                .title("제목 수정")
                .content("내용 수정")
                .build();
        ArticleEntity responseArticleEntity = getArticleEntity(
                1L, articleId, userId,
                articleUpdateRequest.getTitle(), articleUpdateRequest.getContent(),
                requestArticleEntity.getCreatedAt(), LocalDateTime.now()
        );
        given(articleRepository.save(requestArticleEntity)).willReturn(responseArticleEntity);

        
        // when
        ArticleEntity resultArticleEntity = articleService.update(requestArticleEntity, articleUpdateRequest);
        
        
        // then
        assertEquals(responseArticleEntity, resultArticleEntity);
    }
    
    
    @DisplayName("게시글 삭제")
    @Test
    void delete() {
        // given
        long targetUserId = 1L;
        ArticlesDeleteRequest articlesDeleteRequest = ArticlesDeleteRequest.builder()
                .articleIds(Set.of(3L, 40L, 7L, 99L))
                .userId(targetUserId)
                .build();
        
        // -- userId 전체 게시글 조회
        List<ArticleEntity> userArticleEntityList = LongStream.range(0, 100)
                .mapToObj(id -> {
                    long articleId = id % 50 + 1;
                    long userId = id / 50 + 1;
                    
                    return getArticleEntity(
                            id, articleId, userId,
                            "제목 " + articleId, "내용 " + articleId,
                            LocalDateTime.now(), LocalDateTime.now()
                    );
                })
                .filter(article -> article.getUserId() == targetUserId)
                .toList();
        given(articleRepository.findAllByUserIdOrderByCreatedAtDesc(articlesDeleteRequest.getUserId()))
                .willReturn(userArticleEntityList);
        
        // -- userId 전체 게시글에서 삭제할 게시글 얻기
        Set<ArticleEntity> deleteArticleEntitySet = userArticleEntityList.stream()
                .filter(userArticle -> articlesDeleteRequest.getArticleIds().contains(userArticle.getArticleId()))
//                .peek(System.out::println)
                .collect(Collectors.toSet());
        willDoNothing().given(articleRepository).deleteAllInBatch(deleteArticleEntitySet);

        
        // when
        articleService.delete(articlesDeleteRequest);
        
        
        // then
        // -- 삭제할 게시글에서 삭제 요청 게시글 제외하고 남은 수 확인
        int resultRequestDeleteArticleCount = deleteArticleEntitySet.stream()
                .filter(articleEntity -> !articlesDeleteRequest.getArticleIds().contains(articleEntity.getArticleId()))
                .collect(Collectors.toSet())
                .size();
        assertEquals(resultRequestDeleteArticleCount, 0);
    }
    
    
    @DisplayName("탈퇴자 게시글 전체 삭제")
    @Test
    void deleteUserAll() {
        // given
        long memberId = 3L;
        long withdrawalId = 5L;
        long expiredWithdrawalId = 1L;
        Collection<Long> requestUserIds = Set.of(expiredWithdrawalId, memberId, withdrawalId);
        
        JwtUserInfoEntity member = JwtUserInfoEntity.builder()
                .id(memberId)
                .email("user3@email.mail")
                .nickname("유저3")
                .grade(RoleType.ROLE_MEMBER)
                .build();
        
        JwtUserInfoEntity withdrawal = JwtUserInfoEntity.builder()
                .id(withdrawalId)
                .email("user5@email.mail")
                .nickname("유저5")
                .grade(RoleType.ROLE_WITHDRAWAL)
                .build();
        given(jwtUserInfoRepository.findById(eq(expiredWithdrawalId))).willReturn(Optional.empty());
        given(jwtUserInfoRepository.findById(eq(memberId))).willReturn(Optional.of(member));
        given(jwtUserInfoRepository.findById(eq(withdrawalId))).willReturn(Optional.of(withdrawal));

        // -- 탈퇴자 게시글 생성
        List<ArticleEntity> withdrawalArticleEntities = LongStream.range(1, 10)
                        .mapToObj(id -> getArticleEntity(
                                id, id, withdrawalId, "title", "content", LocalDateTime.now(), LocalDateTime.now()
                        )).toList();
        given(articleRepository.findAllByUserIdOrderByCreatedAtDesc(eq(withdrawalId))).willReturn(withdrawalArticleEntities);
        
        // -- 탈퇴자 게시글 삭제
        willDoNothing().given(articleRepository).deleteAllInBatch(withdrawalArticleEntities);
        given(redisTemplate.delete(articleValue.REDIS_ARTICLE_ID_KEY + withdrawalId)).willReturn(true);
        
        // -- `redis`에서 user 정보 기져오기
        given(jwtUserInfoRepository.findById(memberId)).willReturn(Optional.of(member));
        given(jwtUserInfoRepository.findById(expiredWithdrawalId)).willReturn(Optional.empty());
        
        // -- 결과 메세지 생성
        String doFailMessage1 = EXCEPTION_NOT_WITHDRAWAL.formatted(memberId, member.getEmail());
        String doFailMessage2 = EXCEPTION_DELETED_WITHDRAWAL.formatted(expiredWithdrawalId);
        
        
        // when
        String resultFailMessage = articleService.deleteUserAll(requestUserIds);
        
        
        // then
        assertTrue(resultFailMessage.contains(doFailMessage1));
        assertTrue(resultFailMessage.contains(doFailMessage2));
        assertTrue(
                resultFailMessage.replace(doFailMessage1, "")
                        .replace(doFailMessage2, "")
                        .trim()
                        .isEmpty()
        );
    }
    
    
    @DisplayName("`redis`에서 `user`정보 가져와서 반환 - 실패: 없는 사용자")
    @Test
    void findJwtUserInfoEntityByIdWithThrow_fail() {
        long requestUserId = 1L;
        
        given(jwtUserInfoRepository.findById(requestUserId)).willReturn(Optional.empty());
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> articleService.findJwtUserInfoEntityByIdWithThrow(requestUserId),
                CommonExceptionMessage.EXCEPTION_NOT_FOUND
        );
    }
    
    @DisplayName("`redis`에서 `user`정보 가져와서 반환 - 성공")
    @Test
    void findJwtUserInfoEntityByIdWithThrow_success() {
        long requestUserId = 1L;
        
        JwtUserInfoEntity doJwtUserInfoEntity = JwtUserInfoEntity.builder()
                .id(requestUserId)
                .email("유저@email.mail")
                .nickname("회원")
                .grade(RoleType.ROLE_MEMBER)
                .build();
        given(jwtUserInfoRepository.findById(requestUserId)).willReturn(Optional.of(doJwtUserInfoEntity));
        
        
        // when
        JwtUserInfoEntity resultJwtUserInfoEntity = articleService.findJwtUserInfoEntityByIdWithThrow(requestUserId);

        
        // then
        assertEquals(doJwtUserInfoEntity, resultJwtUserInfoEntity);
    }
    
    
    
    private static ArticleEntity getArticleEntity(
            Long id, long articleId, long userId, String title, String content, LocalDateTime createdAt, LocalDateTime modifiedAt
    ) {
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
}


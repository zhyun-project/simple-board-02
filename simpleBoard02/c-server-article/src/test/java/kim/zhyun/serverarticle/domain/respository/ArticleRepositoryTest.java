package kim.zhyun.serverarticle.domain.respository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Article Entity CRUD 테스트")
@SpringBootTest
class ArticleRepositoryTest {
    
    @Autowired ArticleRepository repository;
    
    
    
    @DisplayName("article 90개 생성 (articleId: 1 ~ 30, userId: 1 ~ 3)")
    @BeforeEach
    void beforeEach() {
        makeDummyArticleData();
    }
    
    @DisplayName("article data 전체 삭제")
    @AfterEach
    void afterEach() {
        List<ArticleEntity> all = repository.findAll(Sort.by("createdAt"));
        repository.deleteAllInBatch(all);
    }
    
    
    
    @Disabled("`@BeforeEach`에서 사용하는 기능이라 실행 성공 확인 후 disabled")
    @DisplayName("article 저장")
    @Test
    void save() {
        // given
        ArticleEntity requestArticleEntity = getArticleEntity(
                55L, 55L, "제목 55", "내용 55"
        );
        
        
        // when
        ArticleEntity saved = repository.save(requestArticleEntity);
        
        
        // then
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getModifiedAt());
        assertEquals(saved.getCreatedAt(), saved.getModifiedAt());
    }
    
    
    @DisplayName("sort 입력 받아서 전체 게시글 조회")
    @Test
    void findAll_with_sort() {
        // given
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        
        
        // when
        List<ArticleEntity> resultArticles = repository.findAll(sort);
        
        
        // then
        assertThat(resultArticles).isNotEmpty();
        assertTrue(resultArticles.get(0).getCreatedAt().isAfter(resultArticles.get(55).getCreatedAt()));
        assertTrue(resultArticles.get(5).getCreatedAt().isAfter(resultArticles.get(77).getCreatedAt()));
        assertTrue(resultArticles.get(33).getCreatedAt().isAfter(resultArticles.get(89).getCreatedAt()));
        assertTrue(resultArticles.get(0).getCreatedAt().isAfter(resultArticles.get(89).getCreatedAt()));
    }
    
    
    @DisplayName("user id로 article 조회 + 생성일자 내림차순 정렬")
    @ParameterizedTest
    @ValueSource(longs = {
            1L, 2L, 3L
    })
    void findAll_by_UserId_with_OrderBy_CreatedAt_Desc(long userId) {
        // when
        List<ArticleEntity> resultArticles = repository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        
        // then
        assertThat(resultArticles).isNotEmpty();
        
        // -- createdAt 내림차순 검증
        ArticleEntity firstArticle = resultArticles.get(0);
        ArticleEntity lastArticle = resultArticles.get(resultArticles.size() - 1);

        assertTrue(firstArticle.getCreatedAt().isAfter(lastArticle.getCreatedAt()));
        
        // -- userId 검증
        repository.deleteAllInBatch(resultArticles);
        List<ArticleEntity> articlesByDeletedTargetUserId = repository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        assertTrue(articlesByDeletedTargetUserId.isEmpty());
    }
    
    
    @DisplayName("`userId`와 `articleId`로 article 조회")
    @ParameterizedTest
    @CsvSource({
            "1, 10",
            "2, 22",
            "3, 30",
    })
    void findByUserIdAndArticleId(long userId, long articleId) {
        // when
        Optional<ArticleEntity> resultOptionalArticle = repository.findByUserIdAndArticleId(userId, articleId);
        
        
        // then
        assertTrue(resultOptionalArticle.isPresent());
        
        ArticleEntity articleEntity = resultOptionalArticle.get();
        
        assertEquals(articleEntity.getUserId(), userId);
        assertEquals(articleEntity.getArticleId(), articleId);
    }
    
    
    @DisplayName("article 수정")
    @ParameterizedTest
    @CsvSource({
            "1, 15",
            "2, 7",
            "3, 23",
    })
    void update(long userId, long articleId) {
        // given
        ArticleEntity originArticle = repository.findByUserIdAndArticleId(userId, articleId).get();
        
        String updateTitle   = "업뎃 %s".formatted(originArticle.getTitle());
        String updateContent = "업뎃 %s".formatted(originArticle.getContent());

        originArticle.setTitle(updateTitle);
        originArticle.setContent(updateContent);
        
        
        // when
        ArticleEntity updatedArticle = repository.save(originArticle);
        
        
        // then
        assertEquals(originArticle.getCreatedAt(), originArticle.getModifiedAt());
        assertNotEquals(updatedArticle.getCreatedAt(), updatedArticle.getModifiedAt());
        
        assertEquals(updatedArticle.getTitle(), updateTitle);
        assertEquals(updatedArticle.getContent(), updateContent);
    }
    
    
    @Disabled("`@AfterEach`에서 사용하는 기능이라 실행 성공 확인 후 disabled")
    @DisplayName("게시글 1건 이상 삭제")
    @ParameterizedTest
    @ValueSource(longs = {
            1L, 2L, 3L, 100L
    })
    void deleteAllInBatch(Long userId) {
        // given
        List<ArticleEntity> searchArticles = repository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        
        // when
        repository.deleteAllInBatch(searchArticles);
        
        
        // then
        List<ArticleEntity> resultArticles = repository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        assertTrue(resultArticles.isEmpty());
    }
    
    
    
    private void makeDummyArticleData() {
        IntStream.rangeClosed(1, 3)
                .forEach(userId ->
                        IntStream.rangeClosed(1, 30)
                                .forEach(articleId -> repository.save(getArticleEntity(
                                        articleId, userId,
                                        "title %d".formatted(articleId), "content %d".formatted(articleId)
                                )))
                );
    }
    
    private ArticleEntity getArticleEntity(
            long articleId, long userId, String title, String content
    ) {
        return ArticleEntity.builder()
                .articleId(articleId)
                .userId(userId)
                
                .title(title)
                .content(content)
                
                .build();
    }
}
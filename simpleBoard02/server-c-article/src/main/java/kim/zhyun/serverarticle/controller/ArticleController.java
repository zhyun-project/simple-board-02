package kim.zhyun.serverarticle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kim.zhyun.serverarticle.data.ArticleSaveRequest;
import kim.zhyun.serverarticle.data.ArticleUpdateRequest;
import kim.zhyun.serverarticle.data.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.data.response.ApiResponse;
import kim.zhyun.serverarticle.data.response.ArticleResponse;
import kim.zhyun.serverarticle.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import static kim.zhyun.serverarticle.data.message.ResponseMessage.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@Tag(name = "게시글 관련 API")
@RequiredArgsConstructor
@RestController
public class ArticleController {
    private final ArticleService articleService;
    
    @Operation(summary = "전체 게시글 조회")
    @GetMapping("/articles")
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_ARTICLE_FIND_ALL)
                .result(articleService.findAll()).build());
    }
    
    @Operation(summary = "유저 전체 게시글 조회")
    @GetMapping("/{userId}/articles")
    public ResponseEntity<Object> findAllByUser(@PathVariable long userId) {
        List<ArticleResponse> response = articleService.findAllByUser(userId);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(String.format(RESPONSE_ARTICLE_FIND_ALL_BY_USER, response.get(0).getUser().getNickname()))
                .result(response).build());
    }
    
    @Operation(summary = "유저 게시글 상세 조회")
    @GetMapping("/{userId}/articles/{articleId}")
    public ResponseEntity<Object> findByArticleId(@PathVariable long userId,
                                                  @PathVariable long articleId) {
        ArticleResponse response = articleService.findByArticleId(userId, articleId);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(String.format(RESPONSE_ARTICLE_FIND_ONE_BY_USER, response.getUser().getNickname(), articleId))
                .result(response).build());
    }
    
    @Operation(summary = "게시글 등록")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @PostMapping("/{userId}/articles")
    public ResponseEntity<Object> save(@PathVariable long userId,
                                       @RequestBody @Valid ArticleSaveRequest request) {
        return ResponseEntity.created(ServletUriComponentsBuilder
                        .fromCurrentRequestUri().path("/{id}").build(userId))
                .body(ApiResponse.builder()
                        .status(true)
                        .message(RESPONSE_ARTICLE_INSERT)
                        .result(articleService.save(request)).build());
    }
    
    @Operation(summary = "게시글 수정")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @PutMapping("/{userId}/articles/{articleId}")
    public ResponseEntity<Object> updateByArticleId(@PathVariable long userId,
                                                    @PathVariable long articleId,
                                                    @RequestBody @Valid ArticleUpdateRequest request) {
        articleService.update(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().build().toUri())
                .body(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_ARTICLE_UPDATE).build());
    }
    
    @Operation(summary = "게시글 삭제")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @DeleteMapping("/{userId}/articles")
    public ResponseEntity<Object> deleteByArticleId(@PathVariable long userId,
                                                    @RequestBody ArticlesDeleteRequest request) {
        articleService.delete(request);
        return ResponseEntity.status(NO_CONTENT)
                .body(ApiResponse.builder()
                        .status(true)
                        .message(RESPONSE_ARTICLE_DELETE).build());
    }
    
    @Operation(summary = "탈퇴 유저 게시글 삭제")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @DeleteMapping("/withdrawal/{userId}/articles")
    public ResponseEntity<Object> deleteAllByUser(@PathVariable long userId) {
        articleService.deleteUserAll(userId);
        return ResponseEntity.status(NO_CONTENT)
                .body(ApiResponse.builder()
                        .status(true)
                        .message(RESPONSE_ARTICLE_DELETE_FOR_WITHDRAWAL).build());
    }
    
}

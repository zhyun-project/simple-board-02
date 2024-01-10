package kim.zhyun.serverarticle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kim.zhyun.serverarticle.data.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "게시글 관련 API")
@RequiredArgsConstructor
@RestController
public class ArticleController {
    
    @Operation(summary = "전체 게시글 조회")
    @GetMapping("/articles")
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("전체 게시글 조회").build());
    }
    
    @Operation(summary = "유저 전체 게시글 조회")
    @GetMapping("/{userId}/articles")
    public ResponseEntity<Object> findAllByUser(@PathVariable long userId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("유저 전체 게시글 조회").build());
    }
    
    @Operation(summary = "유저 게시글 상세 조회")
    @GetMapping("/{userId}/articles/{articleId}")
    public ResponseEntity<Object> findByArticleId(@PathVariable long userId,
                                                  @PathVariable long articleId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("유저 게시글 상세 조회").build());
    }
    
    @Operation(summary = "게시글 등록")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @PostMapping("/{userId}/articles")
    public ResponseEntity<Object> save(@PathVariable long userId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("게시글 등록").build());
    }
    
    @Operation(summary = "게시글 수정")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @PutMapping("/{userId}/articles/{articleId}")
    public ResponseEntity<Object> updateByArticleId(@PathVariable long userId,
                                                    @PathVariable long articleId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("게시글 수정").build());
    }
    
    @Operation(summary = "게시글 삭제")
    @PreAuthorize("#userId == T(kim.zhyun.jwt.data.JwtUserDto).from(principal).id")
    @DeleteMapping("/{userId}/articles/{articleId}")
    public ResponseEntity<Object> deleteByArticleId(@PathVariable long userId,
                                                    @PathVariable long articleId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("게시글 삭제").build());
    }
    
}

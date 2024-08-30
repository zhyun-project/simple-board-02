package kim.zhyun.serverarticle.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.serverarticle.domain.business.ArticleBusiness;
import kim.zhyun.serverarticle.domain.controller.model.ArticleResponse;
import kim.zhyun.serverarticle.domain.controller.model.ArticleSaveRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticleUpdateRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticlesDeleteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.List;

import static kim.zhyun.serverarticle.common.message.ResponseMessage.*;

@Slf4j
@Tag(name = "게시글 관련 API")
@RequiredArgsConstructor
@RestController
public class ArticleController {
    private final ArticleBusiness articlebusiness;
    
    @Operation(summary = "전체 게시글 조회")
    @GetMapping("/all")
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_ARTICLE_FIND_ALL)
                .result(articlebusiness.findAll()).build());
    }
    
    @Operation(summary = "유저 전체 게시글 조회")
    @GetMapping("/all/user/{userId}")
    public ResponseEntity<Object> findAllByUser(
            @PathVariable long userId
    ) {
        List<ArticleResponse> response = articlebusiness.findAllByUser(userId);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_ARTICLE_FIND_ALL_BY_USER.formatted(userId))
                .result(response)
                .build());
    }

    @Operation(summary = "유저 게시글 상세 조회")
    @GetMapping("/{articleId}/user/{userId}")
    public ResponseEntity<Object> findByArticleId(
            @PathVariable long userId,
            @PathVariable long articleId
    ) {
        ArticleResponse response = articlebusiness.findByArticleId(userId, articleId);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_ARTICLE_FIND_ONE_BY_USER.formatted(userId, articleId))
                .result(response)
                .build());
    }

    @Operation(summary = "게시글 등록")
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<ArticleResponse>> save(
            @RequestBody @Valid ArticleSaveRequest request,
            Authentication authentication
    ) {
        JwtUserInfoDto userInfo = JwtUserInfoDto.from(authentication.getPrincipal());
        ArticleResponse response = articlebusiness.save(request, userInfo.getId());

        return ResponseEntity.created(ServletUriComponentsBuilder.fromPath("/all/user/{id}").build(userInfo.getId()))
                .body(ApiResponse.<ArticleResponse>builder()
                        .status(true)
                        .message(RESPONSE_ARTICLE_INSERT)
                        .result(response)
                        .build());
    }
    
    @Operation(summary = "게시글 수정")
    @PreAuthorize("(#request.getUserId() == T(kim.zhyun.jwt.domain.dto.JwtUserInfoDto).from(principal).id)")
    @PutMapping("/update")
    public ResponseEntity<Object> updateByArticleId(
            @RequestBody @Valid ArticleUpdateRequest request
    ) {
        articlebusiness.update(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromPath("/{articleId}/user/{id}")
                        .build(request.getArticleId(), request.getUserId()))
                .body(ApiResponse.builder()
                .status(true)
                .message(RESPONSE_ARTICLE_UPDATE).build());
    }
    
    @Operation(summary = "게시글 삭제")
    @PreAuthorize("(#request.getUserId() == T(kim.zhyun.jwt.domain.dto.JwtUserInfoDto).from(principal).id)")
    @PostMapping("/delete")
    public ResponseEntity<Object> deleteByArticleId(
            @RequestBody ArticlesDeleteRequest request
    ) {
        articlebusiness.delete(request);
        return ResponseEntity.ok(ApiResponse.builder()
                        .status(true)
                        .message(RESPONSE_ARTICLE_DELETE).build());
    }
    
    @Operation(summary = "탈퇴 유저 게시글 삭제")
    @PostMapping("/delete/withdrawal")
    public ResponseEntity<ApiResponse<String>> deleteAllByUser(
            @RequestBody Collection<Long> userIds
    ) {
        String failMessage = articlebusiness.deleteUserAll(userIds);
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(true)
                .message(RESPONSE_ARTICLE_DELETE_FOR_WITHDRAWAL)
                .result(failMessage)
                .build());
    }
    
}

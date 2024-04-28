package kim.zhyun.serverarticle.common.message;

public class ResponseMessage {
    public static final String RESPONSE_ARTICLE_DELETE_FOR_WITHDRAWAL = "탈퇴 회원의 게시글이 전부 삭제되었습니다.";
    public static final String RESPONSE_ARTICLE_DELETE = "게시글이 삭제되었습니다.";
    public static final String RESPONSE_ARTICLE_UPDATE = "게시글이 수정되었습니다.";
    public static final String RESPONSE_ARTICLE_INSERT = "게시글이 등록되었습니다.";
    
    public static final String RESPONSE_ARTICLE_FIND_ALL = "게시글 전체 조회";
    public static final String RESPONSE_ARTICLE_FIND_ALL_BY_USER = "userId %d - 게시글 전체 조회";
    public static final String RESPONSE_ARTICLE_FIND_ONE_BY_USER = "userId %d - 게시글 %d 조회";

}

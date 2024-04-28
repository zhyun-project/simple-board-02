package kim.zhyun.serverarticle.common.message;

import lombok.Getter;

@Getter
public class ExceptionMessage {
    
    // 공통
    public static final String EXCEPTION_VALID_FORMAT = "입력 값이 올바르지 않습니다.";
    public static final String EXCEPTION_REQUIRED_REQUEST_BODY = "Required request body is missing";
    public static final String EXCEPTION_PERMISSION = "권한이 없습니다.";
    public static final String EXCEPTION_AUTHENTICATION = "로그인이 필요합니다.";
    public static final String EXCEPTION_NOT_FOUND = "잘못된 요청입니다.";
    
    // 게시글
    public static final String EXCEPTION_TITLE_IS_NULL = "제목을 입력해주세요";
    public static final String EXCEPTION_CONTENT_IS_NULL = "내용을 입력해주세요";
    public static final String EXCEPTION_TITLE_FORMAT = "제목은 1~30글자 사이로 입력 가능합니다.";
    public static final String EXCEPTION_ARTICLE_NOT_FOUND = "게시글이 없습니다.";
    public static final String EXCEPTION_NOT_WITHDRAWAL = "- user id : [ %d ] email : [ %s ] 탈퇴 회원이 아닙니다.\n";
    public static final String EXCEPTION_DELETED_WITHDRAWAL = "- user id : [ %d ] 존재하지 않습니다.\n";
    
}

package kim.zhyun.serveruser.domain.signup.service;

import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.SECONDS;


@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final RedisTemplate<String, String> template;
    private final SessionUserService sessionUserService;
    
    
    @Value("${sign-up.email.expire}")   private long expireTime;
    
    /**
     * (redis) email auth
     * - email 존재 여부 반환 (이메일 인증을 마친 상태인지 확인 )
     */
    public boolean existEmail(EmailAuthDto dto) {
        return template.hasKey(dto.getEmail());
    }
    
    /**
     * (redis) email auth
     * - email에 할당된 인증코드인지 여부 반환
     */
    public boolean existCode(EmailAuthDto dto) {
        return template.opsForSet().isMember(dto.getEmail(), dto.getCode());
    }
    
    /**
     * (redis) email auth
     * - email과 인증 코드 신규 저장
     */
    public void saveEmailAuthInfo(EmailAuthDto dto) {
        template.opsForSet().add(dto.getEmail(), dto.getCode());
        template.expire(dto.getEmail(), expireTime, SECONDS);
    }
    
    /**
     * 1. (redis) email auth
     * - email 삭제
     * 2. (redis) session user
     * - email 할당
     */
    public void deleteAndUpdateSessionUserEmail(SessionUserEmailUpdateDto dto) {
        template.delete(dto.getEmail());
        sessionUserService.updateEmail(dto);
    }
    
}

package kim.zhyun.serveruser.domain.member.scheduler;


import kim.zhyun.serveruser.domain.member.client.ArticleClient;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_USER_INFO_KEY;
import static kim.zhyun.serveruser.utils.DateTimeUtil.beforeDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserScheduler {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ArticleClient articleClient;
    
    @Scheduled(cron = "${withdrawal.cron}", zone = "Asia/Seoul")
    public void userDeleteSchedule() {
        Set<UserEntity> rdbList = deleteListForRdb();
        Set<Long> userIds = deleteUserIdList(rdbList);
        Set<String> redisList = deleteListForRedis(rdbList);
        
        if (rdbList.size() > 0) {
            log.info("📆 Scheduler start - delete count = RDB: {}, userIds: {}, Redis: {} ----┐", rdbList.size(), userIds.size(), redisList.size());
            var response = articleClient.withdrawalArticleDelete(userIds);
            
            if (response.getBody().getStatus()) {
                userRepository.deleteAllInBatch(rdbList);
                redisTemplate.delete(redisList);
            }
            
            log.info("📆 Scheduler end--------------------------------------------------------┘");
        }
    }
    
    /**
     * article - 유예 기간 지난 탈퇴 회원 id 목록
     */
    private static Set<Long> deleteUserIdList(Set<UserEntity> deleteList) {
        return deleteList.stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
    }
    
    /**
     * redis - 유예 기간 지난 탈퇴 회원 목록
     */
    private static Set<String> deleteListForRedis(Set<UserEntity> deleteList) {
        return deleteList.stream()
                .map(user -> String.format("%s:%d", JWT_USER_INFO_KEY, user.getId()))
                .collect(Collectors.toSet());
    }
    
    /**
     * rdb - 유예 기간 지난 탈퇴 회원 목록
     */
    private Set<UserEntity> deleteListForRdb() {
        LocalDateTime targetDateTime = beforeDateTime();
        return userRepository.findAllByWithdrawalIsTrueOrderByModifiedAtAsc().stream()
                .filter(user -> user.getModifiedAt().isBefore(targetDateTime))
                .collect(Collectors.toSet());
    }
    
}

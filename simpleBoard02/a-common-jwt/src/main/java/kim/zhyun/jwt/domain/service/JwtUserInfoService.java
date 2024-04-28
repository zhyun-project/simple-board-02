package kim.zhyun.jwt.domain.service;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class JwtUserInfoService {
    
    private final JwtUserInfoRepository jwtUserInfoRepository;
    
    public Map<Long, JwtUserInfoDto> jwtUserInfoMap() {
        Map<Long, JwtUserInfoDto> jwtUserMap = new HashMap<>();
        
        jwtUserInfoRepository.findAll().stream()
                .filter(Objects::nonNull)
                .map(entity -> jwtUserMap.put(entity.getId(), JwtUserInfoDto.from(entity)))
                .close();
        
        return jwtUserMap;
    }
    
}

package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.NicknameDto;

public interface NicknameReserveService {
    
    boolean existNickname(NicknameDto dto);
    boolean availableNickname(NicknameDto dto);
    void saveNickname(NicknameDto dto);
    void deleteNickname(NicknameDto dto);
    
}

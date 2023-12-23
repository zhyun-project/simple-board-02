package kim.zhyun.serveruser.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
public class EmailAuth {
    
    @ColumnDefault("false")
    @Builder.Default
    private boolean isVerification = false;
    
    private String code;
    private LocalDateTime expiredAt;
    
}

package kim.zhyun.serveruser.data;

import lombok.*;

import java.util.Objects;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailAuthDto {
    
    private String email;
    
    @Getter
    private String code;
    
    public String getEmail() {
        return "EMAIL:" + email;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EmailAuthDto that = (EmailAuthDto) obj;
        
        if (!Objects.equals(email, that.email)) return false;
        return Objects.equals(code, that.code);
    }
    
}

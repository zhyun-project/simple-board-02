package kim.zhyun.jwt.domain.dto;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


public record JwtAuthentication (
        JwtUserInfoDto jwtUserInfoDto,
        String token,
        Collection<? extends GrantedAuthority> authorities
) implements Authentication {

    @Override
    public JwtUserInfoDto getPrincipal() {
        return jwtUserInfoDto;
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(jwtUserInfoDto.getId());
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {  }

}

package kr.or.kimsn.radarsms.config.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.or.kimsn.radarsms.dto.MembersDto;

//시큐리티가 /login 주소 요청이 오면 낚아채서 로그인을 진행시킨다.
// 로그인 완료가 되면 시큐리티 session을 만들어 줌.
//Security ContextHolder에 session 정보를 저장함.
//오브젝트 타입. => Authentication 타입객체
//Authentication 안에 User 정보가 있어야함.
//User 오브젝트 타입 => UserDetails 타입 객체
//Security Sesstion => Authentication => UserDetails(PrincipalDetails)
public class PrincipalDetails implements UserDetails {

    private MembersDto user; // 콤포지션

    public PrincipalDetails(MembersDto user) {
        this.user = user;
    }

    // 해당 User 권한 리턴하는 곳
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRoles().toString();
            }
        });
        // user.getRoles();
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getMemberId();
    }

    // 계정 만료
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 '아니오'
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 계정 만료 기한 '아니오'
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화
    @Override
    public boolean isEnabled() {
        // 1년 동안 로그인 안한 계청 체크
        // 현재시간 - 로그인 시간 => 1년 초과하면 return false;
        // userDTO에 loginDate 만들어야함.
        return true;
    }

}

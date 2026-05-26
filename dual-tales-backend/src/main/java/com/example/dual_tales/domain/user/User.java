package com.example.dual_tales.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter @Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //ID 자동 생성
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true) //email null값 불허, 한 이메일 복수 계정 생성 불허
    private String email;

    private String nickname;

    @Column(nullable = false)
    private String role; //사용자 관리자 권한 (USER, ADMIN)

    // ================= UserDetails 필수 구현 메서드 ================= //

    /**
     * 사용자가 가진 권한(Role) 목록을 반환합니다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 사용자의 식별값(여기서는 PK인 id)을 반환합니다.
     * (보통 이메일이나 아이디를 쓰지만, 세빈님은 ID 기반 조회를 하므로 id.toString()을 씁니다.)
     */
    @Override
    public String getUsername() {
        return this.id.toString();
    }

    /**
     * 계정 만료 여부 (true: 만료 안 됨)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠김 여부 (true: 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부 (true: 만료 안 됨)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 (true: 활성화됨)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
